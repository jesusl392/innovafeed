package co.innovafeed.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Después de iniciar sesión, envía a cada rol a su propio panel.
 */
@Component
public class RedireccionPorRolHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String destino = "/";
        for (GrantedAuthority autoridad : authentication.getAuthorities()) {
            switch (autoridad.getAuthority()) {
                case "ROLE_ADMIN" -> destino = "/admin";
                case "ROLE_EMPRENDEDOR" -> destino = "/emprendedor";
                case "ROLE_USUARIO" -> destino = "/usuario";
                default -> { }
            }
        }
        response.sendRedirect(request.getContextPath() + destino);
    }
}
