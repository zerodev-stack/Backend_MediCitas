package co.edu.monteria.medicitas.application.service;

import co.edu.monteria.medicitas.application.ports.in.GestionarPerfilMedicoUseCase;
import co.edu.monteria.medicitas.application.ports.out.MedicoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.SedeRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.ServicioMedicoRepositoryPort;
import co.edu.monteria.medicitas.application.ports.out.ServicioRepositoryPort;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.exception.ReglaNegocioException;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.Sede;
import co.edu.monteria.medicitas.domain.model.Servicio;
import co.edu.monteria.medicitas.domain.model.ServicioMedico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerfilMedicoService implements GestionarPerfilMedicoUseCase {

    private final MedicoRepositoryPort medicoRepository;
    private final ServicioRepositoryPort servicioRepository;
    private final SedeRepositoryPort sedeRepository;
    private final ServicioMedicoRepositoryPort servicioMedicoRepository;

    public PerfilMedicoService(MedicoRepositoryPort medicoRepository,
                               ServicioRepositoryPort servicioRepository,
                               SedeRepositoryPort sedeRepository,
                               ServicioMedicoRepositoryPort servicioMedicoRepository) {
        this.medicoRepository = medicoRepository;
        this.servicioRepository = servicioRepository;
        this.sedeRepository = sedeRepository;
        this.servicioMedicoRepository = servicioMedicoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Medico obtenerPerfil(Long medicoUsuarioId) {
        return medico(medicoUsuarioId);
    }

    @Override
    public Medico actualizarDatosContacto(Long medicoUsuarioId, String ciudadAtencion,
                                          String correoContacto, String telefonoContacto) {
        Medico medico = medico(medicoUsuarioId);
        if (vacio(ciudadAtencion)) {
            throw new ReglaNegocioException("Selecciona la ciudad donde atiendes.");
        }
        medico.setCiudadAtencion(ciudadAtencion.trim());
        // Los campos opcionales vacíos se guardan como null para que la app muestre
        // "Sin registrar" y no una cadena en blanco.
        medico.setCorreoContacto(vacio(correoContacto) ? null : correoContacto.trim());
        medico.setTelefonoContacto(vacio(telefonoContacto) ? null : telefonoContacto.trim());
        return medicoRepository.guardar(medico);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> serviciosDeMiEspecialidad(Long medicoUsuarioId) {
        return servicioRepository.buscarPorEspecialidadId(medico(medicoUsuarioId).getEspecialidad().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sede> sedesDeMiInstitucion(Long medicoUsuarioId) {
        return sedeRepository.buscarPorIpsId(medico(medicoUsuarioId).getIps().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioMedico> obtenerServiciosConfigurados(Long medicoUsuarioId) {
        return servicioMedicoRepository.buscarPorMedicoId(medico(medicoUsuarioId).getId());
    }

    @Override
    public List<ServicioMedico> configurarServicios(Long medicoUsuarioId, Map<Long, Set<Long>> sedesPorServicio) {
        Medico medico = medico(medicoUsuarioId);

        // Solo se aceptan servicios de su especialidad y sedes de su IPS: el catálogo
        // completo es público, pero ofrecer un servicio ajeno publicaría cupos falsos.
        Map<Long, Servicio> servicios = servicioRepository.buscarPorEspecialidadId(medico.getEspecialidad().getId())
                .stream().collect(Collectors.toMap(Servicio::getId, Function.identity()));
        Map<Long, Sede> sedes = sedeRepository.buscarPorIpsId(medico.getIps().getId())
                .stream().collect(Collectors.toMap(Sede::getId, Function.identity()));

        List<ServicioMedico> configuracion = new ArrayList<>();
        sedesPorServicio.forEach((servicioId, sedeIds) -> {
            Servicio servicio = servicios.get(servicioId);
            if (servicio == null) {
                throw new ReglaNegocioException("El servicio seleccionado no pertenece a tu especialidad.");
            }
            if (sedeIds == null || sedeIds.isEmpty()) {
                throw new ReglaNegocioException("Selecciona al menos una sede para " + servicio.getNombre() + ".");
            }
            for (Long sedeId : sedeIds) {
                Sede sede = sedes.get(sedeId);
                if (sede == null) {
                    throw new ReglaNegocioException("Solo puedes elegir sedes de tu institución.");
                }
                configuracion.add(new ServicioMedico(null, medico, servicio, sede));
            }
        });

        return servicioMedicoRepository.reemplazarConfiguracion(medico.getId(), configuracion);
    }

    private Medico medico(Long medicoUsuarioId) {
        return medicoRepository.buscarPorUsuarioId(medicoUsuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de médico no encontrado"));
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
