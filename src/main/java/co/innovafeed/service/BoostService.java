package co.innovafeed.service;

import co.innovafeed.model.Boost;
import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.EstadoEmprendimiento;
import co.innovafeed.model.Usuario;
import co.innovafeed.repository.BoostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Boosts: ponen un emprendimiento de PRIMERO en el catálogo durante N horas (48 por defecto).
 * Cada plan define cuántos boosts se pueden usar por mes calendario (Gratis 0, Visible 1, Impulso 3).
 */
@Service
public class BoostService {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BoostRepository boostRepository;
    private final EmprendimientoService emprendimientoService;
    private final int horasBoost;

    public BoostService(BoostRepository boostRepository, EmprendimientoService emprendimientoService,
                        @Value("${innovafeed.boost.horas:48}") int horasBoost) {
        this.boostRepository = boostRepository;
        this.emprendimientoService = emprendimientoService;
        this.horasBoost = horasBoost;
    }

    @Transactional
    public Boost activar(Long emprendimientoId, Usuario usuario) {
        Emprendimiento e = emprendimientoService.buscarPorId(emprendimientoId);
        if (!emprendimientoService.puedeGestionar(e, usuario)) {
            throw new AccessDeniedException("No es tu emprendimiento");
        }
        if (e.getEstado() != EstadoEmprendimiento.ACTIVO) {
            throw new ReglaNegocioException("Solo puedes impulsar emprendimientos activos.");
        }
        Optional<Boost> activo = boostActivo(e);
        if (activo.isPresent()) {
            throw new ReglaNegocioException("Ya tienes un boost activo hasta el "
                    + activo.get().getFechaFin().format(FECHA_HORA) + ".");
        }
        if (disponiblesEsteMes(e) <= 0) {
            throw new ReglaNegocioException(e.getPlan().getBoostsPorMes() == 0
                    ? "Tu plan no incluye boosts. Mejora a Visible o Impulso."
                    : "Ya usaste los " + e.getPlan().getBoostsPorMes() + " boost(s) de este mes.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        Boost b = new Boost();
        b.setEmprendimiento(e);
        b.setFechaInicio(ahora);
        b.setFechaFin(ahora.plusHours(horasBoost));
        return boostRepository.save(b);
    }

    @Transactional(readOnly = true)
    public Optional<Boost> boostActivo(Emprendimiento e) {
        return boostRepository.findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(e, LocalDateTime.now());
    }

    /** Boosts que le quedan en el mes calendario actual según su plan. */
    @Transactional(readOnly = true)
    public int disponiblesEsteMes(Emprendimiento e) {
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long usados = boostRepository.countByEmprendimientoAndFechaInicioGreaterThanEqual(e, inicioMes);
        return (int) Math.max(0, e.getPlan().getBoostsPorMes() - usados);
    }

    public int getHorasBoost() {
        return horasBoost;
    }
}
