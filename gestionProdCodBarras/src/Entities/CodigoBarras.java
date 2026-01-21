package Entities;

import java.time.LocalDate;

public class CodigoBarras extends Base {

    private String valor;
    private String tipo;
    private LocalDate fechaAsignacion;
    private String observaciones;

    public CodigoBarras() {
        super();
    }

    public CodigoBarras(int id, String valor, String tipo, LocalDate fechaAsignacion, String observaciones) {
        super(id, false);
        this.valor = valor;
        this.tipo = tipo;
        this.fechaAsignacion = fechaAsignacion;
        this.observaciones = observaciones;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDate fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "CodigoBarras [id=" + getId()
                + ", tipo=" + tipo
                + ", valor=" + valor
                + ", fechaAsignacion=" + fechaAsignacion
                + ", observaciones=" + observaciones
                + ", eliminado=" + isEliminado() + "]";
    }
}
