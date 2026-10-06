package co.edu.monteria.medicitas.domain.exception;

public class AfiliacionNoValidaException extends RuntimeException {
    private final String mensajeOrientacion;

    public AfiliacionNoValidaException(String message, String mensajeOrientacion) {
        super(message);
        this.mensajeOrientacion = mensajeOrientacion;
    }

    public String getMensajeOrientacion() {
        return mensajeOrientacion;
    }
}
