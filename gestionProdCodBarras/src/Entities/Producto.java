package Entities;

/**
 * Entidad que representa un productocategoria en el sistema.
 * Hereda de Base para obtener id y eliminado.
 *
 * Relación con CodigoBarras:
 * - Un Producto puede tener 0 o 1 CodigoBarras (relación opcional)
 * - Se relaciona mediante FK codigoBarras en la tabla personas
 *
 * Tabla BD: producto
 * Campos:
 *id INT AUTO_INCREMENT PRIMARY KEY,
 *eliminado bool,
 *nombre varchar(120) NOT NULL,
 *marca varchar(80),
 *categoria varchar(80),
 *precio double(10,2) NOT NULL,
 *peso double(10,3) CHECK(peso>0),
 *codigoBarras int UNIQUE,
 */
public class Producto extends Base {
    
    private String nombre; //NOT NULL
    
    private String marca;
    
    private String categoria;
    
    private double precio; //NOT NULL
    
    private double peso; 

    private CodigoBarras codigoBarras;

    
    public Producto(int id, String nombre, String marca, String categoria, double precio, double peso ) {    
        super(id, false);
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.precio = precio;
        this.peso = peso;
        }

    public Producto() { // Constructor por defecto para crear un producto nuevo sin ID.
        super();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) { //verificar NOT NULL en service
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public CodigoBarras getCodBarras() {
        return codigoBarras;
    }

    /**
     * Asocia o desasocia un codigo de barra al producto.
     * Si codigoBarras es null, la FK codigoBarras será NULL en la BD.
     */
    public void setCodBarras(CodigoBarras codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    @Override
    public String toString() {      //toString
        return "Producto [id=" + getId() +
                ", nombre=" + nombre +
                ", marca=" + marca +
                ", precio=" + precio +
                ", eliminado=" + isEliminado() +
                ", codigoBarras=" + (codigoBarras != null ? codigoBarras.getValor() : "N/A") + 
                "]";
    }


    public double getPrecio() {
        return precio;
    }

    public double getPeso() {
        return peso;
    }

    public void setPrecio(double precio) {  //verificar NOT NULL en service
        this.precio = precio;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }
}