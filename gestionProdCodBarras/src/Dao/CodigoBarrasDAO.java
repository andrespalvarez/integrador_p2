package Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Config.DatabaseConnection;
import Entities.CodigoBarras;

/**
 * Data Access Object para la entidad Domicilio.
 * Gestiona todas las operaciones de persistencia de domicilios en la base de datos.
 *
 * Características:
 * - Implementa GenericDAO<Domicilio> para operaciones CRUD estándar
 - Usa PreparedStatements en TODAS las consultas (protección contra SQL injection)
 - Implementa soft delete (eliminado=TRUE, no DELETE físico)
 - NO maneja relaciones (CodigoBarras es entidad independiente)
 - Soporta transacciones mediante insertTx() (recibe Connection externa)

 Diferencias con PersonaDAO:
 - Más simple: NO tiene LEFT JOINs (CodigoBarras no tiene relaciones cargadas)
 - NO tiene búsquedas especializadas (solo CRUD básico)
 - Todas las queries filtran por eliminado=FALSE (soft delete)

 Patrón: DAO con try-with-resources para manejo automático de recursos JDBC
 */
public class CodigoBarrasDAO implements GenericDAO<CodigoBarras> {
    /**
     * Query de inserción de domicilio.
     * Inserta calle y número.
     * El id es AUTO_INCREMENT y se obtiene con RETURN_GENERATED_KEYS.
     * El campo eliminado tiene DEFAULT FALSE en la BD.
     */
    private static final String INSERT_SQL = "INSERT INTO codigobarras (tipo, valor, fechaImplantacion, observaciones) VALUES (?, ?, ?, ?)";

    /**
     * Query de actualización de domicilio.
     * Actualiza calle y número por id.
     * NO actualiza el flag eliminado (solo se modifica en soft delete).
     *
     * ⚠️ IMPORTANTE: Si varias personas comparten este domicilio,
     * la actualización los afectará a TODAS (RN-040).
     */
    private static final String UPDATE_SQL = "UPDATE codigobarras SET tipo = ?, valor = ?, fechaAsignacion = ?, observaciones = ?  WHERE id = ?";

    /**
     * Query de soft delete.
     * Marca eliminado=TRUE sin borrar físicamente la fila.
     * Preserva integridad referencial y datos históricos.
     *
     * ⚠️ PELIGRO: Este método NO verifica si hay personas asociadas.
     * Puede dejar FKs huérfanas (personas.domicilio_id apuntando a domicilio eliminado).
     * ALTERNATIVA SEGURA: PersonaServiceImpl.eliminarDomicilioDePersona()
     */
    private static final String DELETE_SQL = "UPDATE codigobarras SET eliminado = TRUE WHERE id = ?";

    /**
     * Query para obtener domicilio por ID.
     * Solo retorna domicilios activos (eliminado=FALSE).
     * SELECT * es aceptable aquí porque Domicilio tiene solo 4 columnas.
     */
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM codigobarras WHERE id = ? AND eliminado = FALSE";

    /**
     * Query para obtener todos los domicilios activos.
     * Filtra por eliminado=FALSE (solo domicilios activos).
     * SELECT * es aceptable aquí porque Domicilio tiene solo 4 columnas.
     */
    private static final String SELECT_ALL_SQL = "SELECT * FROM codigobarras WHERE eliminado = FALSE";

    /**
     * Inserta un domicilio en la base de datos (versión sin transacción).
     * Crea su propia conexión y la cierra automáticamente.
     *
     * Flujo:
     * 1. Abre conexión con DatabaseConnection.getConnection()
     * 2. Crea PreparedStatement con INSERT_SQL y RETURN_GENERATED_KEYS
     * 3. Setea parámetros (calle, numero)
     * 4. Ejecuta INSERT
     * 5. Obtiene el ID autogenerado y lo asigna a domicilio.id
     * 6. Cierra recursos automáticamente (try-with-resources)
     *
     * IMPORTANTE: El ID generado se asigna al objeto domicilio.
     * Esto permite que PersonaServiceImpl.insertar() use domicilio.getId()
     * inmediatamente después de insertar.
     *
     * @param domicilio CodigoBarras a insertar (id será ignorado y regenerado)
     * @throws SQLException Si falla la inserción o no se obtiene ID generado
     */
    @Override
    public void insertar(CodigoBarras codigoBarras) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            setCodigoBarrasParameters(stmt, codigoBarras);
            stmt.executeUpdate();

