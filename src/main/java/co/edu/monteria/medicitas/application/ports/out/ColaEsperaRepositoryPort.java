package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.ColaEspera;
import co.edu.monteria.medicitas.domain.model.enums.EstadoColaEspera;

import java.util.List;
import java.util.Optional;

public interface ColaEsperaRepositoryPort {

    ColaEspera guardar(ColaEspera colaEspera);

    Optional<ColaEspera> buscarPorId(Long id);

    List<ColaEspera> buscarPorPacienteId(Long pacienteId);

    /**
     * Devuelve la solicitud EN_ESPERA más antigua (fecha_solicitud ASC) para la especialidad
     * e IPS dadas, tomando un bloqueo de escritura sobre la fila seleccionada.
     *
     * POR QUÉ EL BLOQUEO: la cabecera de la cola FIFO es un recurso disputado. Sin bloqueo,
     * dos transacciones concurrentes (por ejemplo, dos pacientes que cancelan a la vez y
     * liberan cupos del mismo médico) leerían la misma fila y generarían dos citas para el
     * mismo cupo. El adaptador de persistencia debe traducir este método a
     * 'SELECT ... FOR UPDATE'.
     */
    Optional<ColaEspera> obtenerCabeceraFifoBloqueada(
            Long especialidadId, Long ipsId, EstadoColaEspera estado);

    boolean existeEnEspera(Long pacienteId, Long especialidadId, Long ipsId, EstadoColaEspera estado);
}
