package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.ResumenBloque;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Publicación y gestión de bloques de disponibilidad por parte del médico autenticado.
 */
public interface GestionarBloquesDisponibilidadUseCase {

    /**
     * Datos de un bloque a publicar. Se admite más de una fecha porque la app permite
     * marcar varios días específicos o repetir un día de la semana; se crea un bloque
     * por fecha, todos en la misma transacción.
     */
    record SolicitudBloque(Long servicioId, Long sedeId, List<LocalDate> fechas,
                           LocalTime horaInicio, LocalTime horaFin, Integer duracionCupoMinutos) {}

    /** Bloques activos de hoy en adelante, en orden cronológico. */
    List<ResumenBloque> listarBloques(Long medicoUsuarioId);

    ResumenBloque obtenerBloque(Long medicoUsuarioId, Long bloqueId);

    /**
     * Publica los bloques y genera sus cupos. Cada cupo nuevo pasa por la reasignación
     * FIFO, igual que una franja suelta: si hay pacientes esperando esa especialidad e IPS,
     * se les asigna de inmediato.
     */
    List<ResumenBloque> crearBloques(Long medicoUsuarioId, SolicitudBloque solicitud);

    /**
     * Edita un bloque sin reservas: lo cancela y publica la nueva versión en una sola
     * transacción. Si el bloque ya tiene citas, se rechaza para no mover a nadie de horario.
     */
    List<ResumenBloque> reemplazarBloque(Long medicoUsuarioId, Long bloqueId, SolicitudBloque solicitud);

    /**
     * Cancela un bloque y sus cupos libres. Si hay citas reservadas, se rechaza: las
     * reservas se conservan y no se cancelan ni reasignan automáticamente.
     */
    ResumenBloque cancelarBloque(Long medicoUsuarioId, Long bloqueId);
}
