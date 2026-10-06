package co.edu.monteria.medicitas.domain.model;

public class Medico {
    private Long id;
    private Usuario usuario;
    private Especialidad especialidad;
    private Ips ips;
    private String registroMedico;

    public Medico() {}

    public Medico(Long id, Usuario usuario, Especialidad especialidad, Ips ips, String registroMedico) {
        this.id = id;
        this.usuario = usuario;
        this.especialidad = especialidad;
        this.ips = ips;
        this.registroMedico = registroMedico;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }

    public Ips getIps() { return ips; }
    public void setIps(Ips ips) { this.ips = ips; }

    public String getRegistroMedico() { return registroMedico; }
    public void setRegistroMedico(String registroMedico) { this.registroMedico = registroMedico; }
}
