package co.edu.monteria.medicitas.infrastructure.adapters.in.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TODO: (Prototipo Funcional Inicial - Tutoría 5)
 * Controlador REST para el "CRUD básico de categorías de incidencias".
 * 
 * Siguientes pasos para trabajar:
 * 1. Crear la entidad JPA `CategoriaIncidenciaEntity` en el paquete `infrastructure/adapters/out/persistence/entity`.
 * 2. Crear la interfaz Repositorio Spring Data `CategoriaIncidenciaRepository`.
 * 3. Crear el caso de uso (Puerto de entrada) en `application/ports/in`.
 * 4. Implementar la lógica en un servicio (ej: `IncidenciaService`) dentro de `application/service`.
 * 5. Inyectar el servicio aquí y exponer los endpoints (POST /incidencias/categorias, GET, PUT, DELETE).
 */
@RestController
@RequestMapping("/api/v1/incidencias/categorias")
public class IncidenciaController {

    // TODO: @Autowired private IncidenciaUseCase incidenciaUseCase;

    /**
     * TODO: Implementar GET para listar las categorías de incidencias.
     * Ejemplo de anotación: @GetMapping
     */
    @GetMapping
    public ResponseEntity<?> listarCategorias() {
        // Implementación pendiente
        return ResponseEntity.ok("TODO: Listado de categorías de incidencias");
    }

    /**
     * TODO: Implementar POST para crear nueva categoría de incidencia.
     */
    @PostMapping
    public ResponseEntity<?> crearCategoria() {
        // Implementación pendiente
        return ResponseEntity.ok("TODO: Crear categoría de incidencia");
    }

    /**
     * TODO: Implementar PUT para actualizar una categoría.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCategoria(@PathVariable Long id) {
        // Implementación pendiente
        return ResponseEntity.ok("TODO: Actualizar categoría " + id);
    }

    /**
     * TODO: Implementar DELETE para eliminar una categoría.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Long id) {
        // Implementación pendiente
        return ResponseEntity.ok("TODO: Eliminar categoría " + id);
    }
}
