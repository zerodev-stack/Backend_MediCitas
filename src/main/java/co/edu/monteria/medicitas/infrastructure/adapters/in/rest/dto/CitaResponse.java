package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.Cita;
import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.enums.EstadoCita;
import co.edu.monteria.medicitas.domain.model.enums.TipoCita;

import java.time.LocalDateTime;

public record CitaResponse(
        Long id,
        UsuarioResponse paciente,
        DisponibilidadResponse disponibilidad,
        Ips ips,
        TipoCita tipo,
        EstadoCita estado,
        String observaciones,
        LocalDateTime fechaCreacion
) {
    public static CitaResponse desde(Cita cita) {
        if (cita == null) {
            return null;
        }
        return new CitaResponse(
                cita.getId(),
                UsuarioResponse.desde(cita.getPaciente()),
                DisponibilidadResponse.desde(cita.getDisponibilidad()),
                cita.getIps(),
                cita.getTipo(),
                cita.getEstado(),
                cita.getObservaciones(),
                cita.getFechaCreacion()
        );
    }
}
