package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;
import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cupo de atención. 'bloqueId', 'servicio' y 'sede' son null en las franjas sueltas; en
 * los cupos generados por un bloque le dicen al paciente qué atención reserva y dónde.
 */
public record DisponibilidadResponse(
        Long id,
        MedicoResponse medico,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoDisponibilidad estado,
        Long bloqueId,
        ServicioResponse servicio,
        SedeResponse sede
) {
    public static DisponibilidadResponse desde(DisponibilidadMedico disponibilidad) {
        if (disponibilidad == null) {
            return null;
        }
        var bloque = disponibilidad.getBloque();
        return new DisponibilidadResponse(
                disponibilidad.getId(),
                MedicoResponse.desde(disponibilidad.getMedico()),
                disponibilidad.getFecha(),
                disponibilidad.getHoraInicio(),
                disponibilidad.getHoraFin(),
                disponibilidad.getEstado(),
                bloque != null ? bloque.getId() : null,
                bloque != null ? ServicioResponse.desde(bloque.getServicio()) : null,
                bloque != null ? SedeResponse.desde(bloque.getSede()) : null
        );
    }
}
