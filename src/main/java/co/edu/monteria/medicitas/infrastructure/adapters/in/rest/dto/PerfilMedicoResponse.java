package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.ServicioMedico;

import java.util.List;

/**
 * Perfil completo del médico autenticado, con los servicios y sedes que tiene habilitados.
 */
public record PerfilMedicoResponse(
        Long id,
        UsuarioResponse usuario,
        EspecialidadResponse especialidad,
        Ips ips,
        String registroMedico,
        String ciudadAtencion,
        String correoContacto,
        String telefonoContacto,
        List<ServicioMedicoResponse> servicios
) {
    public static PerfilMedicoResponse desde(Medico medico, List<ServicioMedico> servicios) {
        return new PerfilMedicoResponse(
                medico.getId(),
                UsuarioResponse.desde(medico.getUsuario()),
                EspecialidadResponse.desde(medico.getEspecialidad()),
                medico.getIps(),
                medico.getRegistroMedico(),
                medico.getCiudadAtencion(),
                medico.getCorreoContacto(),
                medico.getTelefonoContacto(),
                servicios.stream().map(ServicioMedicoResponse::desde).toList()
        );
    }
}
