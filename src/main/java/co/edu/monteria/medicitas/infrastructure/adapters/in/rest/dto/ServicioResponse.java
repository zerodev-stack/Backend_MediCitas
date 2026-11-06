package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.Servicio;
import co.edu.monteria.medicitas.domain.model.enums.TipoServicio;

/**
 * Servicio del catálogo. Incluye la etiqueta legible del tipo para que la app no tenga que
 * mantener su propia traducción de los valores del enum.
 */
public record ServicioResponse(Long id, String nombre, TipoServicio tipo, String tipoEtiqueta) {
    public static ServicioResponse desde(Servicio servicio) {
        if (servicio == null) {
            return null;
        }
        return new ServicioResponse(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getTipo(),
                servicio.getTipo() != null ? servicio.getTipo().getEtiqueta() : null
        );
    }
}
