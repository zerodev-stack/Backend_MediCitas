package co.edu.monteria.medicitas.domain.exception;

/**
 * Se lanza cuando un usuario autenticado intenta operar sobre un recurso que no le pertenece,
 * o sobre una operación que su rol no tiene permitidos.
 *
 * A diferencia de 'AccesoDenegadoHistoriaException' (que responde a la reserva legal de la
 * historia clínica), esta excepción cubre la autorización de recursos en general.
 */
public class AccesoNoAutorizadoException extends RuntimeException {

    public AccesoNoAutorizadoException(String message) {
        super(message);
    }
}
