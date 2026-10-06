package co.edu.monteria.medicitas.infrastructure.adapters.out.security;

import co.edu.monteria.medicitas.domain.model.Usuario;
import co.edu.monteria.medicitas.domain.model.enums.RolUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

/**
 * Principal de autenticación que transporta el identificador del usuario.
 *
 * Spring Security solo conserva el 'UserDetails' en el contexto de seguridad, y el
 * 'User' estándar únicamente admite correo, contraseña y autoridades. Al extenderlo con el
 * identificador, 'SecurityContextContextoUsuarioAdapter' puede resolver el usuario
 * autenticado sin volver a decodificar el token ni consultar la base de datos.
 */
public class UsuarioPrincipal extends User {

    private final Long usuarioId;
    private final String nombreCompleto;
    private final String numDocumento;
    private final RolUsuario rol;

    public UsuarioPrincipal(Long usuarioId, String email, String passwordHash,
                            String nombreCompleto, String numDocumento, RolUsuario rol,
                            Collection<? extends GrantedAuthority> authorities) {
        super(email, passwordHash, authorities);
        this.usuarioId = usuarioId;
        this.nombreCompleto = nombreCompleto;
        this.numDocumento = numDocumento;
        this.rol = rol;
    }

    public static UsuarioPrincipal desde(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        return new UsuarioPrincipal(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getNombreCompleto(),
                usuario.getNumDocumento(),
                usuario.getRol(),
                authorities
        );
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getNumDocumento() {
        return numDocumento;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public static List<GrantedAuthority> autoridades(Usuario usuario) {
        return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(usuario.getRol().name()));
    }
}
