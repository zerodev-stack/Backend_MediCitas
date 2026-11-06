package co.edu.monteria.medicitas.domain.model;

/**
 * Habilitación de un servicio en una sede para un médico concreto. Es lo que el médico
 * configura en "Mis servicios y sedes" y lo que autoriza a publicar bloques.
 */
public class ServicioMedico {
    private Long id;
    private Medico medico;
    private Servicio servicio;
    private Sede sede;

    public ServicioMedico() {}

    public ServicioMedico(Long id, Medico medico, Servicio servicio, Sede sede) {
        this.id = id;
        this.medico = medico;
        this.servicio = servicio;
        this.sede = sede;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }

    public Sede getSede() { return sede; }
    public void setSede(Sede sede) { this.sede = sede; }
}
