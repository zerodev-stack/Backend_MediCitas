package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.GestionarDisponibilidadUseCase;
import co.edu.monteria.medicitas.application.ports.in.ReasignacionFifoUseCase;
import co.edu.monteria.medicitas.application.ports.out.DisponibilidadRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.MedicoRepositoryPort;
import co.edu.monteria.medicitas.domain.exception.ReglaNegocioException;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class DisponibilidadService implements GestionarDisponibilidadUseCase {

    private final DisponibilidadRepositoryPort disponibilidadRepository;
    private final MedicoRepositoryPort medicoRepository;
    private final ReasignacionFifoUseCase reasignacionFifoUseCase;

    public DisponibilidadService(DisponibilidadRepositoryPort disponibilidadRepository,
                                 MedicoRepositoryPort medicoRepository,
                                 ReasignacionFifoUseCase reasignacionFifoUseCase) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.medicoRepository = medicoRepository;
        this.reasignacionFifoUseCase = reasignacionFifoUseCase;
    }

    @Override
    public DisponibilidadMedico crearDisponibilidad(Long medicoUsuarioId, LocalDate fecha,
                                                    LocalTime horaInicio, LocalTime horaFin) {
        Medico medico = medicoRepository.buscarPorUsuarioId(medicoUsuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de médico no encontrado"));

        if (!horaInicio.isBefore(horaFin)) {
            throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin.");
        }
        if (fecha != null && !fecha.isAfter(LocalDate.now())) {
            throw new ReglaNegocioException("No es posible publicar disponibilidad en fechas pasadas.");
        }

        DisponibilidadMedico disp = new DisponibilidadMedico();
        disp.setMedico(medico);
        disp.setFecha(fecha);
        disp.setHoraInicio(horaInicio);
        disp.setHoraFin(horaFin);
        disp.setEstado(EstadoDisponibilidad.LIBRE);

        DisponibilidadMedico guardada = disponibilidadRepository.guardar(disp);

        // Al publicar una franja se evalúa, de forma transparente, si hay pacientes en la
        // cola FIFO esperando por esta especialidad e IPS.
        reasignacionFifoUseCase.reasignarSiHayEspera(guardada);

        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadMedico> buscarDisponibilidadesLibres(Long especialidadId, Long ipsId, LocalDate fecha) {
        return disponibilidadRepository.buscarDisponibles(especialidadId, ipsId, fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadMedico> obtenerDisponibilidadesMedico(Long medicoUsuarioId) {
        Medico medico = medicoRepository.buscarPorUsuarioId(medicoUsuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de médico no encontrado"));

        return disponibilidadRepository.buscarPorMedicoId(medico.getId());
    }
}
