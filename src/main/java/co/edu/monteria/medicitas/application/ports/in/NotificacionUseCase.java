package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.Notificacion;
import java.util.List;

public interface NotificacionUseCase {
    List<Notificacion> obtenerNotificacionesUsuario(Long usuarioId);
    void marcarComoLeida(Long notificacionId, Long usuarioId);
}
