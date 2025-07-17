package es.nextdigital.demo.exception;

public class TarjetaNoActivadaException extends RuntimeException {
    public TarjetaNoActivadaException(String numero) {
        super("Tarjeta " + numero + " no está activada");
    }
}
