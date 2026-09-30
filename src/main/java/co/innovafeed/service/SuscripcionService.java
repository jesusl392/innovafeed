package co.innovafeed.service;

import co.innovafeed.model.*;
import co.innovafeed.repository.EmprendimientoRepository;
import co.innovafeed.repository.SuscripcionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Suscripciones con PAGO SIMULADO (no hay pasarela real):
 * el botón "Suscribirme" cambia el plan y registra fechas y monto.
 * Cada suscripción dura N días (innovafeed.suscripcion.dias); al vencer, vuelve a GRATIS.
 */
@Service
public class SuscripcionService {

    private static final Logger log = LoggerFactory.getLogger(SuscripcionService.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SuscripcionRepository suscripcionRepository;
    private final EmprendimientoRepository emprendimientoRepository;
    private final EmprendimientoService emprendimientoService;
    private final PlanService planService;
    private final int diasSuscripcion;

    public SuscripcionService(SuscripcionRepository suscripcionRepository,
                              EmprendimientoRepository emprendimientoRepository,
                              EmprendimientoService emprendimientoService, PlanService planService,
                              @Value("${innovafeed.suscripcion.dias:30}") int diasSuscripcion) {
        this.suscripcionRepository = suscripcionRepository;
        this.emprendimientoRepository = emprendimientoRepository;
        this.emprendimientoService = emprendimientoService;
        this.planService = planService;
        this.diasSuscripcion = diasSuscripcion;
    }

    /**
     * Cambia el plan de un emprendimiento.
     * - A un plan pago: termina la suscripción anterior (si había) y crea una nueva de N días.
     * - A GRATIS: termina la suscripción vigente hoy mismo.
     * @return mensaje para mostrar al emprendedor
     */
    @Transactional
    public String cambiarPlan(Long emprendimientoId, CodigoPlan codigo, Usuario usuario) {
        Emprendimiento e = emprendimientoService.buscarPorId(emprendimientoId);
        if (!emprendimientoService.puedeGestionar(e, usuario)) {
            throw new AccessDeniedException("No es tu emprendimiento");
        }
        Plan nuevo = planService.buscarPorCodigo(codigo);
        LocalDateTime ahora = LocalDateTime.now();
        Optional<Suscripcion> vigente = vigente(e);

        if (codigo == CodigoPlan.GRATIS) {
            if (e.getPlan().getCodigo() == CodigoPlan.GRATIS) {
                throw new ReglaNegocioException("Ya estás en el plan Gratis.");
            }
            vigente.ifPresent(s -> s.setFechaFin(ahora));
            e.setPlan(nuevo);
            return "Volviste al plan Gratis.";
        }

        if (e.getPlan().getCodigo() == codigo && vigente.isPresent()) {
            throw new ReglaNegocioException("Ya tienes el plan " + nuevo.getNombre()
                    + " activo hasta el " + vigente.get().getFechaFin().format(FECHA) + ".");
        }

        // Cambio de plan (o renovación de uno vencido): se cierra la anterior y se abre una nueva
        vigente.ifPresent(s -> s.setFechaFin(ahora));
        Suscripcion s = new Suscripcion();
        s.setEmprendimiento(e);
        s.setPlan(nuevo);
        s.setFechaInicio(ahora);
        s.setFechaFin(ahora.plusDays(diasSuscripcion));
        s.setMontoSimulado(nuevo.getPrecioMensual());
        suscripcionRepository.save(s);
        e.setPlan(nuevo);

        return "¡Listo! Pago simulado de $" + nuevo.getPrecioMensual() + " USD registrado. "
                + e.getNombre() + " tiene el plan " + nuevo.getNombre()
                + " hasta el " + s.getFechaFin().format(FECHA) + ".";
    }

    @Transactional(readOnly = true)
    public Optional<Suscripcion> vigente(Emprendimiento e) {
        return suscripcionRepository.findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(e, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Suscripcion> historial(Emprendimiento e) {
        return suscripcionRepository.findByEmprendimientoOrderByFechaInicioDesc(e);
    }

    /**
     * Devuelve a GRATIS los emprendimientos cuya suscripción ya venció.
     * La ejecuta TareasProgramadas cada hora.
     * @return cuántos emprendimientos se degradaron
     */
    @Transactional
    public int degradarVencidas() {
        List<Emprendimiento> vencidos =
                emprendimientoRepository.buscarConPlanVencido(CodigoPlan.GRATIS, LocalDateTime.now());
        if (!vencidos.isEmpty()) {
            Plan gratis = planService.buscarPorCodigo(CodigoPlan.GRATIS);
            vencidos.forEach(e -> e.setPlan(gratis));
            log.info("{} emprendimiento(s) volvieron al plan Gratis por vencimiento.", vencidos.size());
        }
        return vencidos.size();
    }
}
