package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.GestionarColaEsperaUseCase;
import co.edu.monteria.medicitas.application.ports.out.ColaEsperaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.EspecialidadRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.IpsRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.NotificacionPort;
import co.edu.monteria.medicitas.application.ports.out.SedeRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.ServicioRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.UsuarioRepositoryPort;
import co.edu.monteria.medicitas.domain.exception.AccesoNoAutorizadoException;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.exception.ReglaNegocioException;
import co.edu.monteria.medicitas.domain.model.ColaEspera;
import co.edu.monteria.medicitas.domain.model.Especialidad;
import co.edu.monteria.medicitas.domain.model.Ips;
import co.edu.monteria.medicitas.domain.model.Sede;
import co.edu.monteria.medicitas.domain.model.Servicio;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.EstadoColaEspera;
import co.edu.monteria.medicitas.domain.model.enums.TipoNotificacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ColaEsperaService implements GestionarColaEsperaUseCase {

    private final ColaEsperaRepositoryPort colaEsperaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final EspecialidadRepositoryPort especialidadRepository;
    private final IpsRepositoryPort ipsRepository;
    private final NotificacionPort notificacionPort;
    private final ServicioRepositoryPort servicioRepository;
    private final SedeRepositoryPort sedeRepository;

    public ColaEsperaService(ColaEsperaRepositoryPort colaEsperaRepository,
                             UsuarioRepositoryPort usuarioRepository,
                             EspecialidadRepositoryPort especialidadRepository,
                             IpsRepositoryPort ipsRepository,
                             NotificacionPort notificacionPort,
                             ServicioRepositoryPort servicioRepository,
                             SedeRepositoryPort sedeRepository) {
        this.colaEsperaRepository = colaEsperaRepository;
        this.usuarioRepository = usuarioRepository;
        this.especialidadRepository = especialidadRepository;
        this.ipsRepository = ipsRepository;
        this.notificacionPort = notificacionPort;
        this.servicioRepository = servicioRepository;
        this.sedeRepository = sedeRepository;
    }

    @Override
    public ColaEspera unirseAColaEspera(Long pacienteId, Long especialidadId, Long ipsId,
                                        Long servicioId, Long sedeId) {
        Usuario paciente = usuarioRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));

        Especialidad especialidad = especialidadRepository.buscarPorId(especialidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));

        Ips ips = ipsRepository.buscarPorId(ipsId)
                .orElseThrow(() -> new RecursoNoEncontradoException("IPS no encontrada"));

        Servicio servicio = null;
        if (servicioId != null) {
            servicio = servicioRepository.buscarPorId(servicioId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));
            if (!servicio.getEspecialidad().getId().equals(especialidadId)) {
                throw new ReglaNegocioException("El servicio solicitado no pertenece a la especialidad elegida.");
            }
        }
        Sede sede = null;
        if (sedeId != null) {
            sede = sedeRepository.buscarPorId(sedeId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada"));
            if (!sede.getIps().getId().equals(ipsId)) {
                throw new ReglaNegocioException("La sede solicitada no pertenece a la IPS elegida.");
            }
        }

        boolean yaEnEspera = colaEsperaRepository.existeEnEspera(
                pacienteId, especialidadId, ipsId, EstadoColaEspera.EN_ESPERA);
        if (yaEnEspera) {
            throw new ReglaNegocioException(
                    "Ya posees una solicitud activa en la cola de espera para esta especialidad e IPS.");
        }

        ColaEspera cola = new ColaEspera();
        cola.setPaciente(paciente);
        cola.setEspecialidad(especialidad);
        cola.setIps(ips);
        cola.setServicio(servicio);
        cola.setSede(sede);
        cola.setFechaSolicitud(LocalDateTime.now());
        cola.setEstado(EstadoColaEspera.EN_ESPERA);

        ColaEspera guardada = colaEsperaRepository.guardar(cola);

        notificacionPort.enviarNotificacion(
                paciente,
                "Inscrito en Cola de Espera FIFO",
                "Te has registrado exitosamente en la cola de espera para " + especialidad.getNombre() +
                " en " + ips.getNombre() + ". Te avisaremos inmediatamente cuando se libere un cupo.",
                TipoNotificacion.PUSH
        );

        return guardada;
    }

    @Override
    public void cancelarColaEspera(Long colaId, Long pacienteId) {
        ColaEspera cola = colaEsperaRepository.buscarPorId(colaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de cola de espera no encontrado"));

        if (cola.getPaciente() == null || !cola.getPaciente().getId().equals(pacienteId)) {
            throw new AccesoNoAutorizadoException("No tienes permisos para modificar esta solicitud.");
        }
        if (cola.getEstado() != EstadoColaEspera.EN_ESPERA) {
            throw new ReglaNegocioException("Solo es posible retirarse de una solicitud que siga EN_ESPERA.");
        }

        cola.setEstado(EstadoColaEspera.CANCELADO);
        colaEsperaRepository.guardar(cola);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColaEspera> obtenerColasPaciente(Long pacienteId) {
        return colaEsperaRepository.buscarPorPacienteId(pacienteId);
    }
}
