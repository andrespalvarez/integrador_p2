package Service;

import Entities.Producto;

import java.util.List;
import Dao.ProductoDAO;

// Capa de servicio: valida reglas de negocio y coordina operaciones entre UI y DAO.


public class ProductoServiceImpl implements GenericService<Producto> {


// DAO responsable del acceso a datos de Producto.
 

    private final ProductoDAO productoDAO;

//Servicio auxiliar para manejar la lógica del Código de Barras.
    
    
    private final CodigoBarrasServiceImpl codigoBarrasServiceImpl;

// Constructor que recibe las dependencias necesarias.
// Valida que no sean null.


    public ProductoServiceImpl(ProductoDAO productoDAO, CodigoBarrasServiceImpl codigoBarrasServiceImpl) {
        if (productoDAO == null) {
            throw new IllegalArgumentException("ProductoDAO no puede ser null");
        }
        if (codigoBarrasServiceImpl == null) {
            throw new IllegalArgumentException("CodigoBarrasServiceImpl no puede ser null");
        }
        this.productoDAO = productoDAO;
        this.codigoBarrasServiceImpl = codigoBarrasServiceImpl;
    }

    // Inserta un producto luego de validar sus datos.
    
    
    @Override
    public void insertar(Producto producto) throws Exception {
        validateProducto(producto);
        

        if (producto.getCodBarras() != null) {
            if (producto.getCodBarras().getId() == 0) {
                // CodigoBarras nuevo: insertar primero para obtener ID autogenerado
                codigoBarrasServiceImpl.insertar(producto.getCodBarras());
            } else {
                // CodigoBarras existente: actualizar datos
                codigoBarrasServiceImpl.actualizar(producto.getCodBarras());
            }
        }

        productoDAO.insertar(producto);
    }

// Actualiza un producto existente después de validar sus datos.
    
    
    
    @Override
    public void actualizar(Producto persona) throws Exception {
        validateProducto(persona);
        if (persona.getId() <= 0) {
            throw new IllegalArgumentException("El ID de la persona debe ser mayor a 0 para actualizar");
        }
        productoDAO.actualizar(persona);
    }

// Elimina un producto por ID luego de validar el parámetro.

    
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        productoDAO.eliminar(id);
    }

// Devuelve un Producto por su ID.
    
    
    @Override
    public Producto getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return productoDAO.getById(id);
    }

// Devuelve todos los productos disponibles.}
    
    
    @Override
    public List<Producto> getAll() throws Exception {
        return productoDAO.getAll();
    }

   
     // @return Instancia de CodigoBarrasServiceImpl inyectada en este servicio
     
    public CodigoBarrasServiceImpl getCodigoBarrasService() {
        return this.codigoBarrasServiceImpl;
    }

// Busca productos por nombre o marca usando coincidencias parciales.
    
    
    public List<Producto> buscarPorNombreMarca(String filtro) throws Exception {
        if (filtro == null || filtro.trim().isEmpty()) {
            throw new IllegalArgumentException("El filtro de búsqueda no puede estar vacío");
        }
        return productoDAO.buscarPorNombreMarca(filtro);
    }

    
    public void eliminarCodigoBarrasDeProducto(int personaId, int domicilioId) throws Exception {
        if (personaId <= 0 || domicilioId <= 0) {
            throw new IllegalArgumentException("Los IDs deben ser mayores a 0");
        }

        Producto persona = productoDAO.getById(personaId);
        if (persona == null) {
            throw new IllegalArgumentException("Persona no encontrada con ID: " + personaId);
        }

        if (persona.getCodBarras() == null || persona.getCodBarras().getId() != domicilioId) {
            throw new IllegalArgumentException("El domicilio no pertenece a esta persona");
        }

        // Secuencia transaccional: actualizar FK → eliminar domicilio
        persona.setCodBarras(null);
        productoDAO.actualizar(persona);
        codigoBarrasServiceImpl.eliminar(domicilioId);
    }

// Valida que un producto tenga nombre, marca y categoría válidos.
    
    
    private void validateProducto(Producto persona) {
        if (persona == null) {
            throw new IllegalArgumentException("La persona no puede ser null");
        }
        if (persona.getNombre() == null || persona.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (persona.getMarca() == null || persona.getMarca().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
        if (persona.getCategoria() == null || persona.getCategoria().trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI no puede estar vacío");
        }
    }
}

    