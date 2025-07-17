package es.nextdigital.demo.exception;

public class PinInvalidoException extends RuntimeException {
    public PinInvalidoException() {
        super("PIN incorrecto");
    }
}
