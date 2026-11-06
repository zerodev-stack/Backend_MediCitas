package co.edu.monteria.medicitas.infrastructure.adapters.in.rest;

import co.edu.monteria.medicitas.application.ports.in.GestionarBloquesDisponibilidadUseCase;
import co.edu.monteria.medicitas.application.ports.in.GestionarBloquesDisponibilidadUseCase.SolicitudBloque;
import co.edu.monteria.medicitas.application.ports.in.GestionarColaMedicoUseCase;
import co.edu.monteria.medicitas.application.ports.in.GestionarPerfilMedicoUseCase;
import co.edu.monteria.medicitas.application.ports.out.ContextoUsuarioPort;
import co.edu.monteria.medicitas.domain.model.Medico;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.ActualizarPerfilMedicoRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.BloqueRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.BloqueResponse;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.CatalogoServiciosMedicoResponse;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.CitaResponse;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.ColaEsperaResponse;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.ConfigurarServiciosRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.DisponibilidadResponse;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.PerfilMedicoResponse;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.SedeResponse;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.ServicioMedicoResponse;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.ServicioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Endpoints del médico autenticado: perfil, servicios y sedes, bloques de disponibilidad y
 * cola de espera.
 *
 * Todo cuelga de '/medicos/me' y ningún endpoint recibe el id del médico: se toma del
 * token, de modo que un médico no puede leer ni modificar la agenda de otro.
 */
@RestController
@RequestMapping("/medicos/me")
@PreAuthorize("hasRole('MEDICO')")
@Tag(name = "Médico", description = "Perfil, servicios y sedes, bloques de disponibilidad y cola de espera del médico autenticado")
public class MedicoController {

    private final GestionarPerfilMedicoUseCase perfilUseCase;
    private final GestionarBloquesDisponibilidadUseCase bloquesUseCase;
    private final GestionarColaMedicoUseCase colaUseCase;
    private final ContextoUsuarioPort contextoUsuario;

    public MedicoController(GestionarPerfilMedicoUseCase perfilUseCase,
                            GestionarBloquesDisponibilidadUseCase bloquesUseCase,
                            GestionarColaMedicoUseCase colaUseCase,
                            ContextoUsuarioPort contextoUsuario) {
        this.perfilUseCase = perfilUseCase;
        this.bloquesUseCase = bloquesUseCase;
        this.colaUseCase = colaUseCase;
        this.contextoUsuario = contextoUsuario;
    }

    // ------------------------------------------------------------------
    // Perfil
    // ------------------------------------------------------------------

    @GetMapping
    @Operation(summary = "Consultar mi perfil profesional")
    public ResponseEntity<PerfilMedicoResponse> obtenerPerfil() {
        return ResponseEntity.ok(perfil(perfilUseCase.obtenerPerfil(usuarioId())));
    }

    @PutMapping
    @Operation(summary = "Actualizar mis datos de contacto",
            description = "Solo ciudad de atención, correo y teléfono de contacto. Nombre, especialidad "
                    + "e institución los gestiona la institución.")
    public ResponseEntity<PerfilMedicoResponse> actualizarPerfil(@Valid @RequestBody ActualizarPerfilMedicoRequest request) {
        Medico medico = perfilUseCase.actualizarDatosContacto(usuarioId(),
                request.ciudadAtencion(), request.correoContacto(), request.telefonoContacto());
        return ResponseEntity.ok(perfil(medico));
    }

    // ------------------------------------------------------------------
    // Servicios y sedes
    // ------------------------------------------------------------------

    @GetMapping("/servicios")
    @Operation(summary = "Catálogo de servicios y sedes",
            description = "Servicios de mi especialidad, sedes de mi institución y los que ya tengo habilitados")
    public ResponseEntity<CatalogoServiciosMedicoResponse> obtenerCatalogo() {
        Long usuarioId = usuarioId();
        return ResponseEntity.ok(new CatalogoServiciosMedicoResponse(
                perfilUseCase.serviciosDeMiEspecialidad(usuarioId).stream().map(ServicioResponse::desde).toList(),
                perfilUseCase.sedesDeMiInstitucion(usuarioId).stream().map(SedeResponse::desde).toList(),
                perfilUseCase.obtenerServiciosConfigurados(usuarioId).stream().map(ServicioMedicoResponse::desde).toList()
        ));
    }

    @PutMapping("/servicios")
    @Operation(summary = "Configurar mis servicios y sedes",
            description = "Reemplaza la configuración completa. Cada servicio debe llevar al menos una sede.")
    public ResponseEntity<List<ServicioMedicoResponse>> configurarServicios(
            @Valid @RequestBody ConfigurarServiciosRequest request) {
        Map<Long, Set<Long>> sedesPorServicio = new LinkedHashMap<>();
        for (ConfigurarServiciosRequest.ServicioConSedes item : request.servicios()) {
            sedesPorServicio.computeIfAbsent(item.servicioId(), k -> new LinkedHashSet<>())
                    .addAll(item.sedeIds() == null ? List.of() : item.sedeIds());
        }
        return ResponseEntity.ok(perfilUseCase.configurarServicios(usuarioId(), sedesPorServicio)
                .stream().map(ServicioMedicoResponse::desde).toList());
    }

