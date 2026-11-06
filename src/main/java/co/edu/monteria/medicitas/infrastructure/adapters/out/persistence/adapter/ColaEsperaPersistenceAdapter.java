package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.ColaEsperaRepositoryPort;
import co.edu.monteria.medicitas.domain.model.ColaEspera;
import co.edu.monteria.medicitas.domain.model.enums.EstadoColaEspera;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.ColaEsperaEntity;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.ColaEsperaJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class ColaEsperaPersistenceAdapter implements ColaEsperaRepositoryPort {

    private final ColaEsperaJpaRepository repository;
    private final DomainEntityMapper mapper;

    public ColaEsperaPersistenceAdapter(ColaEsperaJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ColaEspera guardar(ColaEspera colaEspera) {
        ColaEsperaEntity entity = mapper.toColaEsperaEntity(colaEspera);
        ColaEsperaEntity guardado = repository.save(entity);
        return mapper.toColaEsperaDomain(guardado);
    }

    @Override
    public Optional<ColaEspera> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toColaEsperaDomain);
    }

    @Override
    public List<ColaEspera> buscarPorPacienteId(Long pacienteId) {
        return repository.findByPacienteIdOrderByFechaSolicitudDesc(pacienteId)
                .stream().map(mapper::toColaEsperaDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<ColaEspera> obtenerCabeceraFifoBloqueada(Long especialidadId, Long ipsId, Long servicioId,
                                                             Long sedeId, EstadoColaEspera estado) {
        // Sesión de escritura, no de solo lectura: un SELECT ... FOR UPDATE dentro de
        // una transacción marcada como readOnly puede ser degradado por el proveedor
        // de JPA, y entonces dos reasignaciones simultáneas leerían la misma cabecera.
        return repository.findFifoHeadForUpdate(especialidadId, ipsId, servicioId, sedeId, estado, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(mapper::toColaEsperaDomain);
    }

    @Override
    public List<ColaEspera> buscarEnEspera(Long especialidadId, Long ipsId) {
        return repository.findByEspecialidadIdAndIpsIdAndEstadoOrderByFechaSolicitudAscIdAsc(
                        especialidadId, ipsId, EstadoColaEspera.EN_ESPERA)
                .stream().map(mapper::toColaEsperaDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existeEnEspera(Long pacienteId, Long especialidadId, Long ipsId, EstadoColaEspera estado) {
        return repository.existsByPacienteIdAndEspecialidadIdAndIpsIdAndEstado(pacienteId, especialidadId, ipsId, estado);
    }
}
