package Dao;

import Entities.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Config.DatabaseConnection;
import Entities.CodigoBarras;

/**
 * Data Access Object para la entidad Producto.
 * Gestiona todas las operaciones de persistencia de Producto en la base de datos.
 *
 * Características:
 * - Implementa GenericDAO <Producto> para operaciones CRUD estándar
 * - Usa PreparedStatements en TODAS las consultas (protección contra SQL injection)
 * - Maneja LEFT JOIN con Codigos de barra para cargar la relación de forma eager
 * - Implementa soft delete (eliminado=TRUE, no DELETE físico)
 * - Proporciona búsquedas especializadas (por nombre con LIKE)
 * - Soporta transacciones mediante insertTx() (recibe Connection externa)

 */
public class ProductoDAO implements GenericDAO<Producto> {
    /**
     * Clase para insertar productos!.
     * Inserta los atributos de productos.
     * El id es AUTO_INCREMENT y se obtiene con RETURN_GENERATED_KEYS.
     */
    private static final String INSERT_SQL = "INSERT INTO producto (nombre, marca, categoria, precio, peso, codigoBarras) VALUES (?, ?, ?, ?, ?, ? )";

    /**
     * Query de actualización de Productos del inventario.
     * Actualiza nombre, marca, categoria, precio, peso y codigo de barras.
     */
    private static final String UPDATE_SQL = "UPDATE producto SET nombre = ?, marca = ?, categoria = ?, precio = ?, peso = ?, codigoBarras = ? WHERE id = ?";

    /**
     * Query de soft delete para eliminar productos.
     * Marca eliminado=TRUE sin borrar físicamente la fila.
     */
    private static final String DELETE_SQL = "UPDATE producto SET eliminado = TRUE WHERE id = ?";

    /**
     * Query para obtener persona por ID.
     * LEFT JOIN con domicilios para cargar la relación de forma eager.
     * Solo retorna personas activas (eliminado=FALSE).
     *
     
     */
    private static final String SELECT_BY_ID_SQL = "SELECT p.id, p.nombre, p.marca, p.categoria, p.precio, p.peso, p.codigoBarras , " +
            "cb.id AS id, cb.tipo, cb.valor, cb.fechaAsignacion, cb.observaciones  " +
            "FROM producto p LEFT JOIN codigobarras cb ON p.codigobarras = cb.id " +
            "WHERE p.id = ? AND p.eliminado = FALSE";

    
     // Query para obtener los productos activos.
    
  
    private static final String SELECT_ALL_SQL = "SELECT p.id, p.nombre, p.marca, p.categoria, p.precio, p.peso, p.codigoBarras , " +
            "cb.id AS id, cb.tipo, cb.valor , cb.fechaAsignacion, cb.observaciones  " +
            "FROM producto p LEFT JOIN codigobarras cb ON p.codigobarras = cb.id " +
            "WHERE p.eliminado = FALSE";

    /**
     * Query de búsqueda con LIKE.
     * Usa % antes y después del filtro: LIKE '%filtro%'
     * Solo productos activos (eliminado=FALSE).
     */
    private static final String SEARCH_BY_NAME_SQL = "SELECT p.id, p.nombre, p.marca, p.categoria, p.precio, p.peso, p.codigoBarras , " +
            "cb.id AS id, cb.tipo, cb.valor , cb.fechaAsignacion, cb.observaciones " +
            "FROM producto p LEFT JOIN codigobarras cb ON p.codigobarras = cb.id " +
            "WHERE p.eliminado = FALSE AND (p.nombre LIKE ? OR p.marca LIKE ?)";

    /**
     * Query de búsqueda exacta por DNI.
     * Usa comparación exacta (=) porque el DNI es único (RN-001).
     * Usado por PersonaServiceImpl.validateDniUnique() para verificar unicidad.
     * Solo personas activas (eliminado=FALSE).
     */
    private static final String SEARCH_BY_DNI_SQL = "SELECT p.id, p.nombre, p.marca, p.categoria, p.precio, p.peso, p.codigoBarras , " +
            "cb.id AS id, cb.tipo, cb.valor " +
            "FROM producto p LEFT JOIN codigobarras cb ON p.codigobarras = cb.id " +
            "WHERE p.eliminado = FALSE"; // AND p.dni = ?"; CHEQUEAR */

    /**
     * DAO de domicilios (actualmente no usado, pero disponible para operaciones futuras).
     * Inyectado en el constructor por si se necesita coordinar operaciones.
     */
    private final CodigoBarrasDAO codigoBarrasDAO;

    /**
     * Constructor con inyección de DomicilioDAO.
     * Valida que la dependencia no sea null (fail-fast).
     *
     * @param domicilioDAO DAO de domicilios
     * @throws IllegalArgumentException si codigoBarrasDAO es null
     */
    public ProductoDAO(CodigoBarrasDAO codigoBarrasDAO) {
        if (codigoBarrasDAO == null) {
            throw new IllegalArgumentException("CodigoBarrasDAO no puede ser null");
        }
        this.codigoBarrasDAO = codigoBarrasDAO;
    }

