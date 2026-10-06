package co.edu.monteria.medicitas.domain.exception;

/**
 * Se lanza cuando dos transacciones intentan reservar el mismo recurso (cupo de agenda o
 * plaza en la cola de espera) y la base de datos detecta el conflicto.
 *
 * La aplicación la traduce a HTTP 409 Conflict para que el cliente pueda reintentar
 * sin interpretar el fallo como un error del servidor.
 */
public class ConflictoConcurrenciaException extends RuntimeException {

    public ConflictoConcurrenciaException(String message) {
        super(message);
    }

    public ConflictoConcurrenciaException(String message, Throwable causa) {
        super(message, causa);
    }
}
