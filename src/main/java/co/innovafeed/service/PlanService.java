package co.innovafeed.service;

import co.innovafeed.dto.PlanDTO;
import co.innovafeed.model.CodigoPlan;
import co.innovafeed.model.Plan;
import co.innovafeed.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Consulta y configuración de planes. Precio y beneficios viven en la tabla "planes",
 * por eso el admin los puede cambiar sin tocar el código.
 */
@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional(readOnly = true)
    public List<Plan> listarPlanes() {
        return planRepository.findAllByOrderByPrioridadAsc();
    }

    @Transactional(readOnly = true)
    public Plan buscarPorCodigo(CodigoPlan codigo) {
        return planRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado: " + codigo));
    }

    @Transactional(readOnly = true)
    public Plan buscarPorId(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado"));
    }

    /** El admin cambia precio y beneficios. Aplica de inmediato a todos los emprendimientos del plan. */
    @Transactional
    public Plan actualizar(Long id, PlanDTO dto) {
        Plan p = buscarPorId(id);
        p.setNombre(dto.getNombre().trim());
        p.setDescripcion(dto.getDescripcion());
        p.setPrecioMensual(dto.getPrecioMensual());
        p.setPrioridad(dto.getPrioridad());
        p.setBoostsPorMes(dto.getBoostsPorMes());
        p.setBadge(dto.getBadge() == null || dto.getBadge().isBlank() ? null : dto.getBadge().trim());
        p.setVerVisitasSemana(dto.isVerVisitasSemana());
        p.setEstadisticasAvanzadas(dto.isEstadisticasAvanzadas());
        p.setNotificarAlActualizar(dto.isNotificarAlActualizar());
        p.setApareceEnRecomendados(dto.isApareceEnRecomendados());
        p.setPermiteVideoPitch(dto.isPermiteVideoPitch());
        return p;
    }
}
