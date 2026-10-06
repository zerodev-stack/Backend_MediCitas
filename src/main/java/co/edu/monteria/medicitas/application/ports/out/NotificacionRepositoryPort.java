package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.Notificacion;
import java.util.List;
import java.util.Optional;

public interface NotificacionRepositoryPort {
    Notificacion guardar(Notificacion notificacion);
    Optional<Notificacion> buscarPorId(Long id);
    List<Notificacion> buscarPorUsuarioId(Long usuarioId);
}