    // ------------------------------------------------------------------
    // Bloques de disponibilidad
    // ------------------------------------------------------------------

    @GetMapping("/bloques")
    @Operation(summary = "Mis bloques de disponibilidad", description = "Bloques activos de hoy en adelante")
    public ResponseEntity<List<BloqueResponse>> listarBloques() {
        return ResponseEntity.ok(bloquesUseCase.listarBloques(usuarioId()).stream().map(BloqueResponse::desde).toList());
    }

    @GetMapping("/bloques/{id}")
    @Operation(summary = "Detalle de un bloque", description = "Incluye las citas vigentes que ocupan sus cupos")
    public ResponseEntity<BloqueResponse> obtenerBloque(@PathVariable Long id) {
        return ResponseEntity.ok(BloqueResponse.desde(bloquesUseCase.obtenerBloque(usuarioId(), id)));
    }

    @PostMapping("/bloques")
    @Operation(summary = "Publicar disponibilidad",
            description = "Crea un bloque por cada fecha y lo divide en cupos. Si hay pacientes en la cola FIFO "
                    + "de mi especialidad e IPS, los cupos nuevos se les asignan automáticamente. Responde 409 "
                    + "si el horario se cruza con otro bloque o franja propia.")
    public ResponseEntity<List<BloqueResponse>> crearBloques(@Valid @RequestBody BloqueRequest request) {
        List<BloqueResponse> creados = bloquesUseCase.crearBloques(usuarioId(), solicitud(request))
                .stream().map(BloqueResponse::desde).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(creados);
    }

    @PutMapping("/bloques/{id}")
    @Operation(summary = "Editar un bloque sin reservas",
            description = "Sustituye el bloque por la nueva definición (puede abarcar varias fechas)")
    public ResponseEntity<List<BloqueResponse>> editarBloque(@PathVariable Long id,
                                                             @Valid @RequestBody BloqueRequest request) {
        return ResponseEntity.ok(bloquesUseCase.reemplazarBloque(usuarioId(), id, solicitud(request))
                .stream().map(BloqueResponse::desde).toList());
    }

    @PostMapping("/bloques/{id}/cancelar")
    @Operation(summary = "Cancelar un bloque",
            description = "Solo si no tiene citas reservadas; en ese caso responde 422 y las reservas se conservan")
    public ResponseEntity<BloqueResponse> cancelarBloque(@PathVariable Long id) {
        return ResponseEntity.ok(BloqueResponse.desde(bloquesUseCase.cancelarBloque(usuarioId(), id)));
    }

    // ------------------------------------------------------------------
    // Cola de espera
    // ------------------------------------------------------------------

    @GetMapping("/cola-espera")
    @Operation(summary = "Pacientes esperando cupo", description = "Solicitudes EN_ESPERA de mi especialidad e IPS, en orden FIFO")
    public ResponseEntity<List<ColaEsperaResponse>> obtenerCola() {
        return ResponseEntity.ok(colaUseCase.obtenerColaDelMedico(usuarioId())
                .stream().map(ColaEsperaResponse::desde).toList());
    }

    @GetMapping("/cola-espera/{id}")
    @Operation(summary = "Detalle de una solicitud de la cola")
    public ResponseEntity<ColaEsperaResponse> obtenerSolicitud(@PathVariable Long id) {
        return ResponseEntity.ok(ColaEsperaResponse.desde(colaUseCase.obtenerSolicitud(usuarioId(), id)));
    }

    @GetMapping("/cola-espera/{id}/cupos-compatibles")
    @Operation(summary = "Cupos que puedo asignar a la solicitud", description = "Mis cupos libres y futuros")
    public ResponseEntity<List<DisponibilidadResponse>> cuposCompatibles(@PathVariable Long id) {
        return ResponseEntity.ok(colaUseCase.cuposCompatibles(usuarioId(), id)
                .stream().map(DisponibilidadResponse::desde).toList());
    }

    @PostMapping("/cola-espera/{id}/asignar")
    @Operation(summary = "Asignar un cupo a un paciente de la cola",
            description = "Registra la cita, saca la solicitud de la cola y notifica al paciente")
    public ResponseEntity<CitaResponse> asignarCupo(@PathVariable Long id, @RequestParam Long disponibilidadId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CitaResponse.desde(colaUseCase.asignarCupo(usuarioId(), id, disponibilidadId)));
    }

    private PerfilMedicoResponse perfil(Medico medico) {
        return PerfilMedicoResponse.desde(medico, perfilUseCase.obtenerServiciosConfigurados(usuarioId()));
    }

    private static SolicitudBloque solicitud(BloqueRequest request) {
        return new SolicitudBloque(request.servicioId(), request.sedeId(), request.fechas(),
                request.horaInicio(), request.horaFin(), request.duracionCupoMinutos());
    }

    private Long usuarioId() {
        return contextoUsuario.usuarioActual().getId();
    }
}
