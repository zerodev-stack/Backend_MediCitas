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
    default Optional<ColaEspera> obtenerCabeceraFifoBloqueada(
            Long especialidadId, Long ipsId, EstadoColaEspera estado) {
        return obtenerCabeceraFifoBloqueada(especialidadId, ipsId, null, null, estado);
    }

    /**
     * Igual que la anterior, pero solo considera solicitudes compatibles con el cupo: las que
     * no pidieron servicio o sede, o pidieron exactamente los del cupo. Un cupo sin bloque
     * (servicioId y sedeId null) solo atiende solicitudes sin servicio ni sede.
     */
    Optional<ColaEspera> obtenerCabeceraFifoBloqueada(
            Long especialidadId, Long ipsId, Long servicioId, Long sedeId, EstadoColaEspera estado);

    /**
     * Solicitudes EN_ESPERA de una especialidad e IPS en orden FIFO, sin bloqueo: es la
     * lista que consulta el médico, no la que se usa para asignar automáticamente.
     */
    List<ColaEspera> buscarEnEspera(Long especialidadId, Long ipsId);

    boolean existeEnEspera(Long pacienteId, Long especialidadId, Long ipsId, EstadoColaEspera estado);
}
