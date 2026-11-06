package co.edu.monteria.medicitas.domain.model;

import co.edu.monteria.medicitas.domain.model.enums.EstadoDisponibilidad;

import java.util.List;

/**
 * Un bloque junto con sus cupos y las citas vigentes que los ocupan. Es lo que necesita
 * la pantalla de disponibilidad para mostrar "1 de 4 cupos libres" y, al intentar
 * cancelar, la lista de "Citas que se conservan".
 */
public class ResumenBloque {
    private final BloqueDisponibilidad bloque;
    private final List<DisponibilidadMedico> cupos;
    private final List<Cita> citasVigentes;

    public ResumenBloque(BloqueDisponibilidad bloque, List<DisponibilidadMedico> cupos, List<Cita> citasVigentes) {
        this.bloque = bloque;
        this.cupos = cupos;
        this.citasVigentes = citasVigentes;
    }

    public BloqueDisponibilidad getBloque() { return bloque; }
    public List<DisponibilidadMedico> getCupos() { return cupos; }
    public List<Cita> getCitasVigentes() { return citasVigentes; }

    public long cuposLibres() {
        return cupos.stream().filter(c -> c.getEstado() == EstadoDisponibilidad.LIBRE).count();
    }

    public long cuposReservados() {
        return cupos.stream().filter(c -> c.getEstado() == EstadoDisponibilidad.OCUPADO).count();
    }

    /** Los cupos cancelados no cuentan: un bloque cancelado muestra 0 de 0. */
    public long cuposTotales() {
        return cuposLibres() + cuposReservados();
    }
}