            setGeneratedId(stmt, codigoBarras);
        }
    }

    /**
     * Inserta un domicilio dentro de una transacción existente.
     * NO crea nueva conexión, recibe una Connection externa.
     * NO cierra la conexión (responsabilidad del caller con TransactionManager).
     *
     * Usado por: (Actualmente no usado, pero disponible para transacciones futuras)
     * - Operaciones que requieren múltiples inserts coordinados
     * - Rollback automático si alguna operación falla
     *
     * @param domicilio CodigoBarras a insertar
     * @param conn Conexión transaccional (NO se cierra en este método)
     * @throws Exception Si falla la inserción
     */
    @Override
    public void insertTx(CodigoBarras codigobarras, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setCodigoBarrasParameters(stmt, codigobarras);
            stmt.executeUpdate();
            setGeneratedId(stmt, codigobarras);
        }
    }

    /**
     * Actualiza un domicilio existente en la base de datos.
     * Actualiza calle y número.
     *
     * Validaciones:
     * - Si rowsAffected == 0 → El domicilio no existe o ya está eliminado
     *
     * ⚠️ IMPORTANTE: Si varias personas comparten este domicilio,
     * la actualización los afectará a TODAS (RN-040).
     * Ejemplo:
     * - Domicilio ID=1 "Av. Siempreviva 742" tiene 3 personas asociadas
     * - actualizar(domicilio con calle="Calle Nueva") cambia la dirección de las 3 personas
     *
     * Esto es CORRECTO: permite que familias compartan la misma dirección
     * y se actualice en un solo lugar.
     *
     * @param domicilio CodigoBarras con los datos actualizados (id debe ser > 0)
     * @throws SQLException Si el domicilio no existe o hay error de BD
     */
    @Override
    public void actualizar(CodigoBarras codigoBarras) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, codigoBarras.getTipo());
            stmt.setString(2, codigoBarras.getValor());
            stmt.setString(3, codigoBarras.getFechaAsignacion());
            stmt.setString(4, codigoBarras.getObservaciones());
            stmt.setInt(5, codigoBarras.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el codigobarras con ID: " + codigoBarras.getId());
            }
        }
    }

    /**
     * Elimina lógicamente un domicilio (soft delete).
     * Marca eliminado=TRUE sin borrar físicamente la fila.
     *
     * Validaciones:
     * - Si rowsAffected == 0 → El domicilio no existe o ya está eliminado
     *
     * ⚠️ PELIGRO: Este método NO verifica si hay personas asociadas (RN-029).
     * Si hay personas con personas.domicilio_id apuntando a este domicilio,
     * quedarán con FK huérfana (apuntando a un domicilio eliminado).
     *
     * Esto puede causar:
     * - Datos inconsistentes (persona asociada a domicilio "eliminado")
     * - Errores en LEFT JOINs que esperan domicilios activos
     *
     * ALTERNATIVA SEGURA: PersonaServiceImpl.eliminarDomicilioDePersona()
     * - Primero actualiza persona.domicilio_id = NULL
     * - Luego elimina el domicilio
     * - Garantiza que no queden FKs huérfanas
     *
     * Este método se mantiene para casos donde:
     * - Se está seguro de que el domicilio NO tiene personas asociadas
     * - Se quiere eliminar domicilios en lote (administración)
     *
     * @param id ID del domicilio a eliminar
     * @throws SQLException Si el domicilio no existe o hay error de BD
     */
    @Override
    public void eliminar(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No se encontró codigobarras con ID: " + id);
            }
        }
    }

    /**
     * Obtiene un domicilio por su ID.
     * Solo retorna domicilios activos (eliminado=FALSE).
     *
     * @param id ID del domicilio a buscar
     * @return CodigoBarras encontrado, o null si no existe o está eliminado
     * @throws SQLException Si hay error de BD
     */
    @Override
    public CodigoBarras getById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCodigoBarras(rs);
                }
            }
        }
        return null;
    }

    /**
     * Obtiene todos los domicilios activos (eliminado=FALSE).
     *
     * Nota: Usa Statement (no PreparedStatement) porque no hay parámetros.
     *
     * Uso típico:
     * - MenuHandler opción 7: Listar domicilios existentes para asignar a persona
     *
     * @return Lista de domicilios activos (puede estar vacía)
     * @throws SQLException Si hay error de BD
     */
    @Override
    public List<CodigoBarras> getAll() throws SQLException {
        List<CodigoBarras> codigosBarras = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                codigosBarras.add(mapResultSetToCodigoBarras(rs));
            }
        }

        return codigosBarras;
    }

    /**
     * Setea los parámetros de domicilio en un PreparedStatement.
     * Método auxiliar usado por insertar() e insertTx().
     *
     * Parámetros seteados:
     * 1. calle (String)
     * 2. numero (String)
     *
     * @param stmt PreparedStatement con INSERT_SQL
     * @param domicilio CodigoBarras con los datos a insertar
     * @throws SQLException Si hay error al setear parámetros
     */
    private void setCodigoBarrasParameters(PreparedStatement stmt, CodigoBarras codigoBarras) throws SQLException {
        stmt.setString(1, codigoBarras.getTipo());
        stmt.setString(2, codigoBarras.getValor());
        stmt.setString(3, codigoBarras.getFechaAsignacion());
        stmt.setString(4, codigoBarras.getObservaciones());
    }

    /**
     * Obtiene el ID autogenerado por la BD después de un INSERT.
     * Asigna el ID generado al objeto domicilio.
     *
     * IMPORTANTE: Este método es crítico para mantener la consistencia:
     * - Después de insertar, el objeto domicilio debe tener su ID real de la BD
     * - PersonaServiceImpl.insertar() depende de esto para setear la FK:
     *   1. domicilioService.insertar(domicilio) → domicilio.id se setea aquí
     *   2. personaDAO.insertar(persona) → usa persona.getDomicilio().getId() para la FK
     * - Necesario para operaciones transaccionales que requieren el ID generado
     *
     * @param stmt PreparedStatement que ejecutó el INSERT con RETURN_GENERATED_KEYS
     * @param domicilio Objeto domicilio a actualizar con el ID generado
     * @throws SQLException Si no se pudo obtener el ID generado (indica problema grave)
     */
    private void setGeneratedId(PreparedStatement stmt, CodigoBarras codigoBarras) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                codigoBarras.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción del codigo de barras falló, no se obtuvo ID generado");
            }
        }
    }

    /**
     * Mapea un ResultSet a un objeto Domicilio.
     * Reconstruye el objeto usando el constructor completo.
     *
     * Mapeo de columnas:
     * - id → id
     * - calle → calle
     * - numero → numero
     *
     * Nota: El campo eliminado NO se mapea porque las queries filtran por eliminado=FALSE,
     * garantizando que solo se retornan domicilios activos.
     *
     * @param rs ResultSet posicionado en una fila con datos de domicilio
     * @return CodigoBarras reconstruido
     * @throws SQLException Si hay error al leer columnas del ResultSet
     */
    private CodigoBarras mapResultSetToCodigoBarras(ResultSet rs) throws SQLException {
        return new CodigoBarras(
            rs.getInt("id"),
            rs.getString("tipo"),
            rs.getString("valor"),    
            rs.getString("fechaAsignacion"),
            rs.getString("observaciones")
        );
    }
}