    /**
     * Inserta una persona en la base de datos (versión sin transacción).
     * Crea su propia conexión y la cierra automáticamente.
     *
     * Flujo:
     * 1. Abre conexión con DatabaseConnection.getConnection()
     * 2. Crea PreparedStatement con INSERT_SQL y RETURN_GENERATED_KEYS
     * 3. Setea parámetros (nombre, apellido, dni, domicilio_id)
     * 4. Ejecuta INSERT
     * 5. Obtiene el ID autogenerado y lo asigna a persona.id
     * 6. Cierra recursos automáticamente (try-with-resources)
     *
     * @param producto Producto a insertar (id será ignorado y regenerado)
     * @throws Exception Si falla la inserción o no se obtiene ID generado
     */
    @Override
    public void insertar(Producto producto) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            setProductoParameters(stmt, producto);
            stmt.executeUpdate();
            setGeneratedId(stmt, producto);
        }
    }

    /**
     * Inserta una persona dentro de una transacción existente.
     * NO crea nueva conexión, recibe una Connection externa.
     * NO cierra la conexión (responsabilidad del caller con TransactionManager).
     *
     * Usado por: (Actualmente no usado, pero disponible para transacciones futuras)
     * - Operaciones que requieren múltiples inserts coordinados
     * - Rollback automático si alguna operación falla
     *
     * @param producto Producto a insertar
     * @param conn Conexión transaccional (NO se cierra en este método)
     * @throws Exception Si falla la inserción
     */
    @Override
    public void insertTx(Producto producto, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setProductoParameters(stmt, producto);
            stmt.executeUpdate();
            setGeneratedId(stmt, producto);
        }
    }

    /**
     * Actualiza una persona existente en la base de datos.
     * Actualiza nombre, apellido, dni y FK domicilio_id.
     *
     * Validaciones:
     * - Si rowsAffected == 0 → La persona no existe o ya está eliminada
     *
     * IMPORTANTE: Este método puede cambiar la FK domicilio_id:
     * - Si persona.domicilio == null → domicilio_id = NULL (desasociar)
     * - Si persona.domicilio.id > 0 → domicilio_id = domicilio.id (asociar/cambiar)
     *
     * @param producto Producto con los datos actualizados (id debe ser > 0)
     * @throws SQLException Si la producto no existe o hay error de BD
     */
    @Override
    public void actualizar(Producto producto) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

           stmt.setString(1, producto.getNombre());          // nombre
           stmt.setString(2, producto.getMarca());           // marca
           stmt.setString(3, producto.getCategoria());       // categoria
           stmt.setString(4, producto.getPrecio());          // precio
           stmt.setString(5, producto.getPeso());            // peso
