package co.edu.monteria.medicitas.infrastructure.adapters.in.rest;

import co.edu.monteria.medicitas.application.ports.in.ConsultarEntidadesUseCase;
import co.edu.monteria.medicitas.domain.model.Eps;
import co.edu.monteria.medicitas.domain.model.Especialidad;
import co.edu.monteria.medicitas.domain.model.Ips;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entidades")
@Tag(name = "Catálogos EPS/IPS/Especialidades", description = "Endpoints de consulta para listas de referencia de la red de salud de Montería")
public class EntidadesController {

    private final ConsultarEntidadesUseCase consultarEntidadesUseCase;

    public EntidadesController(ConsultarEntidadesUseCase consultarEntidadesUseCase) {
        this.consultarEntidadesUseCase = consultarEntidadesUseCase;
    }

    @GetMapping("/eps")
    @Operation(summary = "Listar EPS", description = "Obtiene la lista de EPS registradas (ej. Mutualser, Coosalud)")
    public ResponseEntity<List<Eps>> obtenerEps() {
        return ResponseEntity.ok(consultarEntidadesUseCase.obtenerEps());
    }

    @GetMapping("/ips")
    @Operation(summary = "Listar IPS", description = "Obtiene todas las IPS de Montería")
    public ResponseEntity<List<Ips>> obtenerIps() {
        return ResponseEntity.ok(consultarEntidadesUseCase.obtenerIps());
    }

    @GetMapping("/ips/por-eps/{epsId}")
    @Operation(summary = "Listar IPS con Convenio EPS", description = "Filtra la red de IPS prestadoras con convenio activo para una EPS específica")
    public ResponseEntity<List<Ips>> obtenerIpsPorEps(@PathVariable Long epsId) {
        return ResponseEntity.ok(consultarEntidadesUseCase.obtenerIpsPorEps(epsId));
    }

    @GetMapping("/especialidades")
    @Operation(summary = "Listar Especialidades", description = "Obtiene las especialidades médicas disponibles (Medicina General, Pediatría, Cardiología, etc.)")
    public ResponseEntity<List<Especialidad>> obtenerEspecialidades() {
        return ResponseEntity.ok(consultarEntidadesUseCase.obtenerEspecialidades());
    }
}
