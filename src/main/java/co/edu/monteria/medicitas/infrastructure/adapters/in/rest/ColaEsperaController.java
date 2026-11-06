package co.edu.monteria.medicitas.infrastructure.adapters.in.rest;

import co.edu.monteria.medicitas.application.ports.in.GestionarColaEsperaUseCase;
import co.edu.monteria.medicitas.application.ports.out.ContextoUsuarioPort;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.ColaEsperaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colas-espera")
@PreAuthorize("hasRole('PACIENTE')")
@Tag(name = "Cola de Espera FIFO",
        description = "Registro y gestión en cola de espera cuando no hay cupos disponibles")
public class ColaEsperaController {

    private final GestionarColaEsperaUseCase gestionarColaEsperaUseCase;
    private final ContextoUsuarioPort contextoUsuario;

    public ColaEsperaController(GestionarColaEsperaUseCase gestionarColaEsperaUseCase,
                                ContextoUsuarioPort contextoUsuario) {
        this.gestionarColaEsperaUseCase = gestionarColaEsperaUseCase;
        this.contextoUsuario = contextoUsuario;
    }

    @PostMapping("/unirse")
    @Operation(summary = "Unirse a Cola de Espera FIFO",
            description = "Registra al paciente autenticado en la cola de una especialidad e IPS para "
                    + "asignación automática. Servicio y sede son opcionales: si se indican, solo se asignan cupos que "
                    + "coincidan con ellos.")
    public ResponseEntity<ColaEsperaResponse> unirseAColaEspera(
            @RequestParam Long especialidadId,
            @RequestParam Long ipsId,
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Long sedeId) {
        ColaEsperaResponse enEspera = ColaEsperaResponse.desde(
                gestionarColaEsperaUseCase.unirseAColaEspera(
                        contextoUsuario.usuarioActual().getId(), especialidadId, ipsId, servicioId, sedeId));
        return ResponseEntity.status(HttpStatus.CREATED).body(enEspera);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Retirarse de la Cola de Espera", description = "Cancela una solicitud propia en cola")
    public ResponseEntity<Void> cancelarColaEspera(@PathVariable Long id) {
        gestionarColaEsperaUseCase.cancelarColaEspera(id, contextoUsuario.usuarioActual().getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mias")
    @Operation(summary = "Consultar mis colas de espera",
            description = "Solicitudes activas o procesadas del paciente autenticado")
    public ResponseEntity<List<ColaEsperaResponse>> obtenerMisColas() {
        List<ColaEsperaResponse> colas = gestionarColaEsperaUseCase
                .obtenerColasPaciente(contextoUsuario.usuarioActual().getId())
                .stream().map(ColaEsperaResponse::desde).toList();
        return ResponseEntity.ok(colas);
    }
}
