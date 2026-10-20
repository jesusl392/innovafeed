package co.innovafeed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Reglas de seguridad de innovaFeed:
 * - Público: inicio, catálogo, detalle de emprendimientos, planes, registro y login.
 * - /admin/**        solo ADMIN
 * - /emprendedor/**  solo EMPRENDEDOR
 * - /usuario/**      solo USUARIO
 * - /perfil          cualquier usuario con sesión
 * - URL inexistente  página 404 (también para visitantes sin sesión)
 */
@Configuration
public class SecurityConfig {

    private final RedireccionPorRolHandler redireccionPorRolHandler;

    public SecurityConfig(RedireccionPorRolHandler redireccionPorRolHandler) {
        this.redireccionPorRolHandler = redireccionPorRolHandler;
    }

    /** BCrypt: cifrado de contraseñas de una sola vía y con "sal" aleatoria. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos y páginas públicas
                .requestMatchers("/css/**", "/img/**", "/favicon.ico", "/error/**", "/error").permitAll()
                .requestMatchers("/", "/catalogo/**", "/emprendimientos/**", "/planes", "/privacidad").permitAll()
                .requestMatchers("/login", "/registro").permitAll()
                // Consola H2 (solo existe en desarrollo; en prod está apagada)
                .requestMatchers("/h2-console/**").permitAll()
                // Zonas privadas: por rol, o con cualquier sesión
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/emprendedor/**").hasRole("EMPRENDEDOR")
                .requestMatchers("/usuario/**").hasRole("USUARIO")
                .requestMatchers("/perfil").authenticated()
                // Cualquier otra URL: se deja pasar para que, si no existe, responda la página 404
                // (con "authenticated()" un visitante veía el login en vez del 404).
                // IMPORTANTE: toda página privada nueva debe ir bajo una de las rutas de arriba.
                .anyRequest().permitAll())
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(redireccionPorRolHandler)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"))
            // La consola H2 usa frames y formularios propios
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
