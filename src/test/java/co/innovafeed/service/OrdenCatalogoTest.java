package co.innovafeed.service;

import co.innovafeed.model.Emprendimiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static co.innovafeed.DatosDePrueba.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regla de negocio: boost activo > Impulso > Visible > Gratis > más recientes.
 * Es una prueba unitaria pura: no necesita base de datos ni Spring.
 */
@DisplayName("Orden del catálogo")
class OrdenCatalogoTest {

    private List<String> ordenar(List<Emprendimiento> lista, Set<Long> idsConBoost) {
        List<Emprendimiento> copia = new ArrayList<>(lista);
        copia.sort(OrdenCatalogo.comparador(idsConBoost));
        return copia.stream().map(Emprendimiento::getNombre).toList();
    }

    @Test
    @DisplayName("Sin boosts: Impulso > Visible > Gratis")
    void sinBoostGanaElPlanDeMayorPrioridad() {
        var gratis = emprendimiento(1L, "Gratis", planGratis(), 0);        // el más reciente...
        var visible = emprendimiento(2L, "Visible", planVisible(), 5);
        var impulso = emprendimiento(3L, "Impulso", planImpulso(), 10);    // ...y el más antiguo

        assertThat(ordenar(List.of(gratis, visible, impulso), Set.of()))
                .containsExactly("Impulso", "Visible", "Gratis");
    }

    @Test
    @DisplayName("Un boost activo pone primero incluso a un plan Gratis")
    void boostActivoVaPrimeroAunqueSeaPlanGratis() {
        var impulso = emprendimiento(1L, "Impulso", planImpulso(), 0);
        var gratisConBoost = emprendimiento(2L, "GratisConBoost", planGratis(), 30);

        assertThat(ordenar(List.of(impulso, gratisConBoost), Set.of(2L)))
                .containsExactly("GratisConBoost", "Impulso");
    }

    @Test
    @DisplayName("Con el mismo plan, el más reciente va primero")
    void mismoPlanOrdenaPorMasReciente() {
        var viejo = emprendimiento(1L, "Viejo", planGratis(), 20);
        var nuevo = emprendimiento(2L, "Nuevo", planGratis(), 1);
        var medio = emprendimiento(3L, "Medio", planGratis(), 7);

        assertThat(ordenar(List.of(viejo, nuevo, medio), Set.of()))
                .containsExactly("Nuevo", "Medio", "Viejo");
    }

    @Test
    @DisplayName("Dos con boost: se desempata por plan y luego por fecha")
    void dosConBoostSeDesempatanPorPlanYFecha() {
        var visibleBoost = emprendimiento(1L, "VisibleBoost", planVisible(), 0);
        var impulsoBoost = emprendimiento(2L, "ImpulsoBoost", planImpulso(), 9);
        var gratisBoostNuevo = emprendimiento(3L, "GratisBoostNuevo", planGratis(), 1);
        var gratisBoostViejo = emprendimiento(4L, "GratisBoostViejo", planGratis(), 8);

        assertThat(ordenar(List.of(gratisBoostViejo, visibleBoost, gratisBoostNuevo, impulsoBoost),
                Set.of(1L, 2L, 3L, 4L)))
                .containsExactly("ImpulsoBoost", "VisibleBoost", "GratisBoostNuevo", "GratisBoostViejo");
    }

    @Test
    @DisplayName("Orden completo del enunciado: boost > Impulso > Visible > Gratis > recientes")
    void ordenCompleto() {
        var gratisViejo = emprendimiento(1L, "GratisViejo", planGratis(), 15);
        var gratisNuevo = emprendimiento(2L, "GratisNuevo", planGratis(), 2);
        var visible = emprendimiento(3L, "Visible", planVisible(), 4);
        var impulso = emprendimiento(4L, "Impulso", planImpulso(), 6);
        var gratisConBoost = emprendimiento(5L, "GratisConBoost", planGratis(), 20);

        assertThat(ordenar(List.of(gratisViejo, gratisNuevo, visible, impulso, gratisConBoost), Set.of(5L)))
                .containsExactly("GratisConBoost", "Impulso", "Visible", "GratisNuevo", "GratisViejo");
    }

    @Test
    @DisplayName("La prioridad sale de la tabla de planes: si el admin la cambia, el orden cambia")
    void laPrioridadEsConfigurable() {
        var gratis = emprendimiento(1L, "Gratis", planGratis(), 0);
        var visible = emprendimiento(2L, "Visible", planVisible(), 0);
        visible.getPlan().setPrioridad(0);   // el admin baja la prioridad de Visible por debajo de Gratis

        assertThat(ordenar(List.of(visible, gratis), Set.of()))
                .containsExactly("Gratis", "Visible");
    }
}
