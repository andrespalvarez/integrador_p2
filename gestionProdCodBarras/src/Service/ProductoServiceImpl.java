package Service;

import Entities.Producto;

import java.util.List;
import Dao.ProductoDAO;

//Capa intermedia entre la UI y el DAO que aplica validaciones de negocio complejas de la entidad producto.
 
public class ProductoServiceImpl implements GenericService<Producto> {
    /**
     * DAO para acceso a datos de productos.
     * Inyectado en el constructor (Dependency Injection).
     */
    private final ProductoDAO productoDAO;

    /**
     * Servicio de productos para coordinar operaciones transaccionales.
     * IMPORTANTE: ProductoServiceImpl necesita CodigobarrasServiceImple 
     */
    private final CodigoBarrasServiceImpl codigoBarrasServiceImpl;

    /**
     * Constructor con inyección de dependencias.
     * Valida que ambas dependencias no sean null (fail-fast).
     *
     * @param productoDAO DAO de CodigoBarras (normalmente ProductoDAO)
     * @param codigoBarrasServiceImpl Servicio de codigo de barras para operaciones coordinadas
     * @throws IllegalArgumentException si alguna dependencia es null
     */
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

    /**
     * Inserta un nuevo producto en la base de datos.
     *
     * Flujo transaccional complejo:
     * 1. Valida que los datos del producto sean correctos
     * 2. Valida que el ID sea único en el sistema (RN-001)
    

     * @param producto Producto a insertar (id será ignorado y regenerado)
     * @throws Exception Si la validación falla, el id está duplicado, o hay error de BD
     */
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

    /**
     * Actualiza un producto existente en la base de datos.
     *
     * Validaciones:
     * - La producto debe tener datos válidos (los parametros, nombre, marca, etc)
     * - El ID debe ser > 0 (debe ser una producto ya persistida)
     *
     
     *
     * @param persona Producto con los datos actualizados
     * @throws Exception Si la validación falla, el DNI está duplicado, o la persona no existe
     */
    @Override
    public void actualizar(Producto persona) throws Exception {
        validateProducto(persona);
        if (persona.getId() <= 0) {
            throw new IllegalArgumentException("El ID de la persona debe ser mayor a 0 para actualizar");
        }
//        validateDniUnique(persona.getCategoria(), persona.getId());
        productoDAO.actualizar(persona);
    }

