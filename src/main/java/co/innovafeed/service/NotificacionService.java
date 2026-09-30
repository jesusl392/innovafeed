package co.innovafeed.service;

import co.innovafeed.model.*;
import co.innovafeed.repository.NotificacionRepository;
import co.innovafeed.repository.UsuarioRepository;
import co.innovafeed.service.notificacion.NotificationSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Genera las notificaciones automáticas y maneja la bandeja del usuario.
 *
 * Reglas:
 * 1. Cuando un emprendimiento se ACTIVA → se notifica a todos los usuarios
 *    interesados en su categoría (cualquier plan).
 * 2. Cuando un emprendimiento ACTIVO actualiza su perfil → se notifica solo si su plan
 *    tiene el beneficio "notificarAlActualizar" (Visible e Impulso).
 */
@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    /** Spring inyecta aquí todas las clases que implementan NotificationSender. */
    private final List<NotificationSender> canales;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               UsuarioRepository usuarioRepository,
                               List<NotificationSender> canales) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.canales = canales;
    }

    // ---------------------------------------------------------------
    // Generación automática
    // ---------------------------------------------------------------

    /** Regla 1. Devuelve cuántos usuarios fueron notificados. */
    @Transactional
    public int notificarNuevoEmprendimiento(Emprendimiento e) {
        if (e.getEstado() != EstadoEmprendimiento.ACTIVO) {
            return 0;
        }
        String mensaje = "Nuevo emprendimiento en " + e.getCategoria().getNombre() + ": "
                + e.getNombre() + " (" + e.getCiudad() + ")";
        return notificarInteresados(e, TipoNotificacion.NUEVO_EMPRENDIMIENTO, mensaje);
    }

    /** Regla 2. Devuelve cuántos usuarios fueron notificados (0 si el plan no lo permite). */
    @Transactional
    public int notificarActualizacion(Emprendimiento e) {
        if (e.getEstado() != EstadoEmprendimiento.ACTIVO || !e.getPlan().isNotificarAlActualizar()) {
            return 0;
        }
        String mensaje = e.getNombre() + " actualizó su perfil. ¡Mira las novedades!";
        return notificarInteresados(e, TipoNotificacion.ACTUALIZACION_PERFIL, mensaje);
    }

    private int notificarInteresados(Emprendimiento e, TipoNotificacion tipo, String mensaje) {
        List<Usuario> interesados = usuarioRepository.findInteresadosEn(e.getCategoria(), Rol.USUARIO);
        for (Usuario usuario : interesados) {
            Notificacion n = new Notificacion();
            n.setUsuario(usuario);
            n.setEmprendimiento(e);
            n.setTipo(tipo);
            n.setMensaje(mensaje);
            n.setFecha(LocalDateTime.now());
            // Se entrega por cada canal disponible (hoy: solo dentro de la app)
            for (NotificationSender canal : canales) {
                canal.enviar(n);
            }
        }
        return interesados.size();
    }

    // ---------------------------------------------------------------
    // Bandeja del usuario
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Notificacion> listar(Usuario usuario) {
        return notificacionRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> ultimas(Usuario usuario) {
        return notificacionRepository.findTop5ByUsuarioOrderByFechaDesc(usuario);
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Usuario usuario) {
        return notificacionRepository.countByUsuarioAndLeidaFalse(usuario);
    }

    /**
     * Marca una notificación como leída y la devuelve.
     * Solo funciona si la notificación es del usuario (si no, responde "no encontrada").
     */
    @Transactional
    public Notificacion marcarLeida(Long id, Usuario usuario) {
        Notificacion n = notificacionRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada"));
        n.setLeida(true);
        return n;
    }

    @Transactional
    public int marcarTodasLeidas(Usuario usuario) {
        return notificacionRepository.marcarTodasLeidas(usuario);
    }
}
