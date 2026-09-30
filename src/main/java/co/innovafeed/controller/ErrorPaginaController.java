package co.innovafeed.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Página amigable de "acceso denegado". Los errores 404 y 500 los resuelve
 * Spring Boot automáticamente con las plantillas templates/error/404.html y 500.html.
 */
@Controller
public class ErrorPaginaController {

    // Acepta cualquier método: si el acceso se niega en un POST, se reenvía aquí como POST
    @RequestMapping("/error/403")
    public String accesoDenegado() {
        return "error/403";
    }
}
