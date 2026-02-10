package Dao;

import Entities.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Config.DatabaseConnection;
import Entities.CodigoBarras;
import java.time.LocalDate;

//DAO de Producto: gestiona CRUD, soft delete y consultas con JOIN a CodigoBarras.
//Usa PreparedStatement, soporta transacciones y carga la relación Producto–Código.


public class ProductoDAO implements GenericDAO<Producto> {
    
// INSERT de Producto con ID autogenerado.

    
    private static final String INSERT_SQL = "INSERT INTO producto (nombre, marca, categoria, precio, peso, codigoBarras) VALUES (?, ?, ?, ?, ?, ? )";

//UPDATE de Producto por ID.

    private static final String UPDATE_SQL = "UPDATE producto SET nombre = ?, marca = ?, categoria = ?, precio = ?, peso = ?, codigoBarras = ? WHERE id = ?";

// Soft delete: marca eliminado=TRUE.

    private static final String DELETE_SQL = "UPDATE producto SET eliminado = TRUE WHERE id = ?";

// SELECT por ID con LEFT JOIN a CodigoBarras. Solo productos activos.

    
    
    private static final String SELECT_BY_ID_SQL = "SELECT p.id, p.nombre, p.marca, p.categoria, p.precio, p.peso, p.codigoBarras , " +
            "cb.id AS id, cb.tipo, cb.valor, cb.fechaAsignacion, cb.observaciones  " +
            "FROM producto p LEFT JOIN codigobarras cb ON p.codigobarras = cb.id " +
            "WHERE p.id = ? AND p.eliminado = FALSE";

    
// SELECT de todos los productos activos con JOIN a CodigoBarras.
    
  
    private static final String SELECT_ALL_SQL = "SELECT p.id, p.nombre, p.marca, p.categoria, p.precio, p.peso, p.codigoBarras , " +
            "cb.id AS id, cb.tipo, cb.valor , cb.fechaAsignacion, cb.observaciones  " +
            "FROM producto p LEFT JOIN codigobarras cb ON p.codigobarras = cb.id " +
            "WHERE p.eliminado = FALSE";

// Búsqueda por nombre o marca usando LIKE.

    
    private static final String SEARCH_BY_NAME_SQL = "SELECT p.id, p.nombre, p.marca, p.categoria, p.precio, p.peso, p.codigoBarras , " +
            "cb.id AS id, cb.tipo, cb.valor , cb.fechaAsignacion, cb.observaciones " +
            "FROM producto p LEFT JOIN codigobarras cb ON p.codigobarras = cb.id " +
            "WHERE p.eliminado = FALSE AND (p.nombre LIKE ? OR p.marca LIKE ?)";

   
   
    private final CodigoBarrasDAO codigoBarrasDAO;

    
// Constructor que recibe CodigoBarrasDAO (no debe ser null).

    public ProductoDAO(CodigoBarrasDAO codigoBarrasDAO) {
        if (codigoBarrasDAO == null) {
            throw new IllegalArgumentException("CodigoBarrasDAO no puede ser null");
        }
        this.codigoBarrasDAO = codigoBarrasDAO;
    }

 // Inserta un producto usando conexión propia. Asigna ID generado.

    
    @Override
    public void insertar(Producto producto) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            setProductoParameters(stmt, producto);
            stmt.executeUpdate();
            setGeneratedId(stmt, producto);
        }
    }

    
    @Override
    public void insertTx(Producto producto, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setProductoParameters(stmt, producto);
            stmt.executeUpdate();
            setGeneratedId(stmt, producto);
        }
    }

 // Actualiza Producto por ID. Lanza excepción si no existe.

    
    @Override
    public void actualizar(Producto producto) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

           stmt.setString(1, producto.getNombre());          // nombre
           stmt.setString(2, producto.getMarca());           // marca
           stmt.setString(3, producto.getCategoria());       // categoria
           stmt.setDouble(4, producto.getPrecio());          // precio
           stmt.setDouble(5, producto.getPeso());            // peso
setCodigoBarrasId(stmt, 6, producto.getCodBarras()); // codigoBarras
           stmt.setInt(7, producto.getId());                 // WHERE id


            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el producto con ID: " + producto.getId());
            }
        }
    }

    // Soft delete de Producto. Error si no se encuentra.

    
    
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

 //  Obtiene Producto por ID con JOIN a CodigoBarras.

    
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

 // Devuelve todos los productos activos.

    
    
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

 // Busca por nombre o marca con LIKE.

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

    

    //  Carga parámetros del Producto en PreparedStatement.

    
    
    private void setProductoParameters(PreparedStatement stmt, Producto producto) throws SQLException {
        stmt.setString(1, producto.getNombre());
        stmt.setString(2, producto.getMarca());
        stmt.setString(3, producto.getCategoria());
        stmt.setDouble(4, producto.getPrecio());
        stmt.setDouble(5, producto.getPeso());

        setCodigoBarrasId(stmt, 6, producto.getCodBarras());
    }

  //  Asigna ID de CodigoBarras o NULL.

    
    
    private void setCodigoBarrasId(PreparedStatement stmt, int parameterIndex, CodigoBarras codigoBarras) throws SQLException {
        if (codigoBarras != null && codigoBarras.getId() > 0) {
            stmt.setInt(parameterIndex, codigoBarras.getId());
        } else {
            stmt.setNull(parameterIndex, Types.INTEGER);
        }
       
    }

 // Lee el ID generado y lo asigna al producto.

    private void setGeneratedId(PreparedStatement stmt, Producto producto) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                producto.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción del producto falló, no se obtuvo ID generado");
            }
        }
    }

  // Crea Producto desde ResultSet, incluyendo CodigoBarras si existe.

    
    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setNombre(rs.getString("nombre"));
        producto.setMarca(rs.getString("marca"));
        producto.setCategoria(rs.getString("categoria"));
        producto.setPrecio(rs.getDouble("precio"));
        producto.setPeso(rs.getDouble("peso"));

        
 // Manejo correcto de LEFT JOIN: verificar si codigoBarras es NULL
  
 
 int codigoBarrasId = rs.getInt("id");
        if (codigoBarrasId > 0 && !rs.wasNull()) {
            CodigoBarras codigoBarras = new CodigoBarras();
            codigoBarras.setId(rs.getInt("id"));
            codigoBarras.setTipo(rs.getString("tipo"));
            codigoBarras.setValor(rs.getString("valor"));
            codigoBarras.setFechaAsignacion(rs.getObject("fechaAsignacion", LocalDate.class));
            codigoBarras.setObservaciones(rs.getString("observaciones"));
            
            producto.setCodBarras(codigoBarras);
        }

        return producto;
    }
}
