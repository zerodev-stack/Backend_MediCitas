package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.Notificacion;
import co.edu.monteria.medicitas.domain.model.enums.TipoNotificacion;

import java.time.LocalDateTime;

/**
 * Respuesta de una alerta de la bandeja de entrada.
 *
 * No incluye el usuario destinatario: la bandeja siempre corresponde al usuario
 * autenticado, por lo que anidar el usuario solo multiplicaría el tamaño de la respuesta.
 */
public record NotificacionResponse(
        Long id,
        String titulo,
        String mensaje,
        TipoNotificacion tipo,
        boolean leida,
        LocalDateTime fechaEnvio
) {
    public static NotificacionResponse desde(Notificacion notificacion) {
        if (notificacion == null) {
            return null;
        }
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getTitulo(),
                notificacion.getMensaje(),
                notificacion.getTipo(),
                notificacion.isLeida(),
                notificacion.getFechaEnvio()
        );
    }
}
