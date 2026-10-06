package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.Especialidad;

public record EspecialidadResponse(Long id, String nombre, String descripcion) {
    public static EspecialidadResponse desde(Especialidad especialidad) {
        if (especialidad == null) {
            return null;
        }
        return new EspecialidadResponse(
                especialidad.getId(),
                especialidad.getNombre(),
                especialidad.getDescripcion()
        );
    }
}
