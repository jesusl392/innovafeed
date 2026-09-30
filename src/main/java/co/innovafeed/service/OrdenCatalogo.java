package co.innovafeed.service;

import co.innovafeed.model.Emprendimiento;

import java.util.Comparator;
import java.util.Set;

/**
 * Regla de orden del catálogo (de mayor a menor visibilidad):
 *
 *   1. Tiene un boost activo      → primero
 *   2. Prioridad del plan         → Impulso (3) > Visible (2) > Gratis (1)
 *   3. Fecha de creación          → los más recientes primero
 *
 * Está en una clase aparte para poder probarla sola (fase 8) y explicarla fácil.
 * La prioridad se lee de la tabla planes, así que se puede cambiar sin tocar este código.
 */
public final class OrdenCatalogo {

    private OrdenCatalogo() {
    }

    /**
     * @param idsConBoostActivo ids de los emprendimientos que tienen un boost vigente ahora mismo
     */
    public static Comparator<Emprendimiento> comparador(Set<Long> idsConBoostActivo) {
        // Boolean se ordena false < true: "no tiene boost = false" queda antes → los boosteados primero
        Comparator<Emprendimiento> porBoost =
                Comparator.comparing(e -> !idsConBoostActivo.contains(e.getId()));

        Comparator<Emprendimiento> porPrioridadDelPlan =
                Comparator.comparingInt((Emprendimiento e) -> e.getPlan().getPrioridad()).reversed();

        Comparator<Emprendimiento> porMasReciente =
                Comparator.comparing(Emprendimiento::getFechaCreacion, Comparator.reverseOrder());

        return porBoost.thenComparing(porPrioridadDelPlan).thenComparing(porMasReciente);
    }
}
