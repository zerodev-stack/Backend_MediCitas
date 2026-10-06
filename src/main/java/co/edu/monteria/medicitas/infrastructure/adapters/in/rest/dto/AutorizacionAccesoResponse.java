package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.AutorizacionAcceso;
import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.enums.EstadoAutorizacion;

import java.time.LocalDateTime;

public record AutorizacionAccesoResponse(
        Long id,
        HistoriaClinicaResponse historia,
        Ips ipsSolicitante,
        EstadoAutorizacion estado,
        LocalDateTime fechaAutorizacion,
        LocalDateTime fechaRevocacion
) {
    public static AutorizacionAccesoResponse desde(AutorizacionAcceso autorizacion) {
        if (autorizacion == null) {
            return null;
        }
        return new AutorizacionAccesoResponse(
                autorizacion.getId(),
                HistoriaClinicaResponse.desde(autorizacion.getHistoria()),
                autorizacion.getIpsSolicitante(),
                autorizacion.getEstado(),
                autorizacion.getFechaAutorizacion(),
                autorizacion.getFechaRevocacion()
        );
    }
}
