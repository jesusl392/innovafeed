package co.innovafeed.service;

import co.innovafeed.model.CodigoPlan;
import co.innovafeed.model.Emprendimiento;
import co.innovafeed.repository.EmprendimientoRepository;
import co.innovafeed.repository.SuscripcionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prueba de integración (con base de datos H2 real y los datos del DataLoader):
 * cuando la suscripción de un emprendimiento vence, vuelve al plan GRATIS.
 * @Transactional deshace los cambios al terminar la prueba.
 */
@SpringBootTest
@Transactional
class SuscripcionVencimientoTest {

    @Autowired
    private SuscripcionService suscripcionService;
    @Autowired
    private EmprendimientoRepository emprendimientoRepository;
    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Test
    void emprendimientoConSuscripcionVencidaVuelveAGratis() {
        Emprendimiento ecoPack = buscar("EcoPackCo");   // plan IMPULSO en los datos de prueba
        Emprendimiento mediIA = buscar("MediIA");       // plan VISIBLE, suscripción vigente
        assertEquals(CodigoPlan.IMPULSO, ecoPack.getPlan().getCodigo());

        // Simulamos que pasaron los 30 días: la suscripción de EcoPackCo terminó ayer
        suscripcionRepository.findByEmprendimientoOrderByFechaInicioDesc(ecoPack)
                .forEach(s -> s.setFechaFin(LocalDateTime.now().minusDays(1)));
        suscripcionRepository.flush();

        int degradados = suscripcionService.degradarVencidas();

        assertEquals(1, degradados);
        assertEquals(CodigoPlan.GRATIS, ecoPack.getPlan().getCodigo());
        assertEquals(CodigoPlan.VISIBLE, mediIA.getPlan().getCodigo(), "MediIA sigue vigente");
    }

    private Emprendimiento buscar(String nombre) {
        return emprendimientoRepository.findAll().stream()
                .filter(e -> e.getNombre().equals(nombre))
                .findFirst().orElseThrow();
    }
}
