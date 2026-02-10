package Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Config.DatabaseConnection;
import Entities.CodigoBarras;
import java.time.LocalDate;

// DAO de CodigoBarras: CRUD, soft delete y consultas.
 
public class CodigoBarrasDAO implements GenericDAO<CodigoBarras> {
   
    private static final String INSERT_SQL = "INSERT INTO codigobarras (tipo, valor, fechaAsignacion, observaciones) VALUES (?, ?, ?, ?)";

// UPDATE de CodigoBarras por ID.

     
    private static final String UPDATE_SQL = "UPDATE codigobarras SET tipo = ?, valor = ?, fechaAsignacion = ?, observaciones = ?  WHERE id = ?";

// Soft delete: marca eliminado=TRUE.

    
    private static final String DELETE_SQL = "UPDATE codigobarras SET eliminado = TRUE WHERE id = ?";

// SELECT por ID de CodigoBarras activo.

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM codigobarras WHERE id = ? AND eliminado = FALSE";

// SELECT de todos los CodigoBarras activos.

    private static final String SELECT_ALL_SQL = "SELECT * FROM codigobarras WHERE eliminado = FALSE";

// Inserta CodigoBarras con ID generado (conexión propia).

    
    @Override
    public void insertar(CodigoBarras codigoBarras) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            setCodigoBarrasParameters(stmt, codigoBarras);
            stmt.executeUpdate();

            setGeneratedId(stmt, codigoBarras);
        }
    }

// Inserta CodigoBarras dentro de una transacción existente.

    @Override
    public void insertTx(CodigoBarras codigobarras, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setCodigoBarrasParameters(stmt, codigobarras);
            stmt.executeUpdate();
            setGeneratedId(stmt, codigobarras);
        }
    }

// Actualiza CodigoBarras por ID. Lanza error si no existe.

    
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

// Soft delete de CodigoBarras por ID.

    
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

// Obtiene CodigoBarras por ID (solo activos).

    
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

 // Devuelve todos los CodigoBarras activos.

    
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

// Carga parámetros de CodigoBarras en PreparedStatement.

    
    private void setCodigoBarrasParameters(PreparedStatement stmt, CodigoBarras codigoBarras) throws SQLException {
        stmt.setString(1, codigoBarras.getTipo());
        stmt.setString(2, codigoBarras.getValor());
        stmt.setDate(3, Date.valueOf(codigoBarras.getFechaAsignacion()));
        stmt.setString(4, codigoBarras.getObservaciones());
    }

// Obtiene y asigna ID generado tras INSERT.

    private void setGeneratedId(PreparedStatement stmt, CodigoBarras codigoBarras) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                codigoBarras.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción del codigo de barras falló, no se obtuvo ID generado");
            }
        }
    }

// Crea CodigoBarras desde ResultSet.

    
    private CodigoBarras mapResultSetToCodigoBarras(ResultSet rs) throws SQLException {
        return new CodigoBarras(
            rs.getInt("id"),
            rs.getString("valor"),
            rs.getString("tipo"),    
            rs.getObject("fechaAsignacion", LocalDate.class), 
            rs.getString("observaciones")
        );
    }
}
