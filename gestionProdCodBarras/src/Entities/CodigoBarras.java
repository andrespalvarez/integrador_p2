package Entities;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa un codigo de barras en el sistema.
 * Hereda de Base para obtener id y eliminado.
 *
 * Relación con Producto:
 * - Un Producto puede tener 0 o 1 Codigo de Barras
 * - Un Codigo de barras puede estar asociado UN UNICO codigo de barras
 *
 * Tabla BD: codigobarras
 * Campos:
 * id INT AUTO_INCREMENT PRIMARY KEY,
 * eliminado bool,
 * tipo enum ('EAN13','EAN8','UPC') NOT NULL,
 * valor varchar(20) NOT NULL UNIQUE,
 * fechaAsignacion LocalDate,
 * observaciones varchar(255) 
 */
public class CodigoBarras extends Base {
    
    private String valor;              // NOT NULL
    private String tipo;               // EAN8, EAN13, UPC
    private LocalDate fechaAsignacion;
    private String observaciones;

    public CodigoBarras(int id, String valor, String tipo, LocalDate fechaAsignacion, String observaciones) {
        super(id, false);
        this.valor = valor;
        this.tipo = tipo;
        this.fechaAsignacion = fechaAsignacion;
        this.observaciones = observaciones;
    }
    
    public CodigoBarras() { // Constructor por defecto para crear un codigo de barras nuevo sin ID.
        super();
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }


    @Override
    public String toString() {
        return "CodigoBarras{" +
                "id=" + getId() +
                ", tipo='" + tipo + '\'' +
                ", valor='" + valor + '\'' +
                ", fechaAsignacion='" + fechaAsignacion + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", eliminado=" + isEliminado() +
                '}';
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setFechaAsignacion(LocalDate fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    
}