package co.innovafeed.service;

import co.innovafeed.dto.Conteo;
import co.innovafeed.dto.EstadisticasEmprendimiento;
import co.innovafeed.dto.MetricasAdmin;
import co.innovafeed.model.*;
import co.innovafeed.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Métricas de la plataforma:
 * - Registra cada visita a un perfil (fecha, usuario si inició sesión, ciudad del usuario).
 * - Calcula las estadísticas del emprendedor según los beneficios de su plan.
 * - Calcula las métricas del dashboard del admin.
 */
@Service
public class EstadisticaService {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("EEE d", new Locale("es", "CO"));
    private static final String SIN_SESION = "Visitantes sin sesión";

    private final VisitaRepository visitaRepository;
    private final FavoritoRepository favoritoRepository;
    private final EmprendimientoRepository emprendimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;
    private final EmprendimientoService emprendimientoService;

    public EstadisticaService(VisitaRepository visitaRepository, FavoritoRepository favoritoRepository,
                              EmprendimientoRepository emprendimientoRepository, UsuarioRepository usuarioRepository,
                              NotificacionRepository notificacionRepository,
                              EmprendimientoService emprendimientoService) {
        this.visitaRepository = visitaRepository;
        this.favoritoRepository = favoritoRepository;
        this.emprendimientoRepository = emprendimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
        this.emprendimientoService = emprendimientoService;
    }

    // ---------------------------------------------------------------
    // Registro de visitas
    // ---------------------------------------------------------------

    /**
     * Guarda una visita al perfil. No se cuentan las visitas del propio dueño ni del admin,
     * para que las estadísticas reflejen el interés real del público.
     */
    @Transactional
    public void registrarVisita(Emprendimiento e, Usuario visitante) {
        if (e.getEstado() != EstadoEmprendimiento.ACTIVO || emprendimientoService.puedeGestionar(e, visitante)) {
            return;
        }
        Visita v = new Visita();
        v.setEmprendimiento(e);
        v.setUsuario(visitante);                                        // null si no inició sesión
        v.setCiudad(visitante != null ? visitante.getCiudad() : null);
        v.setFecha(LocalDateTime.now());
        visitaRepository.save(v);
    }

    @Transactional(readOnly = true)
    public long visitasTotales(Emprendimiento e) {
        return visitaRepository.countByEmprendimiento(e);
    }

    // ---------------------------------------------------------------
    // Estadísticas del emprendedor
    // ---------------------------------------------------------------

    /**
     * Estadísticas de un emprendimiento. Solo las calcula el dueño (o el admin),
     * y cada sección depende de los beneficios del plan actual.
     */
    @Transactional(readOnly = true)
    public EstadisticasEmprendimiento estadisticasDe(Long emprendimientoId, Usuario usuario) {
        Emprendimiento e = emprendimientoService.buscarPorId(emprendimientoId);
        if (!emprendimientoService.puedeGestionar(e, usuario)) {
            throw new AccessDeniedException("No es tu emprendimiento");
        }
        Plan plan = e.getPlan();

        // Todos los planes: visitas totales y guardados
        long totales = visitaRepository.countByEmprendimiento(e);
        long guardados = favoritoRepository.countByEmprendimiento(e);

        // Visible e Impulso: visitas de la semana, día por día
        long semana = 0;
        List<Conteo> porDia = List.of();
        if (plan.isVerVisitasSemana()) {
            LocalDate hoy = LocalDate.now();
            LocalDateTime desde = hoy.minusDays(6).atStartOfDay();
            List<LocalDateTime> fechas = visitaRepository.fechasDesde(e, desde);
            semana = fechas.size();
            porDia = agruparPorDia(fechas, hoy);
        }

        // Impulso: por ciudad y por intereses de los visitantes
        List<Conteo> porCiudad = List.of();
        List<Conteo> porIntereses = List.of();
        if (plan.isEstadisticasAvanzadas()) {
            porCiudad = visitaRepository.contarPorCiudad(e).stream()
                    .map(c -> c.etiqueta() == null ? new Conteo(SIN_SESION, c.cantidad()) : c)
                    .toList();
            porIntereses = visitaRepository.contarPorInteresesDeVisitantes(e);
        }

        return new EstadisticasEmprendimiento(totales, guardados,
                plan.isVerVisitasSemana(), semana, porDia,
                plan.isEstadisticasAvanzadas(), porCiudad, porIntereses);
    }

    /** Convierte una lista de fechas en 7 conteos (uno por día, del más antiguo a hoy). */
    private List<Conteo> agruparPorDia(List<LocalDateTime> fechas, LocalDate hoy) {
        Map<LocalDate, Long> porFecha = fechas.stream()
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));
        List<Conteo> resultado = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoy.minusDays(i);
            resultado.add(new Conteo(dia.format(DIA), porFecha.getOrDefault(dia, 0L)));
        }
        return resultado;
    }

    // ---------------------------------------------------------------
    // Dashboard del admin
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public MetricasAdmin metricasAdmin() {
        return new MetricasAdmin(
                emprendimientoRepository.count(),
                emprendimientoRepository.countByEstado(EstadoEmprendimiento.ACTIVO),
                emprendimientoRepository.countByEstado(EstadoEmprendimiento.PENDIENTE),
                usuarioRepository.countByActivoTrue(),
                notificacionRepository.countByLeidaFalse(),
                visitaRepository.count(),
                visitaRepository.masVisitados(PageRequest.of(0, 5)));
    }

    /** Visitas totales de varios emprendimientos a la vez (para la tabla del panel). */
    @Transactional(readOnly = true)
    public Map<Long, Long> visitasPorEmprendimiento(List<Emprendimiento> lista) {
        return lista.stream().collect(Collectors.toMap(Emprendimiento::getId,
                e -> visitaRepository.countByEmprendimiento(e)));
    }
}
