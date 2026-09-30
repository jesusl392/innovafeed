package co.innovafeed.controller;

import co.innovafeed.dto.CategoriaDTO;
import co.innovafeed.service.CategoriaService;
import co.innovafeed.service.ReglaNegocioException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CRUD de categorías (solo admin).
 */
@Controller
@RequestMapping("/admin/categorias")
public class AdminCategoriaController {

    private final CategoriaService categoriaService;

    public AdminCategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "admin/categorias";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("categoria", new CategoriaDTO());
        model.addAttribute("accion", "/admin/categorias");
        model.addAttribute("titulo", "Nueva categoría");
        return "admin/categoria-formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("categoria") CategoriaDTO dto, BindingResult resultado,
                        Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                categoriaService.crear(dto);
                flash.addFlashAttribute("mensaje", "Categoría creada.");
                return "redirect:/admin/categorias";
            } catch (ReglaNegocioException ex) {
                resultado.rejectValue("nombre", "duplicado", ex.getMessage());
            }
        }
        model.addAttribute("accion", "/admin/categorias");
        model.addAttribute("titulo", "Nueva categoría");
        return "admin/categoria-formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", CategoriaDTO.desde(categoriaService.buscarPorId(id)));
        model.addAttribute("accion", "/admin/categorias/" + id);
        model.addAttribute("titulo", "Editar categoría");
        return "admin/categoria-formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("categoria") CategoriaDTO dto,
                             BindingResult resultado, Model model, RedirectAttributes flash) {
        if (!resultado.hasErrors()) {
            try {
                categoriaService.actualizar(id, dto);
                flash.addFlashAttribute("mensaje", "Categoría actualizada.");
                return "redirect:/admin/categorias";
            } catch (ReglaNegocioException ex) {
                resultado.rejectValue("nombre", "duplicado", ex.getMessage());
            }
        }
        model.addAttribute("accion", "/admin/categorias/" + id);
        model.addAttribute("titulo", "Editar categoría");
        return "admin/categoria-formulario";
    }

    @PostMapping("/{id}/activa")
    public String cambiarActiva(@PathVariable Long id, @RequestParam boolean activa, RedirectAttributes flash) {
        categoriaService.cambiarActiva(id, activa);
        flash.addFlashAttribute("mensaje", activa ? "Categoría activada." : "Categoría desactivada.");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            categoriaService.eliminar(id);
            flash.addFlashAttribute("mensaje", "Categoría eliminada.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categorias";
    }
}
