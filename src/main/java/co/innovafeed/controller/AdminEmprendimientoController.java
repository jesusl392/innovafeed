package co.innovafeed.controller;

import co.innovafeed.dto.EmprendimientoDTO;
import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.EstadoEmprendimiento;
import co.innovafeed.model.EtapaEmprendimiento;
import co.innovafeed.service.CategoriaService;
import co.innovafeed.service.EmprendimientoService;
import co.innovafeed.service.ResultadoNotificado;
import co.innovafeed.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Gestión de TODOS los emprendimientos por parte del admin:
 * listar (con filtro por estado), editar, cambiar estado y eliminar.
 */
@Controller
@RequestMapping("/admin/emprendimientos")
public class AdminEmprendimientoController {

    private final EmprendimientoService emprendimientoService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;

    public AdminEmprendimientoController(EmprendimientoService emprendimientoService,
                                         CategoriaService categoriaService, UsuarioService usuarioService) {
        this.emprendimientoService = emprendimientoService;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
    }

    /** /admin/emprendimientos?estado=PENDIENTE */
    @GetMapping
    public String listar(@RequestParam(required = false) EstadoEmprendimiento estado, Model model) {
        model.addAttribute("emprendimientos", emprendimientoService.listarTodos(estado));
        model.addAttribute("estados", EstadoEmprendimiento.values());
        model.addAttribute("estadoSeleccionado", estado);
        return "admin/emprendimientos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Emprendimiento e = emprendimientoService.buscarPorId(id);
        prepararFormulario(model, EmprendimientoDTO.desde(e), id, e);
        return "emprendimientos/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("emprendimiento") EmprendimientoDTO dto, BindingResult resultado,
                             Principal principal, Model model, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, dto, id, emprendimientoService.buscarPorId(id));
            return "emprendimientos/formulario";
        }
        emprendimientoService.actualizar(id, dto, usuarioService.usuarioActual(principal));
        flash.addFlashAttribute("mensaje", "Emprendimiento actualizado.");
        return "redirect:/admin/emprendimientos";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam EstadoEmprendimiento estado,
                                RedirectAttributes flash) {
        ResultadoNotificado r = emprendimientoService.cambiarEstado(id, estado);
        String mensaje = "«" + r.emprendimiento().getNombre() + "» ahora está en estado " + estado.getEtiqueta() + ".";
        if (r.usuariosNotificados() > 0) {
            mensaje += " Se notificó a " + r.usuariosNotificados() + " usuario(s) interesado(s).";
        }
        flash.addFlashAttribute("mensaje", mensaje);
        return "redirect:/admin/emprendimientos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        emprendimientoService.eliminar(id, usuarioService.usuarioActual(principal));
        flash.addFlashAttribute("mensaje", "Emprendimiento eliminado.");
        return "redirect:/admin/emprendimientos";
    }

    private void prepararFormulario(Model model, EmprendimientoDTO dto, Long id, Emprendimiento e) {
        model.addAttribute("emprendimiento", dto);
        model.addAttribute("accion", "/admin/emprendimientos/" + id);
        model.addAttribute("titulo", "Editar " + e.getNombre() + " (admin)");
        model.addAttribute("permiteVideo", e.getPlan().isPermiteVideoPitch());
        model.addAttribute("volver", "/admin/emprendimientos");
        model.addAttribute("categorias", categoriaService.listarActivas());
        model.addAttribute("etapas", EtapaEmprendimiento.values());
    }
}
