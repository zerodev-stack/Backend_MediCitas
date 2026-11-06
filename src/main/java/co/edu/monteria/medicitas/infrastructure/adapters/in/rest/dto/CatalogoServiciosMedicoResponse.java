package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import java.util.List;

/**
 * Lo que necesita la pantalla "Configurar servicios y sedes": los servicios de la
 * especialidad del médico, las sedes de su institución y lo que ya tiene habilitado.
 */
public record CatalogoServiciosMedicoResponse(
        List<ServicioResponse> servicios,
        List<SedeResponse> sedes,
        List<ServicioMedicoResponse> configurados
) {}
