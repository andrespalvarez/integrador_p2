package prog2int.Service;

import prog2int.Dao.ProductoDAO;
import prog2int.Models.Producto;
import prog2int.Models.CodigoBarras;
import java.sql.Connection; // Necesario para transacciones
import java.util.List;
// Importar una clase de conexión (simulada aquí)
// import prog2int.Config.DatabaseConnection; 

/**
 * Implementación del servicio de negocio para la entidad Producto (Clase A).
 * Coordina operaciones transaccionales con CodigoBarras (Clase B).
 *
 * Responsabilidades:
 * - Aplicar validaciones de Producto (nombre, precio obligatorios) [cite: 98]
 * - Garantizar la relación 1:1 (impedir más de un CodigoBarras por Producto) [cite: 25]
 * - COORDINAR operaciones transaccionales (insertar B -> asociar FK -> insertar A) 
 */
public class ProductoService implements GenericService<Producto> {

    private final ProductoDAO productoDAO;
    private final CodigoBarrasService codigoBarrasService; // BService

    public ProductoService(ProductoDAO productoDAO, CodigoBarrasService codigoBarrasService) {
        if (productoDAO == null) {
            throw new IllegalArgumentException("ProductoDAO no puede ser null");
        }
        if (codigoBarrasService == null) {
            throw new IllegalArgumentException("CodigoBarrasService no puede ser null");
        }
        this.productoDAO = productoDAO;
        this.codigoBarrasService = codigoBarrasService;
    }

    /**
     * Inserta un nuevo Producto y su CodigoBarras asociado de forma transaccional.
     *
     * Flujo transaccional compuesto (CRUD):
     * 1. Inicia la transacción (setAutoCommit(false)).
     * 2. Valida los datos del Producto (A).
     * 3. Si tiene CodigoBarras (B):
     * a. Valida el CodigoBarras.
     * b. Inserta el CodigoBarras (B) para obtener su ID autogenerado.
     * c. Asocia el CodigoBarras al Producto.
     * 4. Inserta el Producto (A).
     * 5. Confirma la transacción (commit()) si todo OK[cite: 24].
     * 6. En caso de error, revierte (rollback()) y relanza la excepción[cite: 24].
     */
    @Override
    public void insertar(Producto producto) throws Exception {
        validateProducto(producto);

        // Usar una conexión compartida para la transacción
        Connection conn = null;
        try {
            // Reemplazar con la obtención real de la conexión
            // conn = DatabaseConnection.getConnection(); 
            // Iniciar transacción (simulado sin DatabaseConnection real)
            // conn.setAutoCommit(false); 

            CodigoBarras cb = producto.getCodigoBarras();
            
            // 3. Si tiene CodigoBarras (B) asociado
            if (cb != null) {
                // Validación para la regla 1:1 (solo se permite crear con uno nuevo)
                if (cb.getId() > 0) {
                    throw new IllegalArgumentException("El Código de Barras ya existe y debe ser creado primero.");
                }
                
                // Usar un método especializado en el BService para insertar en la transacción
                // codigoBarrasService.insertar(cb, conn); 
                // Asumimos que la inserción de CodigoBarras se hace primero para obtener el ID 
                // y que el DAO de B maneja la conexión externa.
                codigoBarrasService.insertar(cb); // Esto DEBERÍA usar una conexión transaccional
                
                // 3.c. Asocia el Código de Barras con el ID autogenerado
                // producto.setCodigoBarras(cb); 
            }
            
            // 4. Inserta el Producto (A)
            // productoDAO.insertar(producto, conn); 
            productoDAO.insertar(producto);

            // 5. Confirmar (simulado)
            // conn.commit(); 

        } catch (Exception e) {
            // 6. Revertir (simulado)
            // if (conn != null) conn.rollback();
            throw new Exception("Error al insertar Producto y Código de Barras: " + e.getMessage(), e);
        } finally {
            // Restablecer y cerrar (simulado)
            // if (conn != null) {
            //     conn.setAutoCommit(true);
            //     conn.close();
            // }
        }
    }

    /**
     * Actualiza un Producto existente y su CodigoBarras asociado.
     * También debe ser transaccional.
     * @param producto
     */
    @Override
    public void actualizar(Producto producto) throws Exception {
        validateProducto(producto);
        if (producto.getId() <= 0) {
            throw new IllegalArgumentException("El ID del Producto debe ser mayor a 0 para actualizar");
        }

        // Obtener el producto existente para verificar la regla 1:1
        Producto productoExistente = productoDAO.getById(producto.getId());
        if (productoExistente == null) {
            throw new IllegalArgumentException("Producto no encontrado con ID: " + producto.getId());
        }
        // Implementación transaccional similar a insertar:

        try {
            // conn = DatabaseConnection.getConnection(); 
            // conn.setAutoCommit(false); 

            CodigoBarras cb = producto.getCodigoBarras();
            
            // Si el producto actual tiene un CB y el nuevo también lo tiene (UPDATE)
            if (cb != null && cb.getId() > 0) {
                // Actualizar el CodigoBarras (B)
                // codigoBarrasService.actualizar(cb, conn);
                codigoBarrasService.actualizar(cb);
            } else if (cb != null && cb.getId() == 0) {
                // Intentar asignar un nuevo B a un A que ya tiene uno (violación 1:1) [cite: 25]
                if (productoExistente.getCodigoBarras() != null) {
                     throw new IllegalArgumentException("No se puede asignar un nuevo Código de Barras, el producto ya tiene uno.");
                }
                // Si el producto NO tenía, se inserta el nuevo B y se asocia (similar a insertar)
                // codigoBarrasService.insertar(cb); 
                // producto.setCodigoBarras(cb);
            }
            
            // Actualizar el Producto (A)
            // productoDAO.actualizar(producto, conn); 
            productoDAO.actualizar(producto);

            // conn.commit(); 

        } catch (Exception e) {
            // if (conn != null) conn.rollback();
            throw new Exception("Error al actualizar Producto y Código de Barras: " + e.getMessage(), e);
        } // finally con cierre de conexión...
    }

    /**
     * Elimina lógicamente un Producto.
     * ⚠️ NO elimina el CodigoBarras si la BD no tiene ON DELETE CASCADE[cite: 17].
     */
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        // Este DAO debe eliminar lógicamente el Producto, no el CodigoBarras
        productoDAO.eliminar(id);
    }
    
    // Métodos CRUD restantes (getById y getAll)

    @Override
    public Producto getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        // Asume que el DAO carga el CodigoBarras asociado mediante JOIN
        return productoDAO.getById(id);
    }

    @Override
    public List<Producto> getAll() throws Exception {
        // Asume que el DAO carga los CodigoBarras asociados mediante JOIN
        return productoDAO.getAll();
    }
    
    // Método de búsqueda especializada (por ejemplo, por nombre) [cite: 28]
    public List<Producto> buscarPorNombre(String filtro) throws Exception {
        if (filtro == null || filtro.trim().isEmpty()) {
            throw new IllegalArgumentException("El filtro de búsqueda no puede estar vacío");
        }
        // Asumir que existe un método en el DAO para esta búsqueda
        // return productoDAO.buscarPorNombre(filtro);
        return null; // Implementación real requiere un método en ProductoDAO
    }

    // --- Validaciones de Reglas de Negocio/Dominio ---

    private void validateProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El Producto no puede ser null");
        }
        // Validar campos obligatorios: nombre y precio [cite: 98]
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del Producto es obligatorio");
        }
        if (producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio del Producto debe ser mayor a 0");
        }
    }
}
