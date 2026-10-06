package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.AutorizacionAcceso;
import co.edu.monteria.medicitas.domain.model.enums.EstadoAutorizacion;

import java.util.List;
import java.util.Optional;

public interface AutorizacionAccesoRepositoryPort {
    AutorizacionAcceso guardar(AutorizacionAcceso autorizacion);
    Optional<AutorizacionAcceso> buscarPorId(Long id);
    Optional<AutorizacionAcceso> buscarPorHistoriaEIpsSolicitante(Long historiaId, Long ipsSolicitanteId);
    List<AutorizacionAcceso> buscarPorPacienteId(Long pacienteId);
}
