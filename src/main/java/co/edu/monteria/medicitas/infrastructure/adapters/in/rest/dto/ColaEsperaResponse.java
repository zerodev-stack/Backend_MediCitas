package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.ColaEspera;
import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.enums.EstadoColaEspera;

import java.time.LocalDateTime;

public record ColaEsperaResponse(
        Long id,
        UsuarioResponse paciente,
        EspecialidadResponse especialidad,
        Ips ips,
        LocalDateTime fechaSolicitud,
        EstadoColaEspera estado,
        ServicioResponse servicio,
        SedeResponse sede
) {
    public static ColaEsperaResponse desde(ColaEspera cola) {
        if (cola == null) {
            return null;
        }
        return new ColaEsperaResponse(
                cola.getId(),
                UsuarioResponse.desde(cola.getPaciente()),
                EspecialidadResponse.desde(cola.getEspecialidad()),
                cola.getIps(),
                cola.getFechaSolicitud(),
                cola.getEstado(),
                ServicioResponse.desde(cola.getServicio()),
                SedeResponse.desde(cola.getSede())
        );
    }
}
