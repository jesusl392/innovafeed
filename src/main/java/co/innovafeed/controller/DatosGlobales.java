package co.innovafeed.controller;

import co.innovafeed.model.Rol;
import co.innovafeed.model.Usuario;
import co.innovafeed.service.NotificacionService;
import co.innovafeed.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;

/**
 * Datos que necesitan TODAS las páginas (por ejemplo, el contador de notificaciones
 * sin leer que aparece en el menú). Se agregan al modelo automáticamente.
 */
@ControllerAdvice
public class DatosGlobales {

    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;

    public DatosGlobales(UsuarioService usuarioService, NotificacionService notificacionService) {
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
    }

    /** Ruta actual (ej: "/catalogo"), para resaltar en el menú la página donde está el usuario. */
    @ModelAttribute("rutaActual")
    public String rutaActual(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    /**
     * URL actual SIN el parámetro "page", terminada en "?" o "&", lista para pegarle "page=N".
     * La usan los botones Anterior/Siguiente: así conservan los filtros (búsqueda, categoría, estado...).
     */
    @ModelAttribute("urlSinPagina")
    public String urlSinPagina(HttpServletRequest request) {
        String url = UriComponentsBuilder.fromPath(rutaActual(request))
                .query(request.getQueryString())
                .replaceQueryParam("page")
                .build()
                .toUriString();
        return url + (url.contains("?") ? "&" : "?");
    }

    /** Cantidad de notificaciones sin leer; 0 si no hay sesión o no es rol USUARIO. */
    @ModelAttribute("noLeidas")
    public long noLeidas(Principal principal) {
        Usuario usuario = usuarioService.usuarioActual(principal);
        if (usuario == null || usuario.getRol() != Rol.USUARIO) {
            return 0;
        }
        return notificacionService.contarNoLeidas(usuario);
    }
}
