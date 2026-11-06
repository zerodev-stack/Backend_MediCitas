package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.ColaEspera;
import java.util.List;

public interface GestionarColaEsperaUseCase {

    /**
     * Registra al paciente en la cola de espera FIFO para una especialidad e IPS cuando no hay disponibilidad.
     */
    default ColaEspera unirseAColaEspera(Long pacienteId, Long especialidadId, Long ipsId) {
        return unirseAColaEspera(pacienteId, especialidadId, ipsId, null, null);
    }

    /**
     * Variante con servicio y sede preferidos. Ambos son opcionales; si se indican, deben
     * pertenecer a la especialidad y a la IPS de la solicitud.
     */
    ColaEspera unirseAColaEspera(Long pacienteId, Long especialidadId, Long ipsId, Long servicioId, Long sedeId);

    /**
     * Cancela la solicitud en cola de espera.
     */
    void cancelarColaEspera(Long colaId, Long pacienteId);

    /**
     * Consulta las solicitudes en cola de espera de un paciente.
     */
    List<ColaEspera> obtenerColasPaciente(Long pacienteId);
}
