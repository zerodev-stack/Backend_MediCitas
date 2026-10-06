package co.edu.monteria.medicitas.infrastructure.adapters.out.security;

import co.edu.monteria.medicitas.application.ports.out.JwtPort;
import co.edu.monteria.medicitas.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

/**
 * Adaptador de seguridad para la generación y validación de tokens JWT (JJWT 0.12.5).
 *
 * SOBRE EL SECRETO DE FIRMA:
 * No hay ningún valor por defecto en la configuración. Si la propiedad llega vacía, esta
 * clase genera una clave aleatoria de 256 bits al arrancar. Así el proyecto se levanta sin
 * configuración adicional, pero al mismo tiempo:
 *   - no hay ninguna clave firmada en el repositorio;
 *   - un token emitido por otra instalación (por ejemplo, con la clave que antes estaba
 *     versionada) deja de ser válido aquí;
 *   - reiniciar invalida las sesiones, algo que se avisa por consola.
 * En un entorno real se define MEDICITAS_JWT_SECRET y el comportamiento es el de un
 * despliegue normal.
 */
@Component
public class JwtAdapter implements JwtPort {

    private static final Logger log = LoggerFactory.getLogger(JwtAdapter.class);
    private static final int LONGITUD_MINIMA_BYTES = 32;

    private final String jwtSecret;
    private final long jwtExpirationMs;
    private final boolean secretoEfimero;

    public JwtAdapter(
            @Value("${medicitas.jwt.secret:}") String jwtSecret,
            @Value("${medicitas.jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
        this.secretoEfimero = !StringUtils.hasText(jwtSecret);

        if (this.secretoEfimero) {
            this.jwtSecret = generarSecretoAleatorio();
            log.warn("""
                    ------------------------------------------------------------------
                    MEDICITAS_JWT_SECRET no está definida: se generó una clave de firma
                    EFÍMERA para esta ejecución. Ningún token emitido antes será válido
                    y las sesiones se invalidarán al reiniciar.
                    Defina MEDICITAS_JWT_SECRET para un comportamiento estable.
                    ------------------------------------------------------------------""");
        } else {
            this.jwtSecret = jwtSecret;
            if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < LONGITUD_MINIMA_BYTES) {
                throw new IllegalStateException(
                        "MEDICITAS_JWT_SECRET debe tener al menos " + LONGITUD_MINIMA_BYTES
                                + " bytes para el algoritmo de firma. Configure una clave de 256 bits o más.");
            }
        }
    }

    private static String generarSecretoAleatorio() {
        byte[] bytes = new byte[LONGITUD_MINIMA_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isSecretoEfimero() {
        return secretoEfimero;
    }

    @Override
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("numDocumento", usuario.getNumDocumento())
                .claim("nombreCompleto", usuario.getNombreCompleto())
                .claim("rol", usuario.getRol().name())
                .claim("epsId", usuario.getEps() != null ? usuario.getEps().getId() : null)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String obtenerEmailDesdeToken(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public boolean validarToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Token JWT rechazado: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
