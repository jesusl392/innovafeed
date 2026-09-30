package co.innovafeed.controller;

import co.innovafeed.service.PlanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Páginas públicas informativas: planes de visibilidad (precios y beneficios salen de la
 * tabla "planes") y política de tratamiento de datos.
 */
@Controller
public class PlanesController {

    private final PlanService planService;

    public PlanesController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/planes")
    public String planes(Model model) {
        model.addAttribute("planes", planService.listarPlanes());
        return "planes";
    }

    /** Política de tratamiento de datos personales (Ley 1581 de 2012). */
    @GetMapping("/privacidad")
    public String privacidad() {
        return "privacidad";
    }
}
