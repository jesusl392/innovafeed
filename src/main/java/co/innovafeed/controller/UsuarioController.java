package co.innovafeed.controller;

import co.innovafeed.model.Notificacion;
import co.innovafeed.model.Usuario;
import co.innovafeed.service.CategoriaService;
import co.innovafeed.service.EmprendimientoService;
import co.innovafeed.service.FavoritoService;
import co.innovafeed.service.NotificacionService;
import co.innovafeed.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Zona del USUARIO: resumen, intereses, favoritos y bandeja de notificaciones.
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;
    private final FavoritoService favoritoService;
    private final NotificacionService notificacionService;
    private final EmprendimientoService emprendimientoService;

    public UsuarioController(UsuarioService usuarioService, CategoriaService categoriaService,
                             FavoritoService favoritoService, NotificacionService notificacionService,
                             EmprendimientoService emprendimientoService) {
        this.emprendimientoService = emprendimientoService;
        this.usuarioService = usuarioService;
        this.categoriaService = categoriaService;
        this.favoritoService = favoritoService;
        this.notificacionService = notificacionService;
    }

    /** "Mi espacio": resumen de intereses, favoritos y últimas notificaciones. */
    @GetMapping
    public String inicio(Principal principal, Model model) {
        Usuario yo = usuarioService.usuarioActual(principal);
        model.addAttribute("usuario", yo);
        model.addAttribute("ultimasNotificaciones", notificacionService.ultimas(yo));
        model.addAttribute("totalFavoritos", favoritoService.listar(yo).size());
        model.addAttribute("recomendados", emprendimientoService.recomendados(yo, 6));
        model.addAttribute("idsConBoost", emprendimientoService.idsConBoostActivo());
        return "usuario/inicio";
    }

    // --------------------------- Intereses ---------------------------

    @GetMapping("/intereses")
    public String intereses(Principal principal, Model model) {
        Usuario yo = usuarioService.usuarioActual(principal);
        model.addAttribute("categorias", categoriaService.listarActivas());
        model.addAttribute("idsElegidos", yo.getIntereses().stream().map(c -> c.getId()).toList());
        return "usuario/intereses";
    }

    /** Recibe los ids de las casillas marcadas (puede venir vacío = sin intereses). */
    @PostMapping("/intereses")
    public String guardarIntereses(@RequestParam(name = "categorias", required = false) List<Long> categorias,
                                   Principal principal, RedirectAttributes flash) {
        Usuario yo = usuarioService.usuarioActual(principal);
        usuarioService.actualizarIntereses(yo.getId(), categorias);
        flash.addFlashAttribute("mensaje",
                "Intereses guardados. Te avisaremos cuando aparezcan emprendimientos de estas categorías.");
        return "redirect:/usuario/intereses";
    }

    // --------------------------- Favoritos ---------------------------

    @GetMapping("/favoritos")
    public String favoritos(Principal principal, Model model) {
        model.addAttribute("favoritos", favoritoService.listar(usuarioService.usuarioActual(principal)));
        return "usuario/favoritos";
    }

    /** Botón "Guardar / Quitar" del perfil del emprendimiento. */
    @PostMapping("/favoritos/{id}/alternar")
    public String alternarFavorito(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        boolean guardado = favoritoService.alternar(usuarioService.usuarioActual(principal), id);
        flash.addFlashAttribute("mensaje", guardado ? "Guardado en tus favoritos." : "Quitado de tus favoritos.");
        return "redirect:/emprendimientos/" + id;
    }

    /** Botón "Quitar" de la lista de favoritos. */
    @PostMapping("/favoritos/{id}/quitar")
    public String quitarFavorito(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        favoritoService.quitar(usuarioService.usuarioActual(principal), id);
        flash.addFlashAttribute("mensaje", "Quitado de tus favoritos.");
        return "redirect:/usuario/favoritos";
    }

    // ------------------------- Notificaciones -------------------------

    @GetMapping("/notificaciones")
    public String notificaciones(Principal principal, Model model) {
        model.addAttribute("notificaciones", notificacionService.listar(usuarioService.usuarioActual(principal)));
        return "usuario/notificaciones";
    }

    /** Abrir una notificación: se marca como leída y lleva al emprendimiento. */
    @PostMapping("/notificaciones/{id}/abrir")
    public String abrirNotificacion(@PathVariable Long id, Principal principal) {
        Notificacion n = notificacionService.marcarLeida(id, usuarioService.usuarioActual(principal));
        return "redirect:/emprendimientos/" + n.getEmprendimiento().getId();
    }

    @PostMapping("/notificaciones/leer-todas")
    public String leerTodas(Principal principal, RedirectAttributes flash) {
        int marcadas = notificacionService.marcarTodasLeidas(usuarioService.usuarioActual(principal));
        flash.addFlashAttribute("mensaje", marcadas + " notificación(es) marcadas como leídas.");
        return "redirect:/usuario/notificaciones";
    }
}
