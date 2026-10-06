package co.edu.monteria.medicitas.infrastructure.adapters.in.rest;

import co.edu.monteria.medicitas.application.ports.in.AgendarCitaUseCase;
import co.edu.monteria.medicitas.application.ports.out.ContextoUsuarioPort;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.CitaRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.CitaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de agendamiento.
 *
 * NINGÚN endpoint acepta el identificador del paciente como parámetro: se toma siempre del
 * token JWT mediante 'ContextoUsuarioPort'. Antes, un paciente podía cancelar o aplazar
 * citas de otro simplemente cambiando el 'usuarioId' de la URL.
 */
@RestController
@RequestMapping("/citas")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Citas Médicas", description = "Agendamiento, cancelación y aplazamiento con reasignación automática FIFO")
public class CitaController {

    private final AgendarCitaUseCase agendarCitaUseCase;
    private final ContextoUsuarioPort contextoUsuario;

    public CitaController(AgendarCitaUseCase agendarCitaUseCase, ContextoUsuarioPort contextoUsuario) {
        this.agendarCitaUseCase = agendarCitaUseCase;
        this.contextoUsuario = contextoUsuario;
    }

    @PostMapping("/agendar")
    @PreAuthorize("hasRole('PACIENTE')")
    @Operation(summary = "Agendar Cita",
            description = "Reserva una franja libre para el paciente autenticado")
    public ResponseEntity<CitaResponse> agendarCita(@Valid @RequestBody CitaRequest request) {
        CitaResponse cita = CitaResponse.desde(agendarCitaUseCase.agendarCita(
                contextoUsuario.usuarioActual().getId(),
                request.getDisponibilidadId(),
                request.getTipo(),
                request.getObservaciones()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(cita);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('PACIENTE')")
    @Operation(summary = "Cancelar Cita",
            description = "Cancela una cita propia. Al cancelarse se libera el cupo y se reasigna "
                    + "automáticamente al primer paciente de la cola FIFO de esa especialidad e IPS.")
    public ResponseEntity<CitaResponse> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(CitaResponse.desde(
                agendarCitaUseCase.cancelarCita(id, contextoUsuario.usuarioActual().getId())));
    }

    @PostMapping("/{id}/aplazar")
    @PreAuthorize("hasRole('PACIENTE')")
    @Operation(summary = "Aplazar/Reagendar Cita",
            description = "Traslada una cita propia a una nueva franja libre de la misma especialidad e IPS, "
                    + "y reasigna la franja liberada a la cola FIFO.")
    public ResponseEntity<CitaResponse> aplazarCita(
            @PathVariable Long id,
            @RequestParam Long nuevaDisponibilidadId) {
        return ResponseEntity.ok(CitaResponse.desde(
                agendarCitaUseCase.aplazarCita(id, nuevaDisponibilidadId, contextoUsuario.usuarioActual().getId())));
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('PACIENTE')")
    @Operation(summary = "Consultar mis citas", description = "Historial y estado de las citas del paciente autenticado")
    public ResponseEntity<List<CitaResponse>> obtenerMisCitas() {
        List<CitaResponse> citas = agendarCitaUseCase.obtenerCitasPaciente(contextoUsuario.usuarioActual().getId())
                .stream().map(CitaResponse::desde).toList();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/agenda")
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Consultar mi agenda", description = "Citas programadas del médico autenticado")
    public ResponseEntity<List<CitaResponse>> obtenerMiAgenda() {
        List<CitaResponse> citas = agendarCitaUseCase.obtenerCitasMedico(contextoUsuario.usuarioActual().getId())
                .stream().map(CitaResponse::desde).toList();
        return ResponseEntity.ok(citas);
    }
}
