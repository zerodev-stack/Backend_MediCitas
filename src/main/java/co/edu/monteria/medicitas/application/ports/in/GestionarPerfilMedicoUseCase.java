package co.edu.monteria.medicitas.application.ports.in;

import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.domain.model.Sede;
import co.edu.monteria.medicitas.domain.model.Servicio;
import co.edu.monteria.medicitas.domain.model.ServicioMedico;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Perfil del médico autenticado y su configuración de servicios y sedes.
 *
 * Todos los métodos reciben el id de USUARIO del token, nunca el id del médico: así un
 * médico solo puede leer o modificar su propio perfil.
 */
public interface GestionarPerfilMedicoUseCase {

    Medico obtenerPerfil(Long medicoUsuarioId);

    /**
     * Actualiza los únicos datos que el médico puede cambiar por sí mismo. Nombre,
     * especialidad e institución los gestiona la institución.
     */
    Medico actualizarDatosContacto(Long medicoUsuarioId, String ciudadAtencion,
                                   String correoContacto, String telefonoContacto);

    /** Servicios del catálogo de la especialidad del médico. */
    List<Servicio> serviciosDeMiEspecialidad(Long medicoUsuarioId);

    /** Sedes de la IPS a la que está adscrito el médico. */
    List<Sede> sedesDeMiInstitucion(Long medicoUsuarioId);

    List<ServicioMedico> obtenerServiciosConfigurados(Long medicoUsuarioId);

    /**
     * Reemplaza la configuración completa: para cada servicio elegido, las sedes donde lo
     * ofrece. Un servicio sin sedes es un error de validación, no una forma de quitarlo.
     */
    List<ServicioMedico> configurarServicios(Long medicoUsuarioId, Map<Long, Set<Long>> sedesPorServicio);
}
