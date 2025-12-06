package es.nextdigital.demo.model;

import java.util.ArrayList;
import java.util.List;

public class Cuenta {

    private Long id;
    private String iban;
    private Double saldo;
    private List<Movimiento> movimientos = new ArrayList<>();

    public Cuenta(Long id, String iban, Double saldo) {
        this.id = id;
        this.iban = iban;
        this.saldo = saldo;
    }

    public Cuenta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public Double getSaldo() { return saldo; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }

    public List<Movimiento> getMovimientos() { return movimientos; }
    public void setMovimientos(List<Movimiento> movimientos) { this.movimientos = movimientos; }
}
