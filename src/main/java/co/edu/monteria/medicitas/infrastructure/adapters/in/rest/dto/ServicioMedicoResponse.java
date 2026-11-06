package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.ServicioMedico;

public record ServicioMedicoResponse(Long id, ServicioResponse servicio, SedeResponse sede) {
    public static ServicioMedicoResponse desde(ServicioMedico servicioMedico) {
        if (servicioMedico == null) {
            return null;
        }
        return new ServicioMedicoResponse(
                servicioMedico.getId(),
                ServicioResponse.desde(servicioMedico.getServicio()),
                SedeResponse.desde(servicioMedico.getSede())
        );
    }
}
