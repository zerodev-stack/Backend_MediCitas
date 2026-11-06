package co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.mapper;

import co.edu.monteria.medicitas.domain.model.*;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.*;
import org.springframework.stereotype.Component;

@Component
public class DomainEntityMapper {

    public Eps toEpsDomain(EpsEntity entity) {
        if (entity == null) return null;
        return new Eps(entity.getId(), entity.getNombre(), entity.getCodigoBdua(), entity.getActiva());
    }

    public EpsEntity toEpsEntity(Eps domain) {
        if (domain == null) return null;
        return new EpsEntity(domain.getId(), domain.getNombre(), domain.getCodigoBdua(), domain.getActiva());
    }

    public Ips toIpsDomain(IpsEntity entity) {
        if (entity == null) return null;
        return new Ips(entity.getId(), entity.getNombre(), entity.getNit(), entity.getDireccion(), entity.getCiudad(), entity.getTelefono());
    }

    public IpsEntity toIpsEntity(Ips domain) {
        if (domain == null) return null;
        return new IpsEntity(domain.getId(), domain.getNombre(), domain.getNit(), domain.getDireccion(), domain.getCiudad(), domain.getTelefono());
    }

    public Usuario toUsuarioDomain(UsuarioEntity entity) {
        if (entity == null) return null;
        return new Usuario(
                entity.getId(),
                entity.getNumDocumento(),
                entity.getTipoDocumento(),
                entity.getNombreCompleto(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRol(),
                toEpsDomain(entity.getEps()),
                entity.getTelefono(),
                entity.getFechaRegistro()
        );
    }

    public UsuarioEntity toUsuarioEntity(Usuario domain) {
        if (domain == null) return null;
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(domain.getId());
        entity.setNumDocumento(domain.getNumDocumento());
        entity.setTipoDocumento(domain.getTipoDocumento());
        entity.setNombreCompleto(domain.getNombreCompleto());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setRol(domain.getRol());
        entity.setEps(toEpsEntity(domain.getEps()));
        entity.setTelefono(domain.getTelefono());
        if (domain.getFechaRegistro() != null) {
            entity.setFechaRegistro(domain.getFechaRegistro());
        }
        return entity;
    }

    public Especialidad toEspecialidadDomain(EspecialidadEntity entity) {
        if (entity == null) return null;
        return new Especialidad(entity.getId(), entity.getNombre(), entity.getDescripcion());
    }

    public EspecialidadEntity toEspecialidadEntity(Especialidad domain) {
        if (domain == null) return null;
        return new EspecialidadEntity(domain.getId(), domain.getNombre(), domain.getDescripcion());
    }

    public Medico toMedicoDomain(MedicoEntity entity) {
        if (entity == null) return null;
        Medico medico = new Medico(
                entity.getId(),
                toUsuarioDomain(entity.getUsuario()),
                toEspecialidadDomain(entity.getEspecialidad()),
                toIpsDomain(entity.getIps()),
                entity.getRegistroMedico()
        );
        medico.setCiudadAtencion(entity.getCiudadAtencion());
        medico.setCorreoContacto(entity.getCorreoContacto());
        medico.setTelefonoContacto(entity.getTelefonoContacto());
        return medico;
    }

    public MedicoEntity toMedicoEntity(Medico domain) {
        if (domain == null) return null;
        MedicoEntity entity = new MedicoEntity();
        entity.setId(domain.getId());
        entity.setUsuario(toUsuarioEntity(domain.getUsuario()));
        entity.setEspecialidad(toEspecialidadEntity(domain.getEspecialidad()));
        entity.setIps(toIpsEntity(domain.getIps()));
        entity.setRegistroMedico(domain.getRegistroMedico());
        entity.setCiudadAtencion(domain.getCiudadAtencion());
        entity.setCorreoContacto(domain.getCorreoContacto());
        entity.setTelefonoContacto(domain.getTelefonoContacto());
        return entity;
    }

    public DisponibilidadMedico toDisponibilidadDomain(DisponibilidadMedicoEntity entity) {
        if (entity == null) return null;
        DisponibilidadMedico disponibilidad = new DisponibilidadMedico(
                entity.getId(),
                toMedicoDomain(entity.getMedico()),
                entity.getFecha(),
                entity.getHoraInicio(),
                entity.getHoraFin(),
                entity.getEstado()
        );
        disponibilidad.setBloque(toBloqueDomain(entity.getBloque()));
        return disponibilidad;
    }

    public DisponibilidadMedicoEntity toDisponibilidadEntity(DisponibilidadMedico domain) {
        if (domain == null) return null;
        DisponibilidadMedicoEntity entity = new DisponibilidadMedicoEntity();
        entity.setId(domain.getId());
        entity.setMedico(toMedicoEntity(domain.getMedico()));
        entity.setFecha(domain.getFecha());
        entity.setHoraInicio(domain.getHoraInicio());
        entity.setHoraFin(domain.getHoraFin());
        entity.setEstado(domain.getEstado());
        entity.setBloque(toBloqueEntity(domain.getBloque()));
        return entity;
    }

    public Cita toCitaDomain(CitaEntity entity) {
        if (entity == null) return null;
        return new Cita(
                entity.getId(),
                toUsuarioDomain(entity.getPaciente()),
                toDisponibilidadDomain(entity.getDisponibilidad()),
                toIpsDomain(entity.getIps()),
                entity.getTipo(),
                entity.getEstado(),
                entity.getObservaciones(),
                entity.getFechaCreacion()
        );
    }

    public CitaEntity toCitaEntity(Cita domain) {
        if (domain == null) return null;
        CitaEntity entity = new CitaEntity();
        entity.setId(domain.getId());
        entity.setPaciente(toUsuarioEntity(domain.getPaciente()));
        entity.setDisponibilidad(toDisponibilidadEntity(domain.getDisponibilidad()));
        entity.setIps(toIpsEntity(domain.getIps()));
        entity.setTipo(domain.getTipo());
        entity.setEstado(domain.getEstado());
        entity.setObservaciones(domain.getObservaciones());
        if (domain.getFechaCreacion() != null) {
            entity.setFechaCreacion(domain.getFechaCreacion());
        }
        return entity;
    }

    public ColaEspera toColaEsperaDomain(ColaEsperaEntity entity) {
        if (entity == null) return null;
        ColaEspera cola = new ColaEspera(
                entity.getId(),
                toUsuarioDomain(entity.getPaciente()),
                toEspecialidadDomain(entity.getEspecialidad()),
                toIpsDomain(entity.getIps()),
                entity.getFechaSolicitud(),
                entity.getEstado()
        );
        cola.setServicio(toServicioDomain(entity.getServicio()));
        cola.setSede(toSedeDomain(entity.getSede()));
        return cola;
    }

    public ColaEsperaEntity toColaEsperaEntity(ColaEspera domain) {
        if (domain == null) return null;
        ColaEsperaEntity entity = new ColaEsperaEntity();
        entity.setId(domain.getId());
        entity.setPaciente(toUsuarioEntity(domain.getPaciente()));
        entity.setEspecialidad(toEspecialidadEntity(domain.getEspecialidad()));
        entity.setIps(toIpsEntity(domain.getIps()));
        if (domain.getFechaSolicitud() != null) {
            entity.setFechaSolicitud(domain.getFechaSolicitud());
        }
        entity.setEstado(domain.getEstado());
        entity.setServicio(toServicioEntity(domain.getServicio()));
        entity.setSede(toSedeEntity(domain.getSede()));
        return entity;
    }

    public HistoriaClinica toHistoriaClinicaDomain(HistoriaClinicaEntity entity) {
        if (entity == null) return null;
        return new HistoriaClinica(
                entity.getId(),
                toUsuarioDomain(entity.getPaciente()),
                toIpsDomain(entity.getIpsOrigen()),
                entity.getDiagnostico(),
                entity.getTratamiento(),
                entity.getContenido(),
                entity.getFechaRegistro()
        );
    }

    public HistoriaClinicaEntity toHistoriaClinicaEntity(HistoriaClinica domain) {
        if (domain == null) return null;
        HistoriaClinicaEntity entity = new HistoriaClinicaEntity();
        entity.setId(domain.getId());
        entity.setPaciente(toUsuarioEntity(domain.getPaciente()));
        entity.setIpsOrigen(toIpsEntity(domain.getIpsOrigen()));
        entity.setDiagnostico(domain.getDiagnostico());
        entity.setTratamiento(domain.getTratamiento());
        entity.setContenido(domain.getContenido());
        if (domain.getFechaRegistro() != null) {
            entity.setFechaRegistro(domain.getFechaRegistro());
        }
        return entity;
    }

    public AutorizacionAcceso toAutorizacionDomain(AutorizacionAccesoEntity entity) {
        if (entity == null) return null;
        return new AutorizacionAcceso(
                entity.getId(),
                toHistoriaClinicaDomain(entity.getHistoria()),
                toIpsDomain(entity.getIpsSolicitante()),
                entity.getEstado(),
                entity.getFechaAutorizacion(),
                entity.getFechaRevocacion()
        );
    }

    public AutorizacionAccesoEntity toAutorizacionEntity(AutorizacionAcceso domain) {
        if (domain == null) return null;
        AutorizacionAccesoEntity entity = new AutorizacionAccesoEntity();
        entity.setId(domain.getId());
        entity.setHistoria(toHistoriaClinicaEntity(domain.getHistoria()));
        entity.setIpsSolicitante(toIpsEntity(domain.getIpsSolicitante()));
        entity.setEstado(domain.getEstado());
        if (domain.getFechaAutorizacion() != null) {
            entity.setFechaAutorizacion(domain.getFechaAutorizacion());
        }
        entity.setFechaRevocacion(domain.getFechaRevocacion());
        return entity;
    }

    public Notificacion toNotificacionDomain(NotificacionEntity entity) {
        if (entity == null) return null;
        return new Notificacion(
                entity.getId(),
                toUsuarioDomain(entity.getUsuario()),
                entity.getTitulo(),
                entity.getMensaje(),
                entity.getTipo(),
                entity.getLeida(),
                entity.getFechaEnvio()
        );
    }

    public NotificacionEntity toNotificacionEntity(Notificacion domain) {
        if (domain == null) return null;
        NotificacionEntity entity = new NotificacionEntity();
        entity.setId(domain.getId());
        entity.setUsuario(toUsuarioEntity(domain.getUsuario()));
        entity.setTitulo(domain.getTitulo());
        entity.setMensaje(domain.getMensaje());
        entity.setTipo(domain.getTipo());
        entity.setLeida(domain.isLeida());
        if (domain.getFechaEnvio() != null) {
            entity.setFechaEnvio(domain.getFechaEnvio());
        }
        return entity;
    }

    public Sede toSedeDomain(SedeEntity entity) {
        if (entity == null) return null;
        return new Sede(entity.getId(), toIpsDomain(entity.getIps()), entity.getNombre(), entity.getDireccion());
    }

    public SedeEntity toSedeEntity(Sede domain) {
        if (domain == null) return null;
        SedeEntity entity = new SedeEntity();
        entity.setId(domain.getId());
        entity.setIps(toIpsEntity(domain.getIps()));
        entity.setNombre(domain.getNombre());
        entity.setDireccion(domain.getDireccion());
        return entity;
    }

    public Servicio toServicioDomain(ServicioEntity entity) {
        if (entity == null) return null;
        return new Servicio(entity.getId(), toEspecialidadDomain(entity.getEspecialidad()),
                entity.getNombre(), entity.getTipo());
    }

    public ServicioEntity toServicioEntity(Servicio domain) {
        if (domain == null) return null;
        ServicioEntity entity = new ServicioEntity();
        entity.setId(domain.getId());
        entity.setEspecialidad(toEspecialidadEntity(domain.getEspecialidad()));
        entity.setNombre(domain.getNombre());
        entity.setTipo(domain.getTipo());
        return entity;
    }

    public ServicioMedico toServicioMedicoDomain(ServicioMedicoEntity entity) {
        if (entity == null) return null;
        return new ServicioMedico(entity.getId(), toMedicoDomain(entity.getMedico()),
                toServicioDomain(entity.getServicio()), toSedeDomain(entity.getSede()));
    }

    public ServicioMedicoEntity toServicioMedicoEntity(ServicioMedico domain) {
        if (domain == null) return null;
        ServicioMedicoEntity entity = new ServicioMedicoEntity();
        entity.setId(domain.getId());
        entity.setMedico(toMedicoEntity(domain.getMedico()));
        entity.setServicio(toServicioEntity(domain.getServicio()));
        entity.setSede(toSedeEntity(domain.getSede()));
        return entity;
    }

    public BloqueDisponibilidad toBloqueDomain(BloqueDisponibilidadEntity entity) {
        if (entity == null) return null;
        return new BloqueDisponibilidad(
                entity.getId(),
                toMedicoDomain(entity.getMedico()),
                toServicioDomain(entity.getServicio()),
                toSedeDomain(entity.getSede()),
                entity.getFecha(),
                entity.getHoraInicio(),
                entity.getHoraFin(),
                entity.getDuracionCupoMinutos(),
                entity.getEstado(),
                entity.getFechaCreacion()
        );
    }

    public BloqueDisponibilidadEntity toBloqueEntity(BloqueDisponibilidad domain) {
        if (domain == null) return null;
        BloqueDisponibilidadEntity entity = new BloqueDisponibilidadEntity();
        entity.setId(domain.getId());
        entity.setMedico(toMedicoEntity(domain.getMedico()));
        entity.setServicio(toServicioEntity(domain.getServicio()));
        entity.setSede(toSedeEntity(domain.getSede()));
        entity.setFecha(domain.getFecha());
        entity.setHoraInicio(domain.getHoraInicio());
        entity.setHoraFin(domain.getHoraFin());
        entity.setDuracionCupoMinutos(domain.getDuracionCupoMinutos());
        entity.setEstado(domain.getEstado());
        if (domain.getFechaCreacion() != null) {
            entity.setFechaCreacion(domain.getFechaCreacion());
        }
        return entity;
    }
}