    /**
     * Elimina lógicamente un producto (soft delete).
     * Marca el producto como eliminado=TRUE sin borrarla físicamente.
     *
     
     * @param id ID del producto a eliminar
     * @throws Exception Si id <= 0 o no existe el producto
     */
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        productoDAO.eliminar(id);
    }

    /**
     * Obtiene un producto por su ID.
     * Incluye el codigo de barras asociado mediante LEFT JOIN (CodigobarrasDAO).
     *
     * @param id ID del producto a buscar
     * @return Producto encontrado (con su codigo si tiene), o null si no existe o está eliminado
     * @throws Exception Si id <= 0 o hay error de BD
     */
    @Override
    public Producto getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return productoDAO.getById(id);
    }

    /**
     * Obtiene todas las productos activos (eliminado=FALSE).
     * Incluye sus codigos mediante LEFT JOIN (CodigobarrasDAO).
     *
     * @return Lista de productos activos con sus codigos (puede estar vacía)
     * @throws Exception Si hay error de BD
     */
    @Override
    public List<Producto> getAll() throws Exception {
        return productoDAO.getAll();
    }

   
     // @return Instancia de CodigoBarrasServiceImpl inyectada en este servicio
     
    public CodigoBarrasServiceImpl getCodigoBarrasService() {
        return this.codigoBarrasServiceImpl;
    }

    /**
     * Busca productos por nombre (búsqueda flexible con LIKE).
     *
     * Uso típico: El usuario ingresa "mesa" y encuentra "mesa redonda", "Mesa cuadrada", etc.
     *
     
     */
    public List<Producto> buscarPorNombreMarca(String filtro) throws Exception {
        if (filtro == null || filtro.trim().isEmpty()) {
            throw new IllegalArgumentException("El filtro de búsqueda no puede estar vacío");
        }
        return productoDAO.buscarPorNombreMarca(filtro);
    }

    /**
     * Busca una persona por DNI exacto.
     * Usa PersonaDAO.buscarPorDni() que realiza búsqueda exacta (=).
     *
     * Uso típico:
     * - Validar unicidad del DNI (validateDniUnique)
     * - Buscar persona específica desde el menú (opción 4)
     *
     * @param dni DNI exacto a buscar (no puede estar vacío)
     * @return Producto con ese DNI, o null si no existe o está eliminada
     * @throws IllegalArgumentException Si el DNI está vacío
     * @throws Exception Si hay error de BD
         
    public Producto buscarPorDni(String dni) throws Exception {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI no puede estar vacío");
        }
        return productoDAO.buscarPorDni(dni);
    }

    /**
     * Elimina un domicilio de forma SEGURA actualizando primero la FK de la persona.
     * Este es el método RECOMENDADO para eliminar domicilios (RN-029 solucionado).
     *
     * Flujo transaccional SEGURO:
     * 1. Obtiene la persona por ID y valida que exista
     * 2. Verifica que el domicilio pertenezca a esa persona (evita eliminar domicilio ajeno)
     * 3. Desasocia el domicilio de la persona (persona.domicilio = null)
     * 4. Actualiza la persona en BD (domicilio_id = NULL)
     * 5. Elimina el domicilio (ahora no hay FKs apuntando a él)
     *
     * DIFERENCIA con DomicilioService.eliminar():
     * - DomicilioService.eliminar(): Elimina directamente (PELIGROSO, puede dejar FKs huérfanas)
     * - Este método: Primero actualiza FK, luego elimina (SEGURO)
     *
     * Usado en MenuHandler opción 10: "Eliminar domicilio de una persona"
     *
     * @param personaId ID de la persona dueña del domicilio
     * @param domicilioId ID del domicilio a eliminar
     * @throws IllegalArgumentException Si los IDs son <= 0, la persona no existe, o el domicilio no pertenece a la persona
     * @throws Exception Si hay error de BD
     */
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

    /**
     * Valida que un producto tenga datos correctos.
     *
     * Reglas de negocio aplicadas:
     * - RN-035: Nombre, apellido y DNI son obligatorios
     * - RN-036: Se verifica trim() para evitar strings solo con espacios
     *
     * @param  Producto a validar
     * @throws IllegalArgumentException Si alguna validación falla
     */
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

    /**
     * Valida que un DNI sea único en el sistema.
     * Implementa la regla de negocio RN-001: "El DNI debe ser único".
     *
     * Lógica:
     * 1. Busca si existe una persona con ese DNI en la BD
     * 2. Si NO existe → OK, el DNI es único
     * 3. Si existe → Verifica si es la misma persona que estamos actualizando:
     *    a. Si personaId == null (INSERT) → Error, DNI duplicado
     *    b. Si personaId != null (UPDATE) y existente.id == personaId → OK, es la misma persona
     *    c. Si personaId != null (UPDATE) y existente.id != personaId → Error, DNI duplicado
     *
     * Ejemplo de uso correcto en UPDATE:
     * - Persona ID=5 con DNI="12345678" quiere actualizar su nombre
     * - validateDniUnique("12345678", 5) → Encuentra persona con DNI="12345678" (ID=5)
     * - Como existente.id (5) == personaId (5) → OK, la persona se está actualizando a sí misma
     *
     * @param dni DNI a validar
     * @param personaId ID de la persona (null para INSERT, != null para UPDATE)
     * @throws IllegalArgumentException Si el DNI ya existe y pertenece a otra persona
     * @throws Exception Si hay error de BD al buscar
     */  /*
    private void validateDniUnique(String dni, Integer personaId) throws Exception {
        Producto existente = productoDAO.buscarPorDni(dni);
        if (existente != null) {
            // Existe una persona con ese DNI
            if (personaId == null || existente.getId() != personaId) {
                // Es INSERT (personaId == null) o es UPDATE pero el DNI pertenece a otra persona
                throw new IllegalArgumentException("Ya existe una persona con el DNI: " + dni);
            }
            // Si llegamos aquí: es UPDATE y el DNI pertenece a la misma persona → OK
        }
    }
} **/
