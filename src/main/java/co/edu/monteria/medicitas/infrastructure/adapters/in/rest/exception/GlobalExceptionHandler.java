package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.exception;

import co.edu.monteria.medicitas.domain.exception.AccesoDenegadoHistoriaException;
import co.edu.monteria.medicitas.domain.exception.AccesoNoAutorizadoException;
import co.edu.monteria.medicitas.domain.exception.AfiliacionNoValidaException;
import co.edu.monteria.medicitas.domain.exception.ConflictoConcurrenciaException;
import co.edu.monteria.medicitas.domain.exception.CredencialesInvalidasException;
import co.edu.monteria.medicitas.domain.exception.CruceHorarioException;
import co.edu.monteria.medicitas.domain.exception.DatosInvalidosException;
import co.edu.monteria.medicitas.domain.exception.RecursoNoEncontradoException;
import co.edu.monteria.medicitas.domain.exception.ReglaNegocioException;
import co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AfiliacionNoValidaException.class)
    public ResponseEntity<ErrorResponseDto> handleAfiliacionNoValida(AfiliacionNoValidaException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Afiliación EPS Bloqueada / No Operativa",
                ex.getMessage(), ex.getMensajeOrientacion());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponseDto> handleDatosInvalidos(DatosInvalidosException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Datos Inválidos", ex.getMessage(), null);
    }

    @ExceptionHandler(AccesoDenegadoHistoriaException.class)
    public ResponseEntity<ErrorResponseDto> handleAccesoDenegadoHistoria(AccesoDenegadoHistoriaException ex) {
        return responder(HttpStatus.FORBIDDEN, "Acceso Restringido a Historia Clínica (Res. 1995/1999)",
                ex.getMessage(), null);
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorResponseDto> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return responder(HttpStatus.FORBIDDEN, "Operación No Autorizada", ex.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccesoDenegadoPorSeguridad(AccessDeniedException ex) {
        // La misma excepción cubre dos situaciones que deben responderse distinto:
        // sin token válido, lo correcto es 401 (hay que autenticarse); con token pero
        // sin el rol necesario, 403 (no hay que autenticarse, falta permiso).
        boolean haySesion = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !(SecurityContextHolder.getContext().getAuthentication()
                        instanceof AnonymousAuthenticationToken);

        if (!haySesion) {
            return responder(HttpStatus.UNAUTHORIZED, "No Autenticado",
                    "Debe iniciar sesión para acceder a este recurso.", null);
        }
        return responder(HttpStatus.FORBIDDEN, "Operación No Autorizada",
                "Su rol no tiene permisos para realizar esta operación.", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleNoAutenticado(AuthenticationException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "No Autenticado",
                "Credenciales o token de sesión inválidos o vencidos.", null);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponseDto> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "Credenciales Inválidas", ex.getMessage(), null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDto> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, "Recurso No Encontrado", ex.getMessage(), null);
    }

    /**
     * Cruce de horarios al publicar disponibilidad. Es 409 y no 422 para que la app lo
     * distinga de una validación cualquiera y muestre la pantalla específica del cruce.
     */
    @ExceptionHandler(CruceHorarioException.class)
    public ResponseEntity<ErrorResponseDto> handleCruceHorario(CruceHorarioException ex) {
        return responder(HttpStatus.CONFLICT, "Cruce de Horarios", ex.getMessage(), null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponseDto> handleReglaNegocio(ReglaNegocioException ex) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, "Error en Regla de Negocio", ex.getMessage(), null);
    }

    /**
     * Conflicto de concurrencia ya detectado por la aplicación (transición condicionada
     * fallida) o por la base de datos (bloqueo optimista o restricción única violada).
     * Se responde 409 para que el cliente pueda reintentar sin tratarlo como fallo del
     * servidor.
     */
    @ExceptionHandler(ConflictoConcurrenciaException.class)
    public ResponseEntity<ErrorResponseDto> handleConflictoConcurrencia(ConflictoConcurrenciaException ex) {
        return responder(HttpStatus.CONFLICT, "Conflicto de Concurrencia", ex.getMessage(), null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDto> handleOptimisticLock(OptimisticLockingFailureException ex) {
        return responder(HttpStatus.CONFLICT, "Conflicto de Concurrencia",
                "El recurso fue modificado por otra operación simultánea. Por favor intente de nuevo.", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleIntegridadDatos(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "Conflicto de Datos",
                "La operación no se completó porque el registro ya existe o viola una restricción del catálogo.", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
        return responder(HttpStatus.BAD_REQUEST, "Error de Validación",
                detalle.isEmpty() ? "Datos de solicitud no válidos" : detalle, null);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponseDto> handleParametrosInvalidos(Exception ex) {
        return responder(HttpStatus.BAD_REQUEST, "Parámetros Inválidos", ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        // Se registra la excepción completa en el servidor, pero la respuesta al cliente
        // es genérica: los trazas pueden revelar estructura interna y datos del negocio.
        log.error("Error no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno del Servidor",
                "Ocurrió un error inesperado al procesar su solicitud. Por favor intente más tarde.", null);
    }

    private ResponseEntity<ErrorResponseDto> responder(HttpStatus status, String error,
                                                        String mensaje, String orientacion) {
        return ResponseEntity.status(status)
                .body(new ErrorResponseDto(status.value(), error, mensaje, orientacion));
    }
}
