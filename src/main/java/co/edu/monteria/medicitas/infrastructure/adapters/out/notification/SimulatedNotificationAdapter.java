package co.edu.monteria.medicitas.infrastructure.adapters.out.notification;

import co.edu.monteria.medicitas.application.ports.out.NotificacionPort;
import co.edu.monteria.medicitas.application.ports.out.NotificacionRepositoryPort;
import co.edu.monteria.medicitas.domain.model.Notificacion;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.TipoNotificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Adaptador de salida para notificaciones Push y Email.
 * Almacena la notificación en base de datos e imprime el log de envío simulado.
 * Diseñado con interfaz limpia para reemplazar fácilmente por Firebase Cloud Messaging (FCM) o SendGrid.
 */
@Component
public class SimulatedNotificationAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(SimulatedNotificationAdapter.class);

    private final NotificacionRepositoryPort notificacionRepository;

    public SimulatedNotificationAdapter(NotificacionRepositoryPort notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    public void enviarNotificacion(Usuario destinatario, String titulo, String mensaje, TipoNotificacion tipo) {
        if (destinatario == null) return;

        log.info("[SIMULATED NOTIFICATION - {}] Para: {} ({}) | Título: '{}' | Mensaje: '{}'",
                tipo, destinatario.getNombreCompleto(), destinatario.getEmail(), titulo, mensaje);

        // Guardar notificación en base de datos para consulta en la bandeja de entrada del usuario
        Notificacion notif = new Notificacion();
        notif.setUsuario(destinatario);
        notif.setTitulo(titulo);
        notif.setMensaje(mensaje);
        notif.setTipo(tipo != null ? tipo : TipoNotificacion.PUSH);
        notif.setLeida(false);
        notif.setFechaEnvio(LocalDateTime.now());

        notificacionRepository.guardar(notif);
    }
}
