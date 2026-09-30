package co.innovafeed.controller;

import co.innovafeed.dto.PerfilDTO;
import co.innovafeed.model.Usuario;
import co.innovafeed.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Perfil de cualquier usuario con sesión (los tres roles).
 * Muestra qué datos personales guardamos (Ley 1581: derecho a conocer) y permite actualizarlos.
 */
@Controller
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil")
    public String perfil(Principal principal, Model model) {
        Usuario yo = usuarioService.usuarioActual(principal);
        model.addAttribute("usuario", yo);
        model.addAttribute("perfil", PerfilDTO.desde(yo));
        return "perfil";
    }

    @PostMapping("/perfil")
    public String actualizar(@Valid @ModelAttribute("perfil") PerfilDTO dto, BindingResult resultado,
                             Principal principal, Model model, RedirectAttributes flash) {
        Usuario yo = usuarioService.usuarioActual(principal);
        if (resultado.hasErrors()) {
            model.addAttribute("usuario", yo);
            return "perfil";
        }
        usuarioService.actualizarPerfil(yo.getId(), dto);
        flash.addFlashAttribute("mensaje", "Perfil actualizado.");
        return "redirect:/perfil";
    }
}
