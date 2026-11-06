package co.edu.monteria.medicitas.infrastructure.adapters.in.rest.dto;

import co.edu.monteria.medicitas.domain.model.BloqueDisponibilidad;
import co.edu.monteria.medicitas.domain.model.Cita;
import co.edu.monteria.medicitas.domain.model.ResumenBloque;
import co.edu.monteria.medicitas.domain.model.enums.EstadoBloque;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Bloque de disponibilidad con el conteo de cupos y las citas que lo ocupan.
 */
public record BloqueResponse(
        Long id,
        ServicioResponse servicio,
        SedeResponse sede,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer duracionCupoMinutos,
        EstadoBloque estado,
        long cuposTotales,
        long cuposLibres,
        long cuposReservados,
        List<CitaDeBloque> citas
) {
    /** Cita vigente dentro del bloque: solo lo necesario para listarla. */
    public record CitaDeBloque(Long id, Long pacienteId, String pacienteNombre, LocalTime horaInicio, LocalTime horaFin) {
        static CitaDeBloque desde(Cita cita) {
            return new CitaDeBloque(
                    cita.getId(),
                    cita.getPaciente() != null ? cita.getPaciente().getId() : null,
                    cita.getPaciente() != null ? cita.getPaciente().getNombreCompleto() : null,
                    cita.getDisponibilidad().getHoraInicio(),
                    cita.getDisponibilidad().getHoraFin()
            );
        }
    }

    public static BloqueResponse desde(ResumenBloque resumen) {
        BloqueDisponibilidad bloque = resumen.getBloque();
        return new BloqueResponse(
                bloque.getId(),
                ServicioResponse.desde(bloque.getServicio()),
                SedeResponse.desde(bloque.getSede()),
                bloque.getFecha(),
                bloque.getHoraInicio(),
                bloque.getHoraFin(),
                bloque.getDuracionCupoMinutos(),
                bloque.getEstado(),
                resumen.cuposTotales(),
                resumen.cuposLibres(),
                resumen.cuposReservados(),
                resumen.getCitasVigentes().stream().map(CitaDeBloque::desde).toList()
        );
    }
}
