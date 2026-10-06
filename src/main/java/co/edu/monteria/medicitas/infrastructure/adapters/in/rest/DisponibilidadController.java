package co.edu.monteria.medicitas.infrastructure.adapters.in.rest;

import co.edu.monteria.medicitas.application.ports.in.GestionarDisponibilidadUseCase;
import co.edu.monteria.medicitas.application.ports.out.ContextoUsuarioPort;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.DisponibilidadRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.DisponibilidadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/disponibilidades")
@Tag(name = "Disponibilidad Médica", description = "Consulta y registro de franjas horarias médicas")
public class DisponibilidadController {

    private final GestionarDisponibilidadUseCase gestionarDisponibilidadUseCase;
    private final ContextoUsuarioPort contextoUsuario;

    public DisponibilidadController(GestionarDisponibilidadUseCase gestionarDisponibilidadUseCase,
                                    ContextoUsuarioPort contextoUsuario) {
        this.gestionarDisponibilidadUseCase = gestionarDisponibilidadUseCase;
        this.contextoUsuario = contextoUsuario;
    }

    @GetMapping
    @Operation(summary = "Consultar Disponibilidades Libres",
            description = "Filtra franjas libres por especialidad, IPS y fecha opcional. Es público: "
                    + "un paciente que aún no tiene sesión puede explorar la red antes de registrarse.")
    public ResponseEntity<List<DisponibilidadResponse>> buscarDisponibles(
            @RequestParam Long especialidadId,
            @RequestParam Long ipsId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<DisponibilidadResponse> libres = gestionarDisponibilidadUseCase
                .buscarDisponibilidadesLibres(especialidadId, ipsId, fecha)
                .stream().map(DisponibilidadResponse::desde).toList();
        return ResponseEntity.ok(libres);
    }

    @PostMapping
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Registrar Franja de Horario",
            description = "El médico autenticado abre un nuevo bloque de atención. Si hay pacientes en la "
                    + "cola FIFO de esa especialidad e IPS, el cupo se les asigna automáticamente.")
    public ResponseEntity<DisponibilidadResponse> crearDisponibilidad(@Valid @RequestBody DisponibilidadRequest request) {
        DisponibilidadResponse creada = DisponibilidadResponse.desde(
                gestionarDisponibilidadUseCase.crearDisponibilidad(
                        contextoUsuario.usuarioActual().getId(),
                        request.getFecha(),
                        request.getHoraInicio(),
                        request.getHoraFin()
                ));
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('MEDICO')")
    @Operation(summary = "Consultar mis disponibilidades",
            description = "Bloques de horario registrados por el médico autenticado")
    public ResponseEntity<List<DisponibilidadResponse>> obtenerMisDisponibilidades() {
        List<DisponibilidadResponse> disponibilidades = gestionarDisponibilidadUseCase
                .obtenerDisponibilidadesMedico(contextoUsuario.usuarioActual().getId())
                .stream().map(DisponibilidadResponse::desde).toList();
        return ResponseEntity.ok(disponibilidades);
    }
}
