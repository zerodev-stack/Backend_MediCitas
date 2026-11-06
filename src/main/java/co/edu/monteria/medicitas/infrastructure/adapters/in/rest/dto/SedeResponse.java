package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.Sede;

public record SedeResponse(Long id, String nombre, String direccion, Long ipsId, String ipsNombre, String ciudad) {
    public static SedeResponse desde(Sede sede) {
        if (sede == null) {
            return null;
        }
        return new SedeResponse(
                sede.getId(),
                sede.getNombre(),
                sede.getDireccion(),
                sede.getIps() != null ? sede.getIps().getId() : null,
                sede.getIps() != null ? sede.getIps().getNombre() : null,
                sede.getIps() != null ? sede.getIps().getCiudad() : null
        );
    }
}
