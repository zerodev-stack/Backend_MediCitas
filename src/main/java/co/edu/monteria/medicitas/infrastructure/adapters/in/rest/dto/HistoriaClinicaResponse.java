package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.HistoriaClinica;
import co.edu.monteria.medicitas.domain.model.Ips;

import java.time.LocalDateTime;

public record HistoriaClinicaResponse(
        Long id,
        UsuarioResponse paciente,
        Ips ipsOrigen,
        String diagnostico,
        String tratamiento,
        String contenido,
        LocalDateTime fechaRegistro
) {
    public static HistoriaClinicaResponse desde(HistoriaClinica historia) {
        if (historia == null) {
            return null;
        }
        return new HistoriaClinicaResponse(
                historia.getId(),
                UsuarioResponse.desde(historia.getPaciente()),
                historia.getIpsOrigen(),
                historia.getDiagnostico(),
                historia.getTratamiento(),
                historia.getContenido(),
                historia.getFechaRegistro()
        );
    }
}
