package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.ServicioMedico;

import java.util.List;

public interface ServicioMedicoRepositoryPort {
    List<ServicioMedico> buscarPorMedicoId(Long medicoId);
    boolean existe(Long medicoId, Long servicioId, Long sedeId);
    /**
     * Sustituye por completo la configuración del médico. Se hace en una sola operación
     * para que la app no quede con una configuración a medias si una inserción falla.
     */
    List<ServicioMedico> reemplazarConfiguracion(Long medicoId, List<ServicioMedico> nuevaConfiguracion);
}
