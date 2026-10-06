package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface GestionarDisponibilidadUseCase {

    /**
     * El médico registra o abre un nuevo bloque de disponibilidad horaria.
     * Al crear una nueva disponibilidad, verifica si hay solicitudes en cola de espera (FIFO)
     * para asignar automáticamente el cupo recién creado.
     */
    DisponibilidadMedico crearDisponibilidad(Long medicoUsuarioId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);

    /**
     * Consulta disponibilidades médicas libres por especialidad, IPS y fecha opcional.
     */
    List<DisponibilidadMedico> buscarDisponibilidadesLibres(Long especialidadId, Long ipsId, LocalDate fecha);

    /**
     * Consulta todos los bloques de horario registrados por un médico.
     */
    List<DisponibilidadMedico> obtenerDisponibilidadesMedico(Long medicoUsuarioId);
}
