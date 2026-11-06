package co.edu.monteria.medicitas.domain.model.enums;

/**
 * Naturaleza de un servicio del catálogo. La etiqueta es la que muestra la app
 * ("Consulta · IPS Ser Salud · Sede Centro").
 */
public enum TipoServicio {
    CONSULTA("Consulta"),
    PROCEDIMIENTO_MEDICO("Procedimiento médico"),
    PROCEDIMIENTO_QUIRURGICO("Procedimiento quirúrgico"),
    EXAMEN("Examen");

    private final String etiqueta;

    TipoServicio(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
