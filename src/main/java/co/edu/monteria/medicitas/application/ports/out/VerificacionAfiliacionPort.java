package co.edu.monteria.medicitas.application.ports.out;

import co.edu.monteria.medicitas.domain.model.ResultadoAfiliacion;
import co.edu.monteria.medicitas.domain.model.enums.TipoDocumento;

public interface VerificacionAfiliacionPort {
    /**
     * Consulta el estado de afiliación a la EPS en la BDUA (Base de Datos Única de Afiliados).
     * @param tipoDocumento Tipo de documento (CC, TI, etc.)
     * @param numDocumento Número de documento
     * @return ResultadoAfiliacion con estado de la EPS y compatibilidad con la red de Montería
     */
    ResultadoAfiliacion consultarAfiliacionBdua(TipoDocumento tipoDocumento, String numDocumento);
}
