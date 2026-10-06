package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.ResultadoAfiliacion;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.TipoDocumento;

public interface AutenticacionUseCase {

    /**
     * Verifica las credenciales y devuelve el usuario autenticado.
     * Lanza CredencialesInvalidasException con un mensaje genérico si el correo no existe
     * o la contraseña no coincide, para no revelar qué cuentas están registradas.
     */
    Usuario autenticar(String email, String password);

    /**
     * Genera el token JWT de la sesión a partir de un usuario ya verificado.
     */
    String generarToken(Usuario usuario);

    /**
     * Valida previamente si la afiliación BDUA del paciente es válida en la EPS (ej. Mutualser).
     */
    ResultadoAfiliacion validarAfiliacionBdua(TipoDocumento tipoDocumento, String numDocumento);

    /**
     * Registra un paciente en la plataforma. Primero valida la afiliación en BDUA; si la EPS no es soportada
     * en Montería, lanza una AfiliacionNoValidaException con mensaje de orientación.
     */
    Usuario registrarPaciente(String numDocumento, TipoDocumento tipoDocumento, String nombreCompleto,
                              String email, String password, String telefono);

    /**
     * Registra un usuario médico en la plataforma adscrito a una especialidad e IPS.
     */
    Usuario registrarMedico(String numDocumento, TipoDocumento tipoDocumento, String nombreCompleto,
                            String email, String password, String telefono, Long especialidadId,
                            Long ipsId, String registroMedico);
}
