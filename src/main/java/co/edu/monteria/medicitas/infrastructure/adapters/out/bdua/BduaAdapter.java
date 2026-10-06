package co.edu.monteria.medicitas.infrastructure.adapters.out.bdua;

import co.edu.monteria.medicitas.application.ports.out.VerificacionAfiliacionPort;
import co.edu.monteria.medicitas.domain.model.ResultadoAfiliacion;
import co.edu.monteria.medicitas.domain.model.enums.TipoDocumento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Adaptador de Salida Simulado para la BDUA (Base de Datos Única de Afiliados - ADRES).
 * 
 * POR QUÉ Y CÓMO DE LA VERIFICACIÓN DE AFILIACIÓN:
 * En un escenario real de salud en Colombia, la IPS/EPS valida la condición del afiliado
 * en el portal de la ADRES (BDUA) mediante web services SOAP/REST.
 * 
 * En MediCitas, este adaptador simula la consulta para Montería:
 * - Si el documento termina en 9 (ej. CC 1067999999), simula un paciente inactivo/retirado.
 * - Si la EPS asignada por BDUA es 'EPS Sanitas' (o documentos terminados en 8), simula que la EPS no opera en la red directa de Montería.
 * - Para la mayoría de usuarios (ej. CC 1067890123), confirma la afiliación activa a EPS Mutualser (EPS008).
 * - En caso de EPS no soportada, adjunta la orientación legal para el cambio de EPS (Portal Mi Seguridad Social).
 */
@Component
public class BduaAdapter implements VerificacionAfiliacionPort {

    @Value("${medicitas.bdua.supported-eps-codes:EPS008,EPS002}")
    private String supportedCodesConfig;

    @Override
    public ResultadoAfiliacion consultarAfiliacionBdua(TipoDocumento tipoDocumento, String numDocumento) {
        if (numDocumento == null || numDocumento.trim().isEmpty()) {
            return new ResultadoAfiliacion(false, "Desconocida", "EPS999", false, "Número de documento inválido");
        }

        String lastChar = numDocumento.trim().substring(numDocumento.trim().length() - 1);

        // Caso de simulación: Afiliado retirado/inactivo en BDUA
        if ("9".equals(lastChar)) {
            return new ResultadoAfiliacion(
                    false,
                    "Sin Afiliación Activa",
                    "EPS999",
                    false,
                    "Actualmente su documento no registra una afiliación activa en el sistema de salud (BDUA). Por favor diríjase a la oficina del SISBÉN o de la EPS Mutualser en Montería para solicitar su afiliación."
            );
        }

        // Caso de simulación: EPS Sanitas (no convenio activo en esta prueba)
        if ("8".equals(lastChar)) {
            return new ResultadoAfiliacion(
                    true,
                    "EPS Sanitas",
                    "EPS005",
                    false,
                    "Su EPS registrada en BDUA es EPS Sanitas. Actualmente MediCitas Montería presta servicio a la red preferencial EPS Mutualser. Para agendar sus citas en esta red, puede gestionar el traslado de EPS ingresando al portal nacional www.miseguridadsocial.gov.co o visitando la sede EPS Mutualser en la Calle 29 # 14-20, La Julia, Montería."
            );
        }

        // Caso estándar: EPS Mutualser (EPS008) - Red Principal de Montería, Córdoba
        List<String> supportedCodes = Arrays.asList(supportedCodesConfig.split(","));
        boolean isSupported = supportedCodes.contains("EPS008");

        return new ResultadoAfiliacion(
                true,
                "EPS Mutualser",
                "EPS008",
                isSupported,
                "Afiliación activa verificada en EPS Mutualser. Cobertura completa en la red de IPS de Montería."
        );
    }
}
