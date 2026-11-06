package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.ServicioMedicoRepositoryPort;
import co.edu.monteria.medicitas.domain.model.ServicioMedico;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.ServicioMedicoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class ServicioMedicoPersistenceAdapter implements ServicioMedicoRepositoryPort {

    private final ServicioMedicoJpaRepository repository;
    private final DomainEntityMapper mapper;

    public ServicioMedicoPersistenceAdapter(ServicioMedicoJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<ServicioMedico> buscarPorMedicoId(Long medicoId) {
        return repository.findByMedicoIdOrderByServicioIdAscSedeIdAsc(medicoId).stream()
                .map(mapper::toServicioMedicoDomain).toList();
    }

    @Override
    public boolean existe(Long medicoId, Long servicioId, Long sedeId) {
        return repository.existsByMedicoIdAndServicioIdAndSedeId(medicoId, servicioId, sedeId);
    }

    @Override
    @Transactional
    public List<ServicioMedico> reemplazarConfiguracion(Long medicoId, List<ServicioMedico> nuevaConfiguracion) {
        repository.eliminarPorMedicoId(medicoId);
        repository.saveAll(nuevaConfiguracion.stream().map(mapper::toServicioMedicoEntity).toList());
        repository.flush();
        return buscarPorMedicoId(medicoId);
    }
}
