package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.Medico;

/**
 * Respuesta del perfil profesional de un médico.
 *
 * Incluye el usuario titular porque la agenda del paciente necesita mostrar el nombre del
 * profesional que atiende, pero se construye desde 'UsuarioResponse' y por tanto nunca
 * expone el hash de la contraseña.
 */
public record MedicoResponse(
        Long id,
        UsuarioResponse usuario,
        EspecialidadResponse especialidad,
        Ips ips,
        String registroMedico
) {
    public static MedicoResponse desde(Medico medico) {
        if (medico == null) {
            return null;
        }
        return new MedicoResponse(
                medico.getId(),
                UsuarioResponse.desde(medico.getUsuario()),
                EspecialidadResponse.desde(medico.getEspecialidad()),
                medico.getIps(),
                medico.getRegistroMedico()
        );
    }
}
