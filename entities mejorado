package entities;

/**
 * Capa entities
 * Su propósito es modelar los objetos del dominio del negocio.
 * Esta clase representa un Producto y utiliza baja lógica mediante el atributo "eliminado".
 * 
 * Relación 1→1 unidireccional: Producto -> CodigoBarras
 * (El producto conoce su código, pero el código no referencia al producto)
 */
public class Producto {

    // Atributo que mapea la clave primaria BIGINT
    private Long id;

    // Baja lógica: true = dado de baja / false = activo
    private Boolean eliminado;

    private String nombre;
    private String marca;
    private String categoria;
    private Double precio; // NOT NULL
    private Double peso;

    // Relación 1→1 unidireccional
    private CodigoBarras codigoBarras;


    // ------------------------------
    // Constructor vacío
    // ------------------------------
    public Producto() {}


    // ------------------------------
    // Constructor completo (incluye ID)
    // Usado al recuperar desde la BD
    // ------------------------------
    public Producto(Long id, Boolean eliminado, String nombre, String marca,
                    String categoria, Double precio, Double peso,
                    CodigoBarras codigoBarras) {

        this.id = id;
        this.eliminado = eliminado;
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.precio = precio;
        this.peso = peso;
        this.codigoBarras = codigoBarras;
    }


    // ------------------------------
    // Constructor para INSERT (sin ID)
    // ------------------------------
    public Producto(Boolean eliminado, String nombre, String marca,
                    String categoria, Double precio, Double peso,
                    CodigoBarras codigoBarras) {

        this.eliminado = eliminado;
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.precio = precio;
        this.peso = peso;
        this.codigoBarras = codigoBarras;
    }


    // ------------------------------
    // Getters y Setters
    // ------------------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Boolean getEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public CodigoBarras getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(CodigoBarras codigoBarras) { this.codigoBarras = codigoBarras; }


    // ------------------------------
    // toString()
    // ------------------------------
    @Override
    public String toString() {
        return "Producto [id=" + id +
                ", nombre=" + nombre +
                ", marca=" + marca +
                ", precio=" + precio +
                ", eliminado=" + eliminado +
                ", codigoBarras=" + (codigoBarras != null ? codigoBarras.getValor() : "N/A") +
                "]";
    }
}
