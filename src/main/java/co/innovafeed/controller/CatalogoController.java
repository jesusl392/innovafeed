package co.innovafeed.controller;

import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.Usuario;
import co.innovafeed.service.CategoriaService;
import co.innovafeed.service.EmprendimientoService;
import co.innovafeed.service.EstadisticaService;
import co.innovafeed.service.FavoritoService;
import co.innovafeed.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

/**
 * Catálogo público: cualquiera (con o sin sesión) puede explorar.
 */
@Controller
public class CatalogoController {

    private final EmprendimientoService emprendimientoService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;
    private final FavoritoService favoritoService;
    private final EstadisticaService estadisticaService;

    public CatalogoController(EmprendimientoService emprendimientoService, CategoriaService categoriaService,
                              UsuarioService usuarioService, FavoritoService favoritoService,
                              EstadisticaService estadisticaService) {
        this.estadisticaService = estadisticaService;
        this.emprendimientoService = emprendimientoService;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
        this.favoritoService = favoritoService;
    }

    /** /catalogo?categoria=3&q=eco&page=1  (page empieza en 0) */
    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) Long categoria,
                           @RequestParam(required = false) String q,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Page<Emprendimiento> pagina = emprendimientoService.buscarCatalogo(categoria, q, page);
        model.addAttribute("pagina", pagina);
        model.addAttribute("emprendimientos", pagina.getContent());
        model.addAttribute("idsConBoost", emprendimientoService.idsConBoostActivo());
        model.addAttribute("categorias", categoriaService.listarActivas());
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("q", q);
        return "catalogo/lista";
    }

    /**
     * Perfil público de un emprendimiento. Se registra UNA visita por sesión de navegador:
     * recargar la página (o volver tras pulsar "Guardar") no infla las estadísticas.
     */
    @GetMapping("/emprendimientos/{id}")
    public String detalle(@PathVariable Long id, Principal principal, HttpSession sesion, Model model) {
        Usuario visitante = usuarioService.usuarioActual(principal);
        Emprendimiento e = emprendimientoService.buscarPerfilPublico(id, visitante);

        @SuppressWarnings("unchecked")
        Set<Long> visitados = (Set<Long>) sesion.getAttribute("emprendimientosVisitados");
        if (visitados == null) {
            visitados = new HashSet<>();
            sesion.setAttribute("emprendimientosVisitados", visitados);
        }
        if (visitados.add(e.getId())) {   // add() devuelve true solo la primera vez
            estadisticaService.registrarVisita(e, visitante);
        }
        model.addAttribute("e", e);
        model.addAttribute("puedeGestionar", emprendimientoService.puedeGestionar(e, visitante));
        model.addAttribute("esFavorito", favoritoService.esFavorito(visitante, e));
        model.addAttribute("vecesGuardado", favoritoService.contarGuardados(e));
        model.addAttribute("idsConBoost", emprendimientoService.idsConBoostActivo());
        return "catalogo/detalle";
    }
}
