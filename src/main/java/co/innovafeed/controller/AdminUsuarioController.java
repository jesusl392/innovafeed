package co.innovafeed.controller;

import co.innovafeed.model.Rol;
import co.innovafeed.service.ReglaNegocioException;
import co.innovafeed.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Gestión de usuarios por el admin: ver cuentas y activarlas/desactivarlas.
 * (Una cuenta desactivada no puede iniciar sesión.) Nunca se muestran contraseñas.
 */
@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** /admin/usuarios?rol=EMPRENDEDOR */
    @GetMapping
    public String listar(@RequestParam(required = false) Rol rol, Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos().stream()
                .filter(u -> rol == null || u.getRol() == rol)
                .toList());
        model.addAttribute("roles", Rol.values());
        model.addAttribute("rolSeleccionado", rol);
        return "admin/usuarios";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
                                Principal principal, RedirectAttributes flash) {
        try {
            usuarioService.cambiarEstado(id, activo, principal.getName());
            flash.addFlashAttribute("mensaje", activo ? "Cuenta activada." : "Cuenta desactivada: ya no podrá iniciar sesión.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
