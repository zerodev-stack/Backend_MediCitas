package co.edu.monteria.medicitas.infrastructure.adapters.in.rest;

import co.edu.monteria.medicitas.application.ports.in.HistoriaClinicaUseCase;
import co.edu.monteria.medicitas.application.ports.out.ContextoUsuarioPort;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.AutorizacionAccesoResponse;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.HistoriaClinicaRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.HistoriaClinicaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de historia clínica y consentimiento entre IPS.
 *
 * El lado del PACIENTE (leer lo propio, autorizar y revocar) no acepta identificadores del
 * paciente: se toman del token. El lado del MÉDICO sí recibe el id del paciente que
 * atiende, porque esa es su función, pero debe acreditar su IPS y solo alcanza los
 * registros que la Res. 1995/1999 le permiten.
 */
@RestController
@RequestMapping("/historias-clinicas")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Historia Clínica y Consentimiento",
        description = "Consulta de historia clínica y autorización de acceso entre IPS (Res. 1995/1999 y 3280/2018)")
public class HistoriaClinicaController {

    private final HistoriaClinicaUseCase historiaClinicaUseCase;
    private final ContextoUsuarioPort contextoUsuario;

    public HistoriaClinicaController(HistoriaClinicaUseCase historiaClinicaUseCase,
                                     ContextoUsuarioPort contextoUsuario) {
        this.historiaClinicaUseCase = historiaClinicaUseCase;
        this.contextoUsuario = contextoUsuario;
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('PACIENTE')")
    @Operation(summary = "Consultar mi historia clínica",
            description = "Registros clínicos generados bajo custodia de las IPS que me brindaron atención")
    public ResponseEntity<List<HistoriaClinicaResponse>> consultarMiHistoriaClinica() {
        List<HistoriaClinicaResponse> historias = historiaClinicaUseCase
                .consultarHistoriasPaciente(contextoUsuario.usuarioActual().getId())
                .stream().map(HistoriaClinicaResponse::desde).toList();
        return ResponseEntity.ok(historias);
    }

    @GetMapping("/autorizaciones/mias")
    @PreAuthorize("hasRole('PACIENTE')")
    @Operation(summary = "Consultar mis autorizaciones",
            description = "Qué IPS tienen permisos HABILITADOS o REVOCADOS sobre mis registros clínicos")
    public ResponseEntity<List<AutorizacionAccesoResponse>> obtenerMisAutorizaciones() {
        List<AutorizacionAccesoResponse> autorizaciones = historiaClinicaUseCase
                .obtenerAutorizacionesPaciente(contextoUsuario.usuarioActual().getId())
                .stream().map(AutorizacionAccesoResponse::desde).toList();
        return ResponseEntity.ok(autorizaciones);
    }

    @PostMapping("/{historiaId}/autorizar")
    @PreAuthorize("hasRole('PACIENTE')")
    @Operation(summary = "Autorizar acceso a IPS solicitante",
            description = "Otorga consentimiento informado para que una IPS consulte un registro propio")
    public ResponseEntity<AutorizacionAccesoResponse> autorizarAcceso(
            @PathVariable Long historiaId,
            @RequestParam Long ipsSolicitanteId) {
        AutorizacionAccesoResponse autorizada = AutorizacionAccesoResponse.desde(
                historiaClinicaUseCase.autorizarAccesoIps(
                        historiaId, ipsSolicitanteId, contextoUsuario.usuarioActual().getId()));
        return ResponseEntity.ok(autorizada);
    }

    @PostMapping("/{historiaId}/revocar")
    @PreAuthorize("hasRole('PACIENTE')")
    @Operation(summary = "Revocar acceso a IPS solicitante",
            description = "Retira de inmediato la autorización concedida a una IPS solicitante")
    public ResponseEntity<AutorizacionAccesoResponse> revocarAcceso(
            @PathVariable Long historiaId,
            @RequestParam Long ipsSolicitanteId) {
        AutorizacionAccesoResponse revocada = AutorizacionAccesoResponse.desde(
                historiaClinicaUseCase.revocarAccesoIps(
                        historiaId, ipsSolicitanteId, contextoUsuario.usuarioActual().getId()));
        return ResponseEntity.ok(revocada);
    }

    @GetMapping("/paciente/{pacienteId}/desde-mi-ips")
    @PreAuthorize("hasRole('MEDICO') or hasRole('ADMIN')")
    @Operation(summary = "Consultar historia clínica desde mi IPS",
            description = "Acceso del profesional a los registros de un paciente. Se filtran por la autorización "
                    + "vigente del paciente (Res. 1995/1999); si la IPS no tiene acceso a ningún registro, "
                    + "se responde 403.")
    public ResponseEntity<List<HistoriaClinicaResponse>> consultarDesdeMiIps(
            @PathVariable Long pacienteId,
            @RequestParam Long ipsSolicitanteId) {
        List<HistoriaClinicaResponse> historias = historiaClinicaUseCase
                .consultarHistoriasDesdeIps(contextoUsuario.usuarioActual().getId(), pacienteId, ipsSolicitanteId)
                .stream().map(HistoriaClinicaResponse::desde).toList();
        return ResponseEntity.ok(historias);
    }

    @PostMapping
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Registrar atención clínica",
            description = "El médico autenticado registra una atención. La custodia del registro se asigna "
                    + "automáticamente a su propia IPS.")
    public ResponseEntity<HistoriaClinicaResponse> registrarHistoria(@Valid @RequestBody HistoriaClinicaRequest request) {
        HistoriaClinicaResponse historia = HistoriaClinicaResponse.desde(
                historiaClinicaUseCase.registrarHistoriaClinica(
                        contextoUsuario.usuarioActual().getId(),
                        request.getPacienteId(),
                        request.getDiagnostico(),
                        request.getTratamiento(),
                        request.getContenido()
                ));
        return ResponseEntity.status(HttpStatus.CREATED).body(historia);
    }
}
