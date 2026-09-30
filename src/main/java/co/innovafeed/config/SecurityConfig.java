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
                // Zonas por rol
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/emprendedor/**").hasRole("EMPRENDEDOR")
                .requestMatchers("/usuario/**").hasRole("USUARIO")
                // Todo lo demás requiere sesión
                .anyRequest().authenticated())
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