setCodigoBarrasId(stmt, 6, producto.getCodBarras()); // codigoBarras
           stmt.setInt(7, producto.getId());                 // WHERE id


            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el producto con ID: " + producto.getId());
            }
        }
    }

    /**
     * Elimina lógicamente una persona (soft delete).
     * Marca eliminado=TRUE sin borrar físicamente la fila.
     *
     * Validaciones:
     * - Si rowsAffected == 0 → La persona no existe o ya está eliminada
     *
     * IMPORTANTE: NO elimina el domicilio asociado (correcto según RN-037).
     * Múltiples personas pueden compartir un domicilio.
     *
     * @param id ID de la producto a eliminar
     * @throws SQLException Si la producto no existe o hay error de BD
     */
    @Override
    public void eliminar(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No se encontró producto con ID: " + id);
            }
        }
    }

    /**
     * Obtiene una persona por su ID.
     * Incluye su domicilio asociado mediante LEFT JOIN.
     *
     * @param id ID de la producto a buscar
     * @return Producto encontrada con su codigoBarras, o null si no existe o está eliminada
     * @throws Exception Si hay error de BD (captura SQLException y re-lanza con mensaje descriptivo)
     */
    @Override
    public Producto getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProducto(rs);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener producto por ID: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Obtiene todas las personas activas (eliminado=FALSE).
     * Incluye sus domicilios mediante LEFT JOIN.
     *
     * Nota: Usa Statement (no PreparedStatement) porque no hay parámetros.
     *
     * @return Lista de productos activas con sus domicilios (puede estar vacía)
     * @throws Exception Si hay error de BD
     */
    @Override
    public List<Producto> getAll() throws Exception {
        List<Producto> productos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener todos los productos: " + e.getMessage(), e);
        }
        return productos;
    }

    /**
     * Busca personas por nombre o apellido con búsqueda flexible (LIKE).
     * Permite búsqueda parcial: "juan" encuentra "Juan", "María Juana", etc.
     *
     * Patrón de búsqueda: LIKE '%filtro%' en nombre O apellido
     * Búsqueda case-sensitive en MySQL (depende de la collation de la BD).
     *
     * Ejemplo:
     * - filtro = "garcia" → Encuentra personas con nombre o apellido que contengan "garcia"
     *
     * @param filtro Texto a buscar (no puede estar vacío)
     * @return Lista de productos que coinciden con el filtro (puede estar vacía)
     * @throws IllegalArgumentException Si el filtro está vacío
     * @throws SQLException Si hay error de BD
     */
    public List<Producto> buscarPorNombreMarca(String filtro) throws SQLException {
        if (filtro == null || filtro.trim().isEmpty()) {
            throw new IllegalArgumentException("El filtro de búsqueda no puede estar vacío");
        }

        List<Producto> productos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_NAME_SQL)) {

            // Construye el patrón LIKE: %filtro%
            String searchPattern = "%" + filtro + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapResultSetToProducto(rs));
                }
            }
        }
        return productos;
    }

    /**
     * Busca una persona por DNI exacto.
     * Usa comparación exacta (=) porque el DNI es único en el sistema (RN-001).
     *
     * Uso típico:
     * - PersonaServiceImpl.validateDniUnique() para verificar que el DNI no esté duplicado
     * - MenuHandler opción 4 para buscar persona específica por DNI
     *
     * @param dni DNI exacto a buscar (se aplica trim automáticamente)
     * @return Producto con ese DNI, o null si no existe o está eliminada
     * @throws IllegalArgumentException Si el DNI está vacío
     * @throws SQLException Si hay error de BD
     */
    public Producto buscarPorDni(String dni) throws SQLException {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI no puede estar vacío");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_DNI_SQL)) {

            stmt.setString(1, dni.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProducto(rs);
                }
            }
        }
        return null;
    }

    /**
     * Setea los parámetros de persona en un PreparedStatement.
     * Método auxiliar usado por insertar() e insertTx().
     *
     * Parámetros seteados:
     * 1. nombre (String)
     * 2. apellido (String)
     * 3. dni (String)
     * 4. domicilio_id (Integer o NULL)
     *
     * @param stmt PreparedStatement con INSERT_SQL
     * @param producto Producto con los datos a insertar
     * @throws SQLException Si hay error al setear parámetros
     */
    private void setProductoParameters(PreparedStatement stmt, Producto producto) throws SQLException {
        stmt.setString(1, producto.getNombre());
        stmt.setString(2, producto.getMarca());
        stmt.setString(3, producto.getCategoria());
        stmt.setString(4, producto.getPrecio());
        stmt.setString(5, producto.getPeso());

        setCodigoBarrasId(stmt, 6, producto.getCodBarras());
    }

    /**
     * Setea la FK domicilio_id en un PreparedStatement.
     * Maneja correctamente el caso NULL (persona sin domicilio).
     *
     * Lógica:
     * - Si domicilio != null Y domicilio.id > 0 → Setea el ID
     * - Si domicilio == null O domicilio.id <= 0 → Setea NULL
     *
     * Importante: El tipo Types.INTEGER es necesario para setNull() en JDBC.
     *
     * @param stmt PreparedStatement
     * @param parameterIndex Índice del parámetro (1-based)
     * @param codigoBarras CodigoBarras asociado (puede ser null)
     * @throws SQLException Si hay error al setear el parámetro
     */
    private void setCodigoBarrasId(PreparedStatement stmt, int parameterIndex, CodigoBarras codigoBarras) throws SQLException {
        if (codigoBarras != null && codigoBarras.getId() > 0) {
            stmt.setInt(parameterIndex, codigoBarras.getId());
        } else {
            stmt.setNull(parameterIndex, Types.INTEGER);
        }
    }

    /**
     * Obtiene el ID autogenerado por la BD después de un INSERT.
     * Asigna el ID generado al objeto persona.
     *
     * IMPORTANTE: Este método es crítico para mantener la consistencia:
     * - Después de insertar, el objeto persona debe tener su ID real de la BD
     * - Permite usar persona.getId() inmediatamente después de insertar
     * - Necesario para operaciones transaccionales que requieren el ID generado
     *
     * @param stmt PreparedStatement que ejecutó el INSERT con RETURN_GENERATED_KEYS
     * @param persona Objeto producto a actualizar con el ID generado
     * @throws SQLException Si no se pudo obtener el ID generado (indica problema grave)
     */
    private void setGeneratedId(PreparedStatement stmt, Producto producto) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                producto.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción del producto falló, no se obtuvo ID generado");
            }
        }
    }

    /**
     * Mapea un ResultSet a un objeto Producto
     */
    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setNombre(rs.getString("nombre"));
        producto.setMarca(rs.getString("marca"));
        producto.setCategoria(rs.getString("categoria"));
        producto.setPrecio(rs.getString("precio"));
        producto.setPeso(rs.getString("peso"));

        // Manejo correcto de LEFT JOIN: verificar si codigoBarras es NULL
        int codigoBarrasId = rs.getInt("id");
        if (codigoBarrasId > 0 && !rs.wasNull()) {
            CodigoBarras codigoBarras = new CodigoBarras();
            codigoBarras.setId(rs.getInt("id"));
            codigoBarras.setTipo(rs.getString("tipo"));
            codigoBarras.setValor(rs.getString("valor"));
            codigoBarras.setFechaAsignacion(rs.getString("fechaAsignacion"));
            codigoBarras.setObservaciones(rs.getString("observaciones"));
            
            producto.setCodBarras(codigoBarras);
        }

        return producto;
    }
}
