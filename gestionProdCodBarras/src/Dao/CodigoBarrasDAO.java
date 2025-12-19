package Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Config.DatabaseConnection;
import Entities.CodigoBarras;
import java.time.LocalDate;

// Data Access Object para la entidad Codigo de Barras que depende de Producto..
 
public class CodigoBarrasDAO implements GenericDAO<CodigoBarras> {
   
    private static final String INSERT_SQL = "INSERT INTO codigobarras (tipo, valor, fechaAsignacion, observaciones) VALUES (?, ?, ?, ?)";

       //Instruccion de actualización de Codigo de bArras.
      //Actualiza atributos del codigo de barras.
     
    private static final String UPDATE_SQL = "UPDATE codigobarras SET tipo = ?, valor = ?, fechaAsignacion = ?, observaciones = ?  WHERE id = ?";

    /**
     * Query de eliminacion por medio de uso de instruccion delete.
     * Marca eliminado=TRUE sin borrar físicamente la fila.
     * Preserva integridad referencial y datos históricos.
     */
    private static final String DELETE_SQL = "UPDATE codigobarras SET eliminado = TRUE WHERE id = ?";

    /**
     * Query para obtener Codigo de barra por ID.
     * Solo retorna Codigos activos (eliminado=FALSE).
     */
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM codigobarras WHERE id = ? AND eliminado = FALSE";

    /**
     * Query para obtener todos los Codigos activos.
     * Filtra por eliminado=FALSE (solo Codigos de barra activos).
     */
    private static final String SELECT_ALL_SQL = "SELECT * FROM codigobarras WHERE eliminado = FALSE";

    /**
     * Inserta un Codigo de barras en la base de datos 
     * Crea su propia conexión y la cierra automáticamente protegiendo el sistema. (try-with-resources)
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
      * Inserta un Codigo de barras dentro de una transacción existente.
     * NO crea nueva conexión, recibe una Connection externa.
     
     *
     * @param  CodigoBarras a insertar
     * @param conn Conexión transaccional (NO se cierra en este método)
     * @throws Exception Si falla la inserción, Rollback automático si alguna operación falla
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
     * Actualiza un codigo de barras existente en la base de datos.
     * Actualiza sus parametro
     */
    @Override
    public void actualizar(CodigoBarras codigoBarras) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, codigoBarras.getTipo());
            stmt.setString(2, codigoBarras.getValor());
            stmt.setDate(3, Date.valueOf(codigoBarras.getFechaAsignacion()));
            stmt.setString(4, codigoBarras.getObservaciones());
            stmt.setInt(5, codigoBarras.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el codigobarras con ID: " + codigoBarras.getId());
            }
        }
    }

    /**
     * Eliminacion de un codigo de barras
     * Marca eliminado=TRUE sin borrar físicamente la fila
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
     * Obtiene un Codigo de barras por su ID.
     * Solo retorna codigos activos ( funciona la logica baja = eliminado=FALSE).
     *
     * @param id ID del codigo a buscar
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
     * Obtiene todos los codigos de barra activos (eliminado=FALSE).
     *
     * Nota: Usa Statement (no PreparedStatement) porque no hay parámetros.
     * - MenuHandler opción 7: Listar codigos existentes para asignar a un producto
     *
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
     * Setea los parámetros de codigos de barra en un PreparedStatement.
     * Método auxiliar usado por insertar() e insertTx().
     *
     * Parámetros seteados:: tipo, valor, fecha de asignacion y observaciones
     
     * @param stmt PreparedStatement con INSERT_SQL
     * @param  CodigoBarras con los datos a insertar
     * @throws SQLException Si hay error al setear parámetros
     */
    private void setCodigoBarrasParameters(PreparedStatement stmt, CodigoBarras codigoBarras) throws SQLException {
        stmt.setString(1, codigoBarras.getTipo());
        stmt.setString(2, codigoBarras.getValor());
        stmt.setDate(3, Date.valueOf(codigoBarras.getFechaAsignacion()));
        stmt.setString(4, codigoBarras.getObservaciones());
    }

    /**
     * Obtiene el ID autogenerado por la BD después de un INSERT.
     * Asigna el ID generado al objeto Codigo de barras.
     *
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
     * Mapea un ResultSet a un objeto Codigo de barras.
    
     * Nota: El campo eliminado NO se mapea porque las queries filtran por eliminado=FALSE,
     * garantizando que solo se retornan codigos activos.
     *
     * @param rs ResultSet posicionado en una fila con datos del codigo
     * @return CodigoBarras reconstruido
     * @throws SQLException Si hay error al leer columnas del ResultSet
     */
    private CodigoBarras mapResultSetToCodigoBarras(ResultSet rs) throws SQLException {
        return new CodigoBarras(
            rs.getInt("id"),
            rs.getString("tipo"),
            rs.getString("valor"),    
            rs.getObject("fechaAsignacion", LocalDate.class), 
            rs.getString("observaciones")
        );
    }
}
