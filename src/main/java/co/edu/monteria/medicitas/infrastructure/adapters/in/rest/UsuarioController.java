package co.edu.monteria.medicitas.infrastructure.adapters.in.rest;

import co.edu.monteria.medicitas.application.ports.in.PerfilUseCase;
import co.edu.monteria.medicitas.application.ports.out.ContextoUsuarioPort;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.ActualizarPerfilRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.CambiarContrasenaRequest;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Perfil del usuario autenticado. Siempre opera sobre "me" (el usuario del token), nunca
 * sobre un id recibido en la URL.
 */
@RestController
@RequestMapping("/usuarios/me")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Perfil", description = "Datos de la cuenta del usuario autenticado")
public class UsuarioController {

    private final PerfilUseCase perfilUseCase;
    private final ContextoUsuarioPort contextoUsuario;

    public UsuarioController(PerfilUseCase perfilUseCase, ContextoUsuarioPort contextoUsuario) {
        this.perfilUseCase = perfilUseCase;
        this.contextoUsuario = contextoUsuario;
    }

    @GetMapping
    @Operation(summary = "Consultar mi perfil")
    public ResponseEntity<UsuarioResponse> obtenerPerfil() {
        return ResponseEntity.ok(UsuarioResponse.desde(
                perfilUseCase.obtenerPerfil(contextoUsuario.usuarioActual().getId())));
    }

    @PutMapping
    @Operation(summary = "Actualizar mis datos de contacto",
            description = "Solo correo y teléfono; nombre y documento vienen de la afiliación")
    public ResponseEntity<UsuarioResponse> actualizarContacto(@Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(UsuarioResponse.desde(perfilUseCase.actualizarContacto(
                contextoUsuario.usuarioActual().getId(), request.getEmail(), request.getTelefono())));
    }

    @PutMapping("/contrasena")
    @Operation(summary = "Cambiar mi contraseña",
            description = "400 si la contraseña actual no coincide o la nueva no cumple los requisitos")
    public ResponseEntity<Void> cambiarContrasena(@Valid @RequestBody CambiarContrasenaRequest request) {
        perfilUseCase.cambiarContrasena(
                contextoUsuario.usuarioActual().getId(), request.getActual(), request.getNueva());
        return ResponseEntity.noContent().build();
    }
}
