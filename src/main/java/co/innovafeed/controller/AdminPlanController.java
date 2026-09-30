package co.innovafeed.controller;

import co.innovafeed.dto.PlanDTO;
import co.innovafeed.model.Plan;
import co.innovafeed.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * El admin configura precio y beneficios de cada plan (sin tocar código).
 */
@Controller
@RequestMapping("/admin/planes")
public class AdminPlanController {

    private final PlanService planService;

    public AdminPlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("planes", planService.listarPlanes());
        return "admin/planes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Plan p = planService.buscarPorId(id);
        model.addAttribute("plan", PlanDTO.desde(p));
        model.addAttribute("codigo", p.getCodigo());
        model.addAttribute("id", id);
        return "admin/plan-formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("plan") PlanDTO dto,
                             BindingResult resultado, Model model, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            model.addAttribute("codigo", planService.buscarPorId(id).getCodigo());
            model.addAttribute("id", id);
            return "admin/plan-formulario";
        }
        planService.actualizar(id, dto);
        flash.addFlashAttribute("mensaje", "Plan actualizado. Los cambios aplican de inmediato.");
        return "redirect:/admin/planes";
    }
}
