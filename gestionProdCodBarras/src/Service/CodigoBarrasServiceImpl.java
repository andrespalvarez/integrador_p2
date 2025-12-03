package Service;

import java.util.List;
import Dao.GenericDAO;
import Entities.CodigoBarras;

/**
 * Implementación del servicio de negocio para la entidad Domicilio.
 * Capa intermedia entre la UI y el DAO que aplica validaciones de negocio.
 *
 * Responsabilidades:
 * - Validar que los datos del domicilio sean correctos ANTES de persistir
 * - Aplicar reglas de negocio (RN-023: calle y número obligatorios)
 * - Delegar operaciones de BD al DAO
 * - Transformar excepciones técnicas en errores de negocio comprensibles
 *
 * Patrón: Service Layer con inyección de dependencias
 */
public class CodigoBarrasServiceImpl implements GenericService<CodigoBarras> {
    /**
     * DAO para acceso a datos de domicilios.
     * Inyectado en el constructor (Dependency Injection).
     * Usa GenericDAO para permitir testing con mocks.
     */
    private final GenericDAO<CodigoBarras> codigoBarrasDAO;

    /**
     * Constructor con inyección de dependencias.
     * Valida que el DAO no sea null (fail-fast).
     *
     * @param codigoBarrasDAO DAO de domicilios (normalmente DomicilioDAO)
     * @throws IllegalArgumentException si codigoBarrasDAO es null
     */
    public CodigoBarrasServiceImpl(GenericDAO<CodigoBarras> codigoBarrasDAO) {
        if (codigoBarrasDAO == null) {
            throw new IllegalArgumentException("CodigoBarrasDAO no puede ser null");
        }
        this.codigoBarrasDAO = codigoBarrasDAO;
    }

    /**
     * Inserta un nuevo domicilio en la base de datos.
     *
     * Flujo:
     * 1. Valida que calle y número no estén vacíos
     * 2. Delega al DAO para insertar
     * 3. El DAO asigna el ID autogenerado al objeto domicilio
     *
     * @param codigoBarras CodigoBarras a insertar (id será ignorado y regenerado)
     * @throws Exception Si la validación falla o hay error de BD
     */
    @Override
    public void insertar(CodigoBarras codigoBarras) throws Exception {
        validateCodigoBarras(codigoBarras);
        codigoBarrasDAO.insertar(codigoBarras);
    }

    /**
     * Actualiza un domicilio existente en la base de datos.
     *
     * Validaciones:
     * - El domicilio debe tener datos válidos (calle, número)
     * - El ID debe ser > 0 (debe ser un domicilio ya persistido)
     *
     * IMPORTANTE: Si varias personas comparten este domicilio,
     * la actualización los afectará a TODAS (RN-040).
     *
     * @param domicilio CodigoBarras con los datos actualizados
     * @throws Exception Si la validación falla o el domicilio no existe
     */
    @Override
    public void actualizar(CodigoBarras codigoBarras) throws Exception {
        validateCodigoBarras(codigoBarras);
        if (codigoBarras.getId() <= 0) {
            throw new IllegalArgumentException("El ID del Código de Barras debe ser mayor a 0 para actualizar");
        }
        codigoBarrasDAO.actualizar(codigoBarras);
    }

    /**
     * Elimina lógicamente un domicilio (soft delete).
     * Marca el domicilio como eliminado=TRUE sin borrarlo físicamente.
     *
     * ⚠️ ADVERTENCIA: Este método NO verifica si hay personas asociadas.
     * Puede dejar referencias huérfanas en personas.domicilio_id (RN-029).
     *
     * ALTERNATIVA SEGURA: Usar PersonaServiceImpl.eliminarDomicilioDePersona()
     * que actualiza la FK antes de eliminar (opción 10 del menú).
     *
     * @param id ID del domicilio a eliminar
     * @throws Exception Si id <= 0 o no existe el domicilio
     */
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        codigoBarrasDAO.eliminar(id);
    }

    /**
     * Obtiene un domicilio por su ID.
     *
     * @param id ID del domicilio a buscar
     * @return CodigoBarras encontrado, o null si no existe o está eliminado
     * @throws Exception Si id <= 0 o hay error de BD
     */
    @Override
    public CodigoBarras getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return codigoBarrasDAO.getById(id);
    }

    /**
     * Obtiene todos los domicilios activos (eliminado=FALSE).
     *
     * @return Lista de domicilios activos (puede estar vacía)
     * @throws Exception Si hay error de BD
     */
    @Override
    public List<CodigoBarras> getAll() throws Exception {
        return codigoBarrasDAO.getAll();
    }

    /**
     * Valida que un domicilio tenga datos correctos.
     *
     * Reglas de negocio aplicadas:
     * - RN-023: Calle y número son obligatorios
     * - RN-024: Se verifica trim() para evitar strings solo con espacios
     *
     * @param domicilio CodigoBarras a validar
     * @throws IllegalArgumentException Si alguna validación falla
     */
    private void validateCodigoBarras(CodigoBarras codigoBarras) {
        if (codigoBarras == null) {
            throw new IllegalArgumentException("El Código de Barras no puede ser null");
        }
        // Validar campos obligatorios: tipo y valor [cite: 131]
        if (codigoBarras.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de Código de Barras es obligatorio");
        }
        if (codigoBarras.getValor() == null || codigoBarras.getValor().trim().isEmpty()) {
            throw new IllegalArgumentException("El valor del Código de Barras es obligatorio");
        }
    }
}