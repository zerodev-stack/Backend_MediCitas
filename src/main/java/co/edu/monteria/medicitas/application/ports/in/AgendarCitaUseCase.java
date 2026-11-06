package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.Cita;
import co.edu.monteria.medicitas.domain.model.enums.TipoCita;

import java.util.List;

public interface AgendarCitaUseCase {
    
    /**
     * Agenda una cita médica seleccionando un cupo de disponibilidad libre.
     */
    Cita agendarCita(Long pacienteId, Long disponibilidadId, TipoCita tipo, String observaciones);

    /**
     * Cancela una cita médica. AL CANCELAR, dispara de inmediato el proceso automático
     * de reasignación FIFO para atender a la primera persona en la cola de espera de esa especialidad e IPS.
     */
    Cita cancelarCita(Long citaId, Long usuarioId);

    /**
     * Aplaza una cita trasladándola a una nueva franja horaria de disponibilidad libre.
     */
    Cita aplazarCita(Long citaId, Long nuevaDisponibilidadId, Long pacienteId);

    /**
     * Obtiene todas las citas de un paciente.
     */
    List<Cita> obtenerCitasPaciente(Long pacienteId);

    /**
     * Obtiene las citas programadas para un médico.
     */
    List<Cita> obtenerCitasMedico(Long medicoId);

    /**
     * El médico dueño de la agenda registra que atendió la cita. Solo aplica a citas
     * PROGRAMADAS de hoy o de días anteriores.
     */
    Cita marcarAtendida(Long citaId, Long medicoUsuarioId);

    /**
     * El médico cancela una cita de su agenda. El cupo queda CANCELADO (no vuelve a
     * ofrecerse, porque es el médico quien no puede atender) y el paciente es notificado.
     */
    Cita cancelarPorMedico(Long citaId, Long medicoUsuarioId);
}
