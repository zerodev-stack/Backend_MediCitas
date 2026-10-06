package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.TipoNotificacion;

public interface NotificacionPort {
    /**
     * Envía una notificación simulada (Push/Email) al usuario.
     * Diseñada para ser reemplazada posteriormente por Firebase Cloud Messaging (FCM).
     */
    void enviarNotificacion(Usuario destinatario, String titulo, String mensaje, TipoNotificacion tipo);
}
