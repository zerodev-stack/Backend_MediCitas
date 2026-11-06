package co.edu.monteria.medicitas.domain.exception;

/**
 * El médico intentó publicar un bloque que se superpone con otro horario suyo. Es una
 * regla de negocio, pero se distingue para que la app muestre la pantalla de "Cruce de
 * horarios" en lugar de un error genérico.
 */
public class CruceHorarioException extends ReglaNegocioException {
    public CruceHorarioException(String message) {
        super(message);
    }
}
