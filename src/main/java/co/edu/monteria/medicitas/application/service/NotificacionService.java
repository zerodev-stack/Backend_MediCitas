package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.NotificacionUseCase;
import co.edu.monteria.medicitas.application.ports.out.NotificacionRepositoryPort;
import co.edu.monteria.medicitas.domain.exception.AccesoNoAutorizadoException;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.model.Notificacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificacionService implements NotificacionUseCase {

    private final NotificacionRepositoryPort notificacionRepository;

    public NotificacionService(NotificacionRepositoryPort notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> obtenerNotificacionesUsuario(Long usuarioId) {
        return notificacionRepository.buscarPorUsuarioId(usuarioId);
    }

    @Override
    public void marcarComoLeida(Long notificacionId, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.buscarPorId(notificacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada"));

        if (notificacion.getUsuario() == null || !notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new AccesoNoAutorizadoException("No tiene permisos sobre esta notificación.");
        }
        if (notificacion.isLeida()) {
            return;
        }

        notificacion.setLeida(true);
        notificacionRepository.guardar(notificacion);
    }
}
