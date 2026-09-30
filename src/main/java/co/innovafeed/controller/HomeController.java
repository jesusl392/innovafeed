package co.innovafeed.controller;

import co.innovafeed.model.Usuario;
import co.innovafeed.service.EmprendimientoService;
import co.innovafeed.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * Página de inicio pública: slogan, "Recomendados" y los primeros del catálogo.
 */
@Controller
public class HomeController {

    private static final int MAX_EN_INICIO = 6;
    private static final int MAX_RECOMENDADOS = 3;

    private final EmprendimientoService emprendimientoService;
    private final UsuarioService usuarioService;

    public HomeController(EmprendimientoService emprendimientoService, UsuarioService usuarioService) {
        this.emprendimientoService = emprendimientoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String inicio(Principal principal, Model model) {
        Usuario visitante = usuarioService.usuarioActual(principal);
        model.addAttribute("recomendados", emprendimientoService.recomendados(visitante, MAX_RECOMENDADOS));
        model.addAttribute("emprendimientos", emprendimientoService.buscarCatalogo(null, null)
                .stream().limit(MAX_EN_INICIO).toList());
        model.addAttribute("idsConBoost", emprendimientoService.idsConBoostActivo());
        return "index";
    }
}
