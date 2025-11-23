package entities;

import java.time.LocalDate;

/**
 * Representa un Código de Barras asociado a un Producto (relación 1:1).
 * Producto -> CodigoBarras (unidireccional desde Producto)
 */
public class CodigoBarras {

    private Long id;
    private String valor;              // NOT NULL
    private String tipo;               // EAN8, EAN13, UPC
    private LocalDate fechaCreacion;
    private String observaciones;

    // ------------------------------
    // Constructor vacío
    // ------------------------------
    public CodigoBarras() {}

    // ------------------------------
    // Constructor completo
    // ------------------------------
    public CodigoBarras(Long id, String valor, String tipo,
                        LocalDate fechaCreacion, String observaciones) {
        this.id = id;
        this.valor = valor;
        this.tipo = tipo;
        this.fechaCreacion = fechaCreacion;
        this.observaciones = observaciones;
    }

    // ------------------------------
    // Constructor sin ID (para INSERT)
    // ------------------------------
    public CodigoBarras(String valor, String tipo,
                        LocalDate fechaCreacion, String observaciones) {
        this.valor = valor;
        this.tipo = tipo;
        this.fechaCreacion = fechaCreacion;
        this.observaciones = observaciones;
    }

    // ------------------------------
    // Getters y Setters
    // ------------------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // ------------------------------
    // toString()
    // ------------------------------
    @Override
    public String toString() {
        return "CodigoBarras [id=" + id +
               ", valor=" + valor +
               ", tipo=" + tipo +
               ", fechaCreacion=" + fechaCreacion +
               "]";
    }
}
