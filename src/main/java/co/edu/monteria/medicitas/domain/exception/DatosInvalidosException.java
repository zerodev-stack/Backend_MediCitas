package co.edu.monteria.medicitas.domain.exception;

/**
 * Datos enviados por el usuario que no se pueden aceptar (por ejemplo, la contraseña actual
 * no coincide o el correo ya pertenece a otra cuenta). Se responde 400.
 *
 * POR QUÉ NO 401: un 401 significa "la sesión no es válida" y la app móvil cierra la sesión
 * al recibirlo. Equivocarse al escribir la contraseña actual en "Cambiar contraseña" no
 * debe sacar al paciente de la app.
 */
public class DatosInvalidosException extends RuntimeException {
    public DatosInvalidosException(String message) {
        super(message);
    }
}
