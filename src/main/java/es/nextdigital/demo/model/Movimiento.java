package es.nextdigital.demo.model;

import java.time.LocalDateTime;

public class Movimiento {

    private Long id;
    private LocalDateTime fecha;
    private TipoMovimiento tipo;
    private Double cantidad;
    private String descripcion;
    private Long cuentaId;

    public Movimiento(Long id, LocalDateTime fecha, TipoMovimiento tipo, Double cantidad, String descripcion, Long cuentaId) {
        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.descripcion = descripcion;
        this.cuentaId = cuentaId;
    }

    public Movimiento() {}

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
}
