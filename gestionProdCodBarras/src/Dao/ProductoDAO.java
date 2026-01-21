package Dao;

import Entities.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Config.DatabaseConnection;
import Entities.CodigoBarras;
import java.time.LocalDate;

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

   
    private final CodigoBarrasDAO codigoBarrasDAO;

    /**
     * Constructor con inyección de CodigoBarrasDAO.
     * Valida que la dependencia no sea null (fail-fast).
     *
     * @param CodigoBarraDAO DAO de codigobarra
     * @throws IllegalArgumentException si codigoBarrasDAO es null
     */
    public ProductoDAO(CodigoBarrasDAO codigoBarrasDAO) {
        if (codigoBarrasDAO == null) {
            throw new IllegalArgumentException("CodigoBarrasDAO no puede ser null");
        }
        this.codigoBarrasDAO = codigoBarrasDAO;
    }

    /**
     * Inserta un producto en la base de datos (versión sin transacción).
     * Crea su propia conexión y la cierra automáticamente.
     *
     * Flujo:
     * 1. Abre conexión con DatabaseConnection.getConnection()
     * 2. Crea PreparedStatement con INSERT_SQL y RETURN_GENERATED_KEYS
     * 3. Setea parámetros
     * 4. Ejecuta INSERT
     * 5. Obtiene el ID autogenerado y lo asigna a producto.id
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
     * Actualiza un producto existente en la base de datos.
     * Actualiza sus parametros
     *
     * Validaciones:
     * - Si rowsAffected == 0 → El producto no existe o ya está eliminada
    
     * @param Producto con los datos actualizados (id debe ser > 0)
     * @throws SQLException Si el producto no existe o hay error de BD
     */
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

    /**
     * Elimina lógicamente un Producto (soft delete).
     * Marca eliminado=TRUE sin borrar físicamente la fila.
     *
     * Validaciones:
     * - Si rowsAffected == 0 → La persona no existe o ya está eliminada
    
     * @param id ID del producto a eliminar
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
     * Obtiene uN Producto por su ID.
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
     * Obtiene los productos activas (eliminado=FALSE).
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
     * Busqueda de productos con validaciones.
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
     * Setea los parámetros de Producto en un PreparedStatement.
     * Método auxiliar usado por insertar() e insertTx()
     *
     * @param stmt PreparedStatement con INSERT_SQL
     * @param producto Producto con los datos a insertar
     * @throws SQLException Si hay error al setear parámetros
     */
    private void setProductoParameters(PreparedStatement stmt, Producto producto) throws SQLException {
        stmt.setString(1, producto.getNombre());
        stmt.setString(2, producto.getMarca());
        stmt.setString(3, producto.getCategoria());
        stmt.setDouble(4, producto.getPrecio());
        stmt.setDouble(5, producto.getPeso());

        setCodigoBarrasId(stmt, 6, producto.getCodBarras());
    }

    /**
     * Importante: El tipo Types.INTEGER es necesario para setNull() en JDBC.
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
     * Asigna el ID generado a la entidad Producto.
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
