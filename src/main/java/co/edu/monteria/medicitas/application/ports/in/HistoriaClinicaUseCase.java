package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.AutorizacionAcceso;
import co.edu.monteria.medicitas.domain.model.HistoriaClinica;

import java.util.List;

public interface HistoriaClinicaUseCase {

    /**
     * Registra una atención clínica. La custodia del registro (ips_origen) se deriva de la
     * IPS del médico autenticado; no se acepta desde el exterior para evitar que un
     * registro quede bajo custodia de una institución que no atendió.
     */
    HistoriaClinica registrarHistoriaClinica(Long medicoUsuarioId, Long pacienteId,
                                              String diagnostico, String tratamiento, String contenido);

    /**
     * Historias clínicas del paciente indicado. El identificador debe provenir del token.
     */
    List<HistoriaClinica> consultarHistoriasPaciente(Long pacienteId);

    /**
     * Consulta desde una IPS solicitante. Filtra los registros según la autorización vigente
     * del paciente (Res. 1995/1999) y verifica que la IPS solicitante sea la del médico
     * autenticado.
     */
    List<HistoriaClinica> consultarHistoriasDesdeIps(Long medicoUsuarioId, Long pacienteId, Long ipsSolicitanteId);

    AutorizacionAcceso autorizarAccesoIps(Long historiaId, Long ipsSolicitanteId, Long pacienteId);

    AutorizacionAcceso revocarAccesoIps(Long historiaId, Long ipsSolicitanteId, Long pacienteId);

    List<AutorizacionAcceso> obtenerAutorizacionesPaciente(Long pacienteId);
}
