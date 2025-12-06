package prog2int.Service;

import java.util.List;
import prog2int.Dao.GenericDAO;
import prog2int.Models.CodigoBarras;

/**
 * Servicio de negocio para la entidad CodigoBarras (Clase B).
 * Aplica validaciones de dominio y delega la persistencia al DAO.
 */
public class CodigoBarrasService implements GenericService<CodigoBarras> {
    
    private final GenericDAO<CodigoBarras> codigoBarrasDAO;
    
    public CodigoBarrasService(GenericDAO<CodigoBarras> codigoBarrasDAO) {
        if (codigoBarrasDAO == null) {
            throw new IllegalArgumentException("CodigoBarrasDAO no puede ser null");
        }
        this.codigoBarrasDAO = codigoBarrasDAO;
    }

    /**
     * Inserta un nuevo Código de Barras en la base de datos.
     * La validación de unicidad del 'valor' debe ser manejada
     * por el DAO o la restricción UNIQUE de la BD[cite: 131].
     */
    @Override
    public void insertar(CodigoBarras codigoBarras) throws Exception {
        validateCodigoBarras(codigoBarras);
        // Delega al DAO, que debe manejar la obtención del ID autogenerado.
        codigoBarrasDAO.insertar(codigoBarras);
    }

    /**
     * Actualiza un Código de Barras existente.
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
     * Elimina lógicamente un Código de Barras.
     * NOTA: La eliminación real del objeto B debe ser coordinada por el Servicio A 
     * o la base de datos mediante ON DELETE CASCADE en la FK única[cite: 17].
     */
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        codigoBarrasDAO.eliminar(id);
    }

    // Métodos CRUD restantes (getById y getAll)

    @Override
    public CodigoBarras getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return codigoBarrasDAO.getById(id);
    }

    @Override
    public List<CodigoBarras> getAll() throws Exception {
        return codigoBarrasDAO.getAll();
    }
    
    // Método de búsqueda especializada (por ejemplo, por valor)
    public CodigoBarras buscarPorValor(String valor) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El valor del Código de Barras no puede ser vacío");
        }
        // Asumir que existe un método en el DAO para esta búsqueda
        // return ((CodigoBarrasDAO) codigoBarrasDAO).buscarPorValor(valor); 
        return null; // Implementación real requiere un método en CodigoBarrasDAO
    }

    // --- Validaciones de Reglas de Negocio/Dominio ---

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
