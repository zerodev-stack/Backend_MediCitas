package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;
import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;

import java.time.LocalDate;
import java.time.LocalTime;

public record DisponibilidadResponse(
        Long id,
        MedicoResponse medico,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoDisponibilidad estado
) {
    public static DisponibilidadResponse desde(DisponibilidadMedico disponibilidad) {
        if (disponibilidad == null) {
            return null;
        }
        return new DisponibilidadResponse(
                disponibilidad.getId(),
                MedicoResponse.desde(disponibilidad.getMedico()),
                disponibilidad.getFecha(),
                disponibilidad.getHoraInicio(),
                disponibilidad.getHoraFin(),
                disponibilidad.getEstado()
        );
    }
}
