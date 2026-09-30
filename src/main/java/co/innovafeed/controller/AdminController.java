package co.innovafeed.controller;

import co.innovafeed.service.EstadisticaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Dashboard del administrador con las métricas generales de la plataforma.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EstadisticaService estadisticaService;

    public AdminController(EstadisticaService estadisticaService) {
        this.estadisticaService = estadisticaService;
    }

    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("m", estadisticaService.metricasAdmin());
        return "admin/inicio";
    }
}
