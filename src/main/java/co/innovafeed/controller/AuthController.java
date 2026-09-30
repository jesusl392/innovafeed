package co.innovafeed.controller;

import co.innovafeed.dto.RegistroDTO;
import co.innovafeed.service.ReglaNegocioException;
import co.innovafeed.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Login y registro. El POST de /login lo procesa Spring Security automáticamente.
 */
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/registro")
    public String formularioRegistro(Model model) {
        model.addAttribute("registro", new RegistroDTO());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroDTO registro,
                            BindingResult resultado) {
        // 1. Errores de validación del formulario (@NotBlank, @Email, ...)
        if (resultado.hasErrors()) {
            return "auth/registro";
        }
        // 2. Reglas de negocio (correo repetido, contraseñas distintas, ...)
        try {
            usuarioService.registrar(registro);
        } catch (ReglaNegocioException ex) {
            resultado.reject("registro.error", ex.getMessage());
            return "auth/registro";
        }
        return "redirect:/login?registrado";
    }
}
