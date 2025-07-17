package es.nextdigital.demo.exception;


public class TarjetaNoEncontradaException extends RuntimeException {
    public TarjetaNoEncontradaException(String numero) {
        super("Tarjeta no encontrada: " + numero);
    }
}
