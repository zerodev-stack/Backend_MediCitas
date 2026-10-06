package co.edu.monteria.medicitas.domain.exception;

/**
 * Credenciales inválidas en el inicio de sesión.
 *
 * Existe como tipo propio para poder responder 401 Unauthorized con un único mensaje
 * genérico. Responder 404 ("correo no registrado") o 422 ("contraseña incorrecta")
 * revelaría qué correos están dados de alta en el sistema.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String message) {
        super(message);
    }
}
