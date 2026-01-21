package Entities;

public class Producto extends Base {

    private String nombre;
    private String marca;
    private String categoria;
    private double precio;
    private double peso;
    private CodigoBarras codigoBarras;

    public Producto() {
        super();
    }

    public Producto(int id, String nombre, String marca, String categoria, double precio, double peso) {
        super(id, false);
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.precio = precio;
        this.peso = peso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
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

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public CodigoBarras getCodBarras() {
        return codigoBarras;
    }

    public void setCodBarras(CodigoBarras codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    @Override
    public String toString() {
        return "Producto [id=" + getId()
                + ", nombre=" + nombre
                + ", marca=" + marca
                + ", precio=" + precio
                + ", eliminado=" + isEliminado()
                + ", codigoBarras=" + (codigoBarras != null ? codigoBarras.getValor() : "N/A")
                + "]";
    }
}
