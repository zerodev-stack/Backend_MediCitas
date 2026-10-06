package co.edu.monteria.medicitas.domain.model;

/**
 * Modelo de resultado retornado por el puerto de verificación de afiliación EPS (BDUA).
 */
public class ResultadoAfiliacion {
    private boolean afiliadoActivo;
    private String epsNombre;
    private String epsCodigo;
    private boolean epsSoportadaEnRegion;
    private String mensajeOrientacion;

    public ResultadoAfiliacion() {}

    public ResultadoAfiliacion(boolean afiliadoActivo, String epsNombre, String epsCodigo, boolean epsSoportadaEnRegion, String mensajeOrientacion) {
        this.afiliadoActivo = afiliadoActivo;
        this.epsNombre = epsNombre;
        this.epsCodigo = epsCodigo;
        this.epsSoportadaEnRegion = epsSoportadaEnRegion;
        this.mensajeOrientacion = mensajeOrientacion;
    }

    public boolean isAfiliadoActivo() { return afiliadoActivo; }
    public void setAfiliadoActivo(boolean afiliadoActivo) { this.afiliadoActivo = afiliadoActivo; }

    public String getEpsNombre() { return epsNombre; }
    public void setEpsNombre(String epsNombre) { this.epsNombre = epsNombre; }

    public String getEpsCodigo() { return epsCodigo; }
    public void setEpsCodigo(String epsCodigo) { this.epsCodigo = epsCodigo; }

    public boolean isEpsSoportadaEnRegion() { return epsSoportadaEnRegion; }
    public void setEpsSoportadaEnRegion(boolean epsSoportadaEnRegion) { this.epsSoportadaEnRegion = epsSoportadaEnRegion; }

    public String getMensajeOrientacion() { return mensajeOrientacion; }
    public void setMensajeOrientacion(String mensajeOrientacion) { this.mensajeOrientacion = mensajeOrientacion; }
}
