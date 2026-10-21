package co.edu.monteria.medicitas.infrastructure.adapters.out.bdua;

import co.edu.monteria.medicitas.application.ports.out.VerificacionAfiliacionPort;
import co.edu.monteria.medicitas.domain.model.ResultadoAfiliacion;
import co.edu.monteria.medicitas.domain.model.enums.TipoDocumento;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.entity.EpsEntity;
import co.edu.monteria.medicitas.infrastructure.adapters.out.persistence.repository.EpsAffiliateJpaRepository;
import org.springframework.stereotype.Component;

/**
 * Adaptador de Salida para la verificación de afiliación (BDUA - Base de Datos Única de Afiliados).
 *
 * POR QUÉ Y CÓMO DE LA VERIFICACIÓN DE AFILIACIÓN:
 * En un escenario real de salud en Colombia, la IPS/EPS valida la condición del afiliado
 * en el portal de la ADRES (BDUA) mediante web services SOAP/REST.
 *
 * En MediCitas, la BDUA se representa con la tabla 'eps_affiliates':
 * - Si la cédula no está registrada, el documento no tiene afiliación activa.
 * - Si está registrada en una EPS sin convenio activo en la región (eps.activa = false),
 *   se adjunta la orientación legal para el traslado de EPS (Portal Mi Seguridad Social).
 * - Si está registrada en una EPS con convenio, la afiliación queda verificada.
 */
@Component
public class BduaAdapter implements VerificacionAfiliacionPort {

    private final EpsAffiliateJpaRepository affiliateRepository;

    public BduaAdapter(EpsAffiliateJpaRepository affiliateRepository) {
        this.affiliateRepository = affiliateRepository;
    }

    @Override
    public ResultadoAfiliacion consultarAfiliacionBdua(TipoDocumento tipoDocumento, String numDocumento) {
        if (numDocumento == null || numDocumento.trim().isEmpty()) {
            return new ResultadoAfiliacion(false, "Desconocida", "EPS999", false, "Número de documento inválido");
        }

        return affiliateRepository.findByDocumentNumber(numDocumento.trim())
                .map(afiliado -> resultadoPara(afiliado.getEps()))
                .orElseGet(() -> new ResultadoAfiliacion(
                        false,
                        "Sin Afiliación Activa",
                        "EPS999",
                        false,
                        "Actualmente su documento no registra una afiliación activa en el sistema de salud (BDUA). Por favor diríjase a la oficina del SISBÉN o de la EPS Mutualser en Montería para solicitar su afiliación."
                ));
    }

    private ResultadoAfiliacion resultadoPara(EpsEntity eps) {
        if (!Boolean.TRUE.equals(eps.getActiva())) {
            return new ResultadoAfiliacion(
                    true,
                    eps.getNombre(),
                    eps.getCodigoBdua(),
                    false,
                    "Su EPS registrada en BDUA es " + eps.getNombre() + ". Actualmente MediCitas Montería presta servicio a la red de EPS con convenio activo en la región. Para agendar sus citas en esta red, puede gestionar el traslado de EPS ingresando al portal nacional www.miseguridadsocial.gov.co o visitando la sede EPS Mutualser en la Calle 29 # 14-20, La Julia, Montería."
            );
        }

        return new ResultadoAfiliacion(
                true,
                eps.getNombre(),
                eps.getCodigoBdua(),
                true,
                "Afiliación activa verificada en " + eps.getNombre() + ". Cobertura completa en la red de IPS de Montería."
        );
    }
}
