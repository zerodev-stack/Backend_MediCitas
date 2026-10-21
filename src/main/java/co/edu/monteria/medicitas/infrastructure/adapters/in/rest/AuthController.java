package co.edu.monteria.medicitas.infrastructure.adapters.in.rest;

import co.edu.monteria.medicitas.application.ports.in.AutenticacionUseCase;
import co.edu.monteria.medicitas.domain.model.ResultadoAfiliacion;
import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.TipoDocumento;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.AuthRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.AuthResponse;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.RegistroMedicoRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.RegistroPacienteRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación y Registro", description = "Endpoints para inicio de sesión y registro con validación BDUA/EPS")
public class AuthController {

    private final AutenticacionUseCase autenticacionUseCase;

    public AuthController(AutenticacionUseCase autenticacionUseCase) {
        this.autenticacionUseCase = autenticacionUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicio de sesión",
            description = "Verifica las credenciales y retorna el usuario junto con su token Bearer JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        // La app móvil envía numDocumento; la web y las pruebas siguen usando email.
        Usuario usuario = request.getNumDocumento() != null && !request.getNumDocumento().isBlank()
                ? autenticacionUseCase.autenticarPorDocumento(request.getNumDocumento().trim(), request.getPassword())
                : autenticacionUseCase.autenticar(request.getEmail(), request.getPassword());
        String token = autenticacionUseCase.generarToken(usuario);
        return ResponseEntity.ok(AuthResponse.de(usuario, token));
    }

    @GetMapping("/validar-bdua")
    @Operation(summary = "Verificar afiliación BDUA",
            description = "Consulta el adaptador simulado de la BDUA para validar la EPS antes de registrarse")
    public ResponseEntity<ResultadoAfiliacion> validarBdua(
            @RequestParam TipoDocumento tipoDocumento,
            @RequestParam String numDocumento) {
        ResultadoAfiliacion res = autenticacionUseCase.validarAfiliacionBdua(tipoDocumento, numDocumento);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/registro-paciente")
    @Operation(summary = "Registrar Paciente",
            description = "Valida la afiliación BDUA (ej. EPS Mutualser). Si la EPS no está en la red de Montería, "
                    + "bloquea el registro y devuelve la orientación correspondiente.")
    public ResponseEntity<UsuarioResponse> registrarPaciente(@Valid @RequestBody RegistroPacienteRequest request) {
        Usuario paciente = autenticacionUseCase.registrarPaciente(
                request.getNumDocumento(),
                request.getTipoDocumento(),
                request.getNombreCompleto(),
                request.getEmail(),
                request.getPassword(),
                request.getTelefono()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(paciente));
    }

    @PostMapping("/registro-medico")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar Médico",
            description = "Alta de un profesional de la salud adscrito a una especialidad e IPS. "
                    + "Operación reservada al administrador de la plataforma.")
    public ResponseEntity<UsuarioResponse> registrarMedico(@Valid @RequestBody RegistroMedicoRequest request) {
        Usuario medico = autenticacionUseCase.registrarMedico(
                request.getNumDocumento(),
                request.getTipoDocumento(),
                request.getNombreCompleto(),
                request.getEmail(),
                request.getPassword(),
                request.getTelefono(),
                request.getEspecialidadId(),
                request.getIpsId(),
                request.getRegistroMedico()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(medico));
    }
}
