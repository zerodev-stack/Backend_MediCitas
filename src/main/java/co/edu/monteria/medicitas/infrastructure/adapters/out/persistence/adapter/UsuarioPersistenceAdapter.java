package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.adapter;

import co.edu.monteria.medicitas.application.ports.out.UsuarioRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.UsuarioEntity;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper.DomainEntityMapper;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repository;
    private final DomainEntityMapper mapper;

    public UsuarioPersistenceAdapter(UsuarioJpaRepository repository, DomainEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entity = mapper.toUsuarioEntity(usuario);
        UsuarioEntity guardado = repository.save(entity);
        return mapper.toUsuarioDomain(guardado);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toUsuarioDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(mapper::toUsuarioDomain);
    }

    @Override
    public Optional<Usuario> buscarPorNumDocumento(String numDocumento) {
        return repository.findByNumDocumento(numDocumento).map(mapper::toUsuarioDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existePorNumDocumento(String numDocumento) {
        return repository.existsByNumDocumento(numDocumento);
    }
}
