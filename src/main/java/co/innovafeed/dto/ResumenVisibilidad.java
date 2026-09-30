package co.innovafeed.dto;

import co.innovafeed.model.Boost;
import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.Suscripcion;

/**
 * Datos que muestra el panel del emprendedor por cada emprendimiento:
 * su suscripción vigente, su boost activo y cuántos boosts le quedan este mes.
 * suscripcion y boostActivo pueden ser null.
 */
public record ResumenVisibilidad(Emprendimiento emprendimiento, Suscripcion suscripcion,
                                 Boost boostActivo, int boostsDisponibles) {
}
