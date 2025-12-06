package es.nextdigital.demo.model;

public class Tarjeta {

    private Long id;
    private String numero;
    private boolean activada;
    private TipoTarjeta tipo;
    private Double limiteDiario;
    private Double creditoMaximo;
    private Double creditoDisponible;
    private Long cuentaId;
    private Double retiradoHoy = 0.0;
    private String pinHash;
    private boolean pinPendienteCambio = true;

    public Tarjeta(Long id, String numero, TipoTarjeta tipo, Double limiteDiario,
                   Double creditoMaximo, Long cuentaId) {
        this.id = id;
        this.numero = numero;
        this.activada = false;
        this.tipo = tipo;
        this.limiteDiario = limiteDiario;
        this.creditoMaximo = creditoMaximo;
        this.creditoDisponible = creditoMaximo;
        this.cuentaId = cuentaId;
    }

    public Tarjeta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public boolean getActivada() { return activada; }
    public void setActivada(boolean activada) { this.activada = activada; }

    public TipoTarjeta getTipo() { return tipo; }
    public void setTipo(TipoTarjeta tipo) { this.tipo = tipo; }

    public Double getLimiteDiario() { return limiteDiario; }
    public void setLimiteDiario(Double limiteDiario) { this.limiteDiario = limiteDiario; }

    public Double getCreditoMaximo() { return creditoMaximo; }
    public void setCreditoMaximo(Double creditoMaximo) { this.creditoMaximo = creditoMaximo; }

    public Double getCreditoDisponible() { return creditoDisponible; }
    public void setCreditoDisponible(Double creditoDisponible) { this.creditoDisponible = creditoDisponible; }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public Double getRetiradoHoy() { return retiradoHoy; }
    public void setRetiradoHoy(Double retiradoHoy) { this.retiradoHoy = retiradoHoy; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public boolean getPinPendienteCambio() { return pinPendienteCambio; }
    public void setPinPendienteCambio(boolean pinPendienteCambio) { this.pinPendienteCambio = pinPendienteCambio; }
}
