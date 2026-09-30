package co.innovafeed.controller;

import co.innovafeed.dto.EmprendimientoDTO;
import co.innovafeed.dto.ResumenVisibilidad;
import co.innovafeed.model.*;
import co.innovafeed.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Zona del emprendedor: ver, publicar, editar y eliminar SUS emprendimientos,
 * y gestionar su visibilidad (plan y boosts).
 * El servicio vuelve a verificar que sea el dueño (defensa en profundidad).
 */
@Controller
@RequestMapping("/emprendedor")
public class EmprendedorController {

    private final EmprendimientoService emprendimientoService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;
    private final SuscripcionService suscripcionService;
    private final BoostService boostService;
    private final PlanService planService;
    private final EstadisticaService estadisticaService;

    public EmprendedorController(EmprendimientoService emprendimientoService, CategoriaService categoriaService,
                                 UsuarioService usuarioService, SuscripcionService suscripcionService,
                                 BoostService boostService, PlanService planService,
                                 EstadisticaService estadisticaService) {
        this.estadisticaService = estadisticaService;
        this.emprendimientoService = emprendimientoService;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
        this.suscripcionService = suscripcionService;
        this.boostService = boostService;
        this.planService = planService;
    }

    @GetMapping
    public String inicio(Principal principal, Model model) {
        Usuario yo = usuarioService.usuarioActual(principal);
        List<Emprendimiento> mios = emprendimientoService.listarDePropietario(yo);
        model.addAttribute("resumenes", mios.stream().map(this::resumir).toList());
        model.addAttribute("visitas", estadisticaService.visitasPorEmprendimiento(mios));
        return "emprendedor/inicio";
    }

    /** Estadísticas: lo que se muestra depende de los beneficios del plan. */
    @GetMapping("/emprendimientos/{id}/estadisticas")
    public String estadisticas(@PathVariable Long id, Principal principal, Model model) {
        Emprendimiento e = buscarPropio(id, principal);
        model.addAttribute("e", e);
        model.addAttribute("est", estadisticaService.estadisticasDe(id, usuarioService.usuarioActual(principal)));
        return "emprendedor/estadisticas";
    }

    // ------------------- Visibilidad: planes y boosts -------------------

    /** Página donde el emprendedor ve su plan, se suscribe (simulado) y activa boosts. */
    @GetMapping("/emprendimientos/{id}/visibilidad")
    public String visibilidad(@PathVariable Long id, Principal principal, Model model) {
        Emprendimiento e = buscarPropio(id, principal);
        model.addAttribute("r", resumir(e));
        model.addAttribute("planes", planService.listarPlanes());
        model.addAttribute("historial", suscripcionService.historial(e));
        model.addAttribute("horasBoost", boostService.getHorasBoost());
        return "emprendedor/visibilidad";
    }

    /** Botón "Suscribirme": pago SIMULADO, cambia el plan y registra la fecha. */
    @PostMapping("/emprendimientos/{id}/plan")
    public String cambiarPlan(@PathVariable Long id, @RequestParam CodigoPlan plan,
                              Principal principal, RedirectAttributes flash) {
        try {
            flash.addFlashAttribute("mensaje",
                    suscripcionService.cambiarPlan(id, plan, usuarioService.usuarioActual(principal)));
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/emprendedor/emprendimientos/" + id + "/visibilidad";
    }

    @PostMapping("/emprendimientos/{id}/boost")
    public String activarBoost(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        try {
            Boost b = boostService.activar(id, usuarioService.usuarioActual(principal));
            flash.addFlashAttribute("mensaje", "¡Boost activado! Tu emprendimiento aparece de primero en el catálogo hasta el "
                    + b.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ".");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/emprendedor/emprendimientos/" + id + "/visibilidad";
    }

    private ResumenVisibilidad resumir(Emprendimiento e) {
        return new ResumenVisibilidad(e,
                suscripcionService.vigente(e).orElse(null),
                boostService.boostActivo(e).orElse(null),
                boostService.disponiblesEsteMes(e));
    }

    // ------------------------- CRUD -------------------------

    @GetMapping("/emprendimientos/nuevo")
    public String nuevo(Model model) {
        prepararFormulario(model, new EmprendimientoDTO(), "/emprendedor/emprendimientos",
                "Publicar emprendimiento", false);
        return "emprendimientos/formulario";
    }

    @PostMapping("/emprendimientos")
    public String crear(@Valid @ModelAttribute("emprendimiento") EmprendimientoDTO dto, BindingResult resultado,
                        Principal principal, Model model, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, dto, "/emprendedor/emprendimientos", "Publicar emprendimiento", false);
            return "emprendimientos/formulario";
        }
        emprendimientoService.crear(dto, usuarioService.usuarioActual(principal));
        flash.addFlashAttribute("mensaje",
                "¡Emprendimiento enviado! Quedó en estado Pendiente hasta que el administrador lo active.");
        return "redirect:/emprendedor";
    }

    @GetMapping("/emprendimientos/{id}/editar")
    public String editar(@PathVariable Long id, Principal principal, Model model) {
        Emprendimiento e = buscarPropio(id, principal);
        prepararFormulario(model, EmprendimientoDTO.desde(e), "/emprendedor/emprendimientos/" + id,
                "Editar " + e.getNombre(), e.getPlan().isPermiteVideoPitch());
        return "emprendimientos/formulario";
    }

    @PostMapping("/emprendimientos/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("emprendimiento") EmprendimientoDTO dto, BindingResult resultado,
                             Principal principal, Model model, RedirectAttributes flash) {
        Emprendimiento e = buscarPropio(id, principal);
        if (resultado.hasErrors()) {
            prepararFormulario(model, dto, "/emprendedor/emprendimientos/" + id,
                    "Editar " + e.getNombre(), e.getPlan().isPermiteVideoPitch());
            return "emprendimientos/formulario";
        }
        ResultadoNotificado r = emprendimientoService.actualizar(id, dto, usuarioService.usuarioActual(principal));
        flash.addFlashAttribute("mensaje", r.usuariosNotificados() > 0
                ? "Cambios guardados. Avisamos a " + r.usuariosNotificados() + " persona(s) interesada(s)."
                : "Cambios guardados.");
        return "redirect:/emprendedor";
    }

    @PostMapping("/emprendimientos/{id}/eliminar")
    public String eliminar(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        emprendimientoService.eliminar(id, usuarioService.usuarioActual(principal));
        flash.addFlashAttribute("mensaje", "Emprendimiento eliminado.");
        return "redirect:/emprendedor";
    }

    /** Carga el emprendimiento y comprueba que pertenezca al emprendedor logueado. */
    private Emprendimiento buscarPropio(Long id, Principal principal) {
        Emprendimiento e = emprendimientoService.buscarPorId(id);
        if (!emprendimientoService.puedeGestionar(e, usuarioService.usuarioActual(principal))) {
            throw new AccessDeniedException("No es tu emprendimiento");
        }
        return e;
    }

    private void prepararFormulario(Model model, EmprendimientoDTO dto, String accion,
                                    String titulo, boolean permiteVideo) {
        model.addAttribute("emprendimiento", dto);
        model.addAttribute("accion", accion);
        model.addAttribute("titulo", titulo);
        model.addAttribute("permiteVideo", permiteVideo);
        model.addAttribute("volver", "/emprendedor");
        model.addAttribute("categorias", categoriaService.listarActivas());
        model.addAttribute("etapas", EtapaEmprendimiento.values());
    }
}
