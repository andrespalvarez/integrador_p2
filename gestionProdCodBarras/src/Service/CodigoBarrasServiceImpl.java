package Service;

import java.util.List;
import Dao.GenericDAO;
import Entities.CodigoBarras;

/**
 * Servicio de negocio para CodigoBarras (Clase B).
 * Compatible con ProductoServiceImpl.
 */
public class CodigoBarrasServiceImpl implements GenericService<CodigoBarras> {

    private final GenericDAO<CodigoBarras> codigoBarrasDAO;

    public CodigoBarrasServiceImpl(GenericDAO<CodigoBarras> codigoBarrasDAO) {
        if (codigoBarrasDAO == null) {
            throw new IllegalArgumentException("CodigoBarrasDAO no puede ser null");
        }
        this.codigoBarrasDAO = codigoBarrasDAO;
    }

    // -----------------------------------------------------
    // INSERTAR (con validaciones)
    // -----------------------------------------------------
    @Override
    public void insertar(CodigoBarras codigoBarras) throws Exception {
        validateCodigoBarras(codigoBarras);
        codigoBarrasDAO.insertar(codigoBarras);  // genera ID autoincrement
    }

    // -----------------------------------------------------
    // ACTUALIZAR
    // -----------------------------------------------------
    @Override
    public void actualizar(CodigoBarras codigoBarras) throws Exception {
        validateCodigoBarras(codigoBarras);
        if (codigoBarras.getId() <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0 para actualizar");
        }
        codigoBarrasDAO.actualizar(codigoBarras);
    }

    // -----------------------------------------------------
    // ELIMINAR (soft delete)
    // -----------------------------------------------------
    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        codigoBarrasDAO.eliminar(id);
    }

    // -----------------------------------------------------
    // GET BY ID
    // -----------------------------------------------------
    @Override
    public CodigoBarras getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return codigoBarrasDAO.getById(id);
    }

    // -----------------------------------------------------
    // GET ALL
    // -----------------------------------------------------
    @Override
    public List<CodigoBarras> getAll() throws Exception {
        return codigoBarrasDAO.getAll();
    }

    // -----------------------------------------------------
    // BÚSQUEDA ESPECIALIZADA
    // -----------------------------------------------------
    public CodigoBarras buscarPorValor(String valor) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El valor no puede estar vacío");
        }
        // Debe existir un método en tu DAO
        // return ((CodigoBarrasDAO) codigoBarrasDAO).buscarPorValor(valor);
        return null;
    }

    // -----------------------------------------------------
    // VALIDACIONES
    // -----------------------------------------------------
    private void validateCodigoBarras(CodigoBarras codigoBarras) {
        if (codigoBarras == null) {
            throw new IllegalArgumentException("El Código de Barras no puede ser null");
        }
        
        // Validación del Tipo: Debe ser NO nulo y NO vacío/solo espacios.
        if (codigoBarras.getTipo() == null || codigoBarras.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de Código de Barras es obligatorio y no puede estar vacío.");
        }
        
        // Validación de Valores Permitidos para Tipo 
        String tipoIngresado = codigoBarras.getTipo().toUpperCase();
        if (!tipoIngresado.equals("EAN8") && !tipoIngresado.equals("EAN13") && !tipoIngresado.equals("UPC")) {
             throw new IllegalArgumentException("El tipo de Código de Barras debe ser uno de los siguientes: EAN8, EAN13 o UPC.");
        }

        // Validación del Valor: Debe ser NO nulo y NO vacío/solo espacios.
        if (codigoBarras.getValor() == null || codigoBarras.getValor().trim().isEmpty()) {
            throw new IllegalArgumentException("El valor del Código de Barras es obligatorio y no puede estar vacío.");
        }
    }
}
