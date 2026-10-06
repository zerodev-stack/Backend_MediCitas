package co.edu.monteria.medicitas.infrastructure.adapters.in.rest;

import co.edu.monteria.medicitas.application.ports.in.NotificacionUseCase;
import co.edu.monteria.medicitas.application.ports.out.ContextoUsuarioPort;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.NotificacionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Notificaciones", description = "Bandeja de entrada de alertas Push/Email del sistema")
public class NotificacionController {

    private final NotificacionUseCase notificacionUseCase;
    private final ContextoUsuarioPort contextoUsuario;

    public NotificacionController(NotificacionUseCase notificacionUseCase, ContextoUsuarioPort contextoUsuario) {
        this.notificacionUseCase = notificacionUseCase;
        this.contextoUsuario = contextoUsuario;
    }

    @GetMapping
    @Operation(summary = "Consultar Notificaciones",
            description = "Alertas recibidas por el usuario autenticado: confirmaciones, asignaciones "
                    + "automáticas por cola FIFO y cambios de autorización.")
    public ResponseEntity<List<NotificacionResponse>> obtenerNotificaciones() {
        List<NotificacionResponse> notificaciones = notificacionUseCase
                .obtenerNotificacionesUsuario(contextoUsuario.usuarioActual().getId())
                .stream().map(NotificacionResponse::desde).toList();
        return ResponseEntity.ok(notificaciones);
    }

    @PutMapping("/{id}/marcar-leida")
    @Operation(summary = "Marcar notificación como leída",
            description = "Actualiza el estado de lectura de una notificación propia")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        notificacionUseCase.marcarComoLeida(id, contextoUsuario.usuarioActual().getId());
        return ResponseEntity.noContent().build();
    }
}
