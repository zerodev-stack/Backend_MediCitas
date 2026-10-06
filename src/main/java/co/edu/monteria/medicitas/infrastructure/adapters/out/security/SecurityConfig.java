package co.edu.monteria.medicitas.infrastructure.adapters.out.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración central de Seguridad (Spring Security + JWT).
 *
 * La autorización fina NO se resuelve aquí sino con anotaciones @PreAuthorize sobre los
 * controladores, que es donde se sabe qué operación requiere qué rol. Esta cadena de
 * filtros se limita a separar lo público del resto y a exigir un token válido.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final List<String> origenesPermitidos;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter,
            @Value("${medicitas.cors.allowed-origins}") List<String> origenesPermitidos) {
        this.jwtFilter = jwtFilter;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(excepciones -> excepciones
                // Sin este punto de entrada, Spring responde 403 cuando falta el token.
                // Lo correcto es 401: el cliente no está autenticado y debe iniciar
                // sesión. El 403 queda reservado para "autenticado pero sin permiso".
                .authenticationEntryPoint((peticion, respuesta, excepcion) -> respuesta.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Se requiere un token de sesión válido para acceder a este recurso."))
            )
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos, uno a uno. '/auth/**' ya no se abre en bloque:
                // el alta de médicos es una operación de administrador.
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/registro-paciente").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/validar-bdua").permitAll()
                .requestMatchers("/entidades/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // El reenvío interno a /error debe quedar abierto: si se exige
                // autenticación también en el despacho de error, el 401 oculta el
                // fallo original y toda respuesta de error llega como 401.
                .requestMatchers("/error").permitAll()
                // Catálogo de agenda: público para que un visitante sin sesión pueda explorar.
                .requestMatchers(HttpMethod.GET, "/disponibilidades").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS restringido a los orígenes declarados en configuración.
     *
     * Antes se permitía '*' junto con 'allowCredentials(true)', combinación que el navegador
     * rechaza y que, de permitirse, expondría la API con credenciales a cualquier sitio.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(origenesPermitidos);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
