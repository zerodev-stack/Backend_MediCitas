package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.GestionarBloquesDisponibilidadUseCase;
import co.edu.monteria.medicitas.application.ports.in.ReasignacionFifoUseCase;
import co.edu.monteria.medicitas.application.ports.out.BloqueDisponibilidadRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.CitaRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.DisponibilidadRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.MedicoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.SedeRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.ServicioMedicoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.ServicioRepositoryPort;
import co.edu.monteria.medicitas.domain.exception.AccesoNoAutorizadoException;
import co.edu.monteria.medicitas.domain.exception.ConflictoConcurrenciaException;
import co.edu.monteria.medicitas.domain.exception.CruceHorarioException;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.exception.ReglaNegocioException;
import co.edu.monteria.medicitas.domain.model.BloqueDisponibilidad;
import co.edu.monteria.medicitas.domain.model.Cita;
import co.edu.monteria.medicitas.domain.model.DisponibilidadMedico;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.ResumenBloque;
import co.edu.monteria.medicitas.domain.model.Sede;
import co.edu.monteria.medicitas.domain.model.Servicio;
import co.edu.monteria.medicitas.domain.model.enums.EstadoBloque;
import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de bloques de disponibilidad.
 *
 * Un bloque es la forma en que el médico piensa su agenda ("los miércoles de 8 a 10,
 * consulta en Sede Centro, cupos de 30 minutos"). El paciente, en cambio, reserva cupos
 * sueltos. Este servicio traduce lo primero a lo segundo: cada bloque genera sus filas de
 * 'disponibilidades_medicas', y a partir de ahí el agendamiento, la cola FIFO y el control
 * de concurrencia siguen funcionando exactamente igual que antes.
 */
@Service
@Transactional
public class BloqueDisponibilidadService implements GestionarBloquesDisponibilidadUseCase {

    static final int DURACION_MINIMA_MINUTOS = 5;
    static final int DURACION_MAXIMA_MINUTOS = 240;
    static final int MAXIMO_FECHAS_POR_SOLICITUD = 62;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("d/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("H:mm");

    private final MedicoRepositoryPort medicoRepository;
    private final ServicioRepositoryPort servicioRepository;
    private final SedeRepositoryPort sedeRepository;
    private final ServicioMedicoRepositoryPort servicioMedicoRepository;
    private final BloqueDisponibilidadRepositoryPort bloqueRepository;
    private final DisponibilidadRepositoryPort disponibilidadRepository;
    private final CitaRepositoryPort citaRepository;
    private final ReasignacionFifoUseCase reasignacionFifoUseCase;

    public BloqueDisponibilidadService(MedicoRepositoryPort medicoRepository,
                                       ServicioRepositoryPort servicioRepository,
                                       SedeRepositoryPort sedeRepository,
                                       ServicioMedicoRepositoryPort servicioMedicoRepository,
                                       BloqueDisponibilidadRepositoryPort bloqueRepository,
                                       DisponibilidadRepositoryPort disponibilidadRepository,
                                       CitaRepositoryPort citaRepository,
                                       ReasignacionFifoUseCase reasignacionFifoUseCase) {
        this.medicoRepository = medicoRepository;
        this.servicioRepository = servicioRepository;
        this.sedeRepository = sedeRepository;
        this.servicioMedicoRepository = servicioMedicoRepository;
        this.bloqueRepository = bloqueRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.citaRepository = citaRepository;
        this.reasignacionFifoUseCase = reasignacionFifoUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumenBloque> listarBloques(Long medicoUsuarioId) {
        Medico medico = medico(medicoUsuarioId);
        LocalDate hoy = LocalDate.now();
        return bloqueRepository.buscarPorMedicoYEstado(medico.getId(), EstadoBloque.ACTIVO).stream()
                .filter(b -> !b.getFecha().isBefore(hoy))
                .map(this::resumir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenBloque obtenerBloque(Long medicoUsuarioId, Long bloqueId) {
        return resumir(bloquePropio(medico(medicoUsuarioId), bloqueId));
    }

    @Override
    public List<ResumenBloque> crearBloques(Long medicoUsuarioId, SolicitudBloque solicitud) {
        return publicar(medico(medicoUsuarioId), solicitud);
    }

    @Override
    public List<ResumenBloque> reemplazarBloque(Long medicoUsuarioId, Long bloqueId, SolicitudBloque solicitud) {
        Medico medico = medico(medicoUsuarioId);
        BloqueDisponibilidad bloque = bloqueActivoPropio(medico, bloqueId);
        if (!citaRepository.buscarProgramadasPorBloqueId(bloqueId).isEmpty()) {
            throw new ReglaNegocioException(
                    "Este bloque ya tiene citas reservadas y no se puede editar. "
                            + "Publica un bloque nuevo o gestiona primero esas citas.");
        }
        // Se cancela antes de validar el nuevo horario: así el bloque viejo no cuenta como
        // cruce consigo mismo. Si la validación falla, la transacción se revierte entera.
        cancelar(bloque);
        return publicar(medico, solicitud);
    }

    @Override
    public ResumenBloque cancelarBloque(Long medicoUsuarioId, Long bloqueId) {
        Medico medico = medico(medicoUsuarioId);
        BloqueDisponibilidad bloque = bloqueActivoPropio(medico, bloqueId);
        List<Cita> citas = citaRepository.buscarProgramadasPorBloqueId(bloqueId);
        if (!citas.isEmpty()) {
            throw new ReglaNegocioException(
                    "No puedes cancelar este bloque: tiene " + citas.size() + (citas.size() == 1 ? " cita reservada" : " citas reservadas")
                            + ". Gestiona esas citas antes de cancelar el bloque.");
        }
        cancelar(bloque);
        return resumir(bloque);
    }

    private List<ResumenBloque> publicar(Medico medico, SolicitudBloque solicitud) {
        Servicio servicio = servicioRepository.buscarPorId(solicitud.servicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));
        Sede sede = sedeRepository.buscarPorId(solicitud.sedeId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada"));
        if (!servicioMedicoRepository.existe(medico.getId(), servicio.getId(), sede.getId())) {
            throw new ReglaNegocioException("No tienes habilitado " + servicio.getNombre() + " en " + sede.getNombre()
                    + ". Configúralo en Mis servicios y sedes antes de publicar disponibilidad.");
        }

        LocalTime inicio = solicitud.horaInicio();
        LocalTime fin = solicitud.horaFin();
        int duracion = solicitud.duracionCupoMinutos();
        if (!inicio.isBefore(fin)) {
            throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin.");
        }
        if (duracion < DURACION_MINIMA_MINUTOS || duracion > DURACION_MAXIMA_MINUTOS) {
            throw new ReglaNegocioException("La duración de cada cupo debe estar entre "
                    + DURACION_MINIMA_MINUTOS + " y " + DURACION_MAXIMA_MINUTOS + " minutos.");
        }
        long minutosBloque = Duration.between(inicio, fin).toMinutes();
        if (minutosBloque < duracion) {
            throw new ReglaNegocioException("El horario es más corto que la duración de un cupo.");
        }

        List<LocalDate> fechas = solicitud.fechas() == null ? List.of()
                : solicitud.fechas().stream().distinct().sorted().toList();
        if (fechas.isEmpty()) {
            throw new ReglaNegocioException("Selecciona al menos un día para la disponibilidad.");
        }
        if (fechas.size() > MAXIMO_FECHAS_POR_SOLICITUD) {
            throw new ReglaNegocioException("Puedes publicar como máximo " + MAXIMO_FECHAS_POR_SOLICITUD
                    + " días en una sola solicitud.");
        }

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        for (LocalDate fecha : fechas) {
            if (fecha.isBefore(hoy) || (fecha.equals(hoy) && !inicio.isAfter(ahora))) {
                throw new ReglaNegocioException("No es posible publicar disponibilidad en fechas u horas pasadas ("
                        + fecha.format(FORMATO_FECHA) + ").");
            }
            verificarCruces(medico, fecha, inicio, fin);
        }

        int cuposPorBloque = (int) (minutosBloque / duracion);
        List<BloqueDisponibilidad> creados = new ArrayList<>();
        List<DisponibilidadMedico> cuposCreados = new ArrayList<>();
        for (LocalDate fecha : fechas) {
            BloqueDisponibilidad bloque = bloqueRepository.guardar(new BloqueDisponibilidad(
                    null, medico, servicio, sede, fecha, inicio, fin, duracion,
                    EstadoBloque.ACTIVO, LocalDateTime.now()));
            creados.add(bloque);
            // Se itera por número de cupos y no comparando horas: un bloque que acaba a las
            // 23:59 haría que 'hora + duración' diera la vuelta a medianoche.
            for (int i = 0; i < cuposPorBloque; i++) {
                LocalTime cupoInicio = inicio.plusMinutes((long) i * duracion);
                DisponibilidadMedico cupo = new DisponibilidadMedico(null, medico, fecha,
                        cupoInicio, cupoInicio.plusMinutes(duracion), EstadoDisponibilidad.LIBRE);
                cupo.setBloque(bloque);
                cuposCreados.add(disponibilidadRepository.guardar(cupo));
            }
        }

        // Igual que una franja suelta: cada cupo nuevo atiende primero a quien lleva más
        // tiempo esperando en la cola FIFO de esta especialidad e IPS.
        cuposCreados.forEach(reasignacionFifoUseCase::reasignarSiHayEspera);

        return creados.stream().map(this::resumir).toList();
    }

    private void verificarCruces(Medico medico, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        for (BloqueDisponibilidad existente : bloqueRepository.buscarPorMedicoFechaYEstado(
                medico.getId(), fecha, EstadoBloque.ACTIVO)) {
            if (existente.seCruzaCon(fecha, inicio, fin)) {
                throw new CruceHorarioException("Ya tienes " + existente.getServicio().getNombre() + " de "
                        + existente.getHoraInicio().format(FORMATO_HORA) + " a "
                        + existente.getHoraFin().format(FORMATO_HORA) + " el " + fecha.format(FORMATO_FECHA)
                        + " en " + existente.getSede().getNombre() + ". Ajusta el horario o elige otro día.");
            }
        }
        for (DisponibilidadMedico franja : disponibilidadRepository.buscarSueltasVigentesDeMedico(medico.getId(), fecha)) {
            if (franja.getHoraInicio().isBefore(fin) && inicio.isBefore(franja.getHoraFin())) {
                throw new CruceHorarioException("Ya tienes una franja publicada de "
                        + franja.getHoraInicio().format(FORMATO_HORA) + " a "
                        + franja.getHoraFin().format(FORMATO_HORA) + " el " + fecha.format(FORMATO_FECHA)
                        + ". Ajusta el horario o elige otro día.");
            }
        }
    }

    private void cancelar(BloqueDisponibilidad bloque) {
        bloque.setEstado(EstadoBloque.CANCELADO);
        bloqueRepository.guardar(bloque);
        for (DisponibilidadMedico cupo : disponibilidadRepository.buscarPorBloqueId(bloque.getId())) {
            if (cupo.getEstado() != EstadoDisponibilidad.LIBRE) {
                continue;
            }
            // Transición condicionada: si un paciente reservó el cupo entre la comprobación
            // de citas y este punto, se aborta todo para no dejar una cita en un bloque
            // cancelado.
            boolean cancelado = disponibilidadRepository.transicionarEstadoSiCoincide(
                    cupo.getId(), EstadoDisponibilidad.LIBRE, EstadoDisponibilidad.CANCELADO);
            if (!cancelado) {
                throw new ConflictoConcurrenciaException(
                        "Un paciente acaba de reservar un cupo de este bloque. Actualiza e inténtalo de nuevo.");
            }
        }
    }

    private ResumenBloque resumir(BloqueDisponibilidad bloque) {
        return new ResumenBloque(bloque,
                disponibilidadRepository.buscarPorBloqueId(bloque.getId()),
                citaRepository.buscarProgramadasPorBloqueId(bloque.getId()));
    }

    private BloqueDisponibilidad bloqueActivoPropio(Medico medico, Long bloqueId) {
        BloqueDisponibilidad bloque = bloquePropio(medico, bloqueId);
        if (bloque.getEstado() != EstadoBloque.ACTIVO) {
            throw new ReglaNegocioException("Este bloque ya fue cancelado.");
        }
        return bloque;
    }

    private BloqueDisponibilidad bloquePropio(Medico medico, Long bloqueId) {
        BloqueDisponibilidad bloque = bloqueRepository.buscarPorId(bloqueId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bloque de disponibilidad no encontrado"));
        if (!bloque.getMedico().getId().equals(medico.getId())) {
            throw new AccesoNoAutorizadoException("Este bloque pertenece a otro profesional.");
        }
        return bloque;
    }

    private Medico medico(Long medicoUsuarioId) {
        return medicoRepository.buscarPorUsuarioId(medicoUsuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de médico no encontrado"));
    }
}
