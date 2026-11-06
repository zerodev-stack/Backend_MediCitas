package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Configuración completa de "Mis servicios y sedes": la lista reemplaza a la anterior.
 */
public record ConfigurarServiciosRequest(
        @NotNull(message = "La lista de servicios es obligatoria") List<@Valid ServicioConSedes> servicios
) {
    public record ServicioConSedes(
            @NotNull(message = "El servicio es obligatorio") Long servicioId,
            List<Long> sedeIds
    ) {}
}
