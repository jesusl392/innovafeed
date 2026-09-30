package co.innovafeed.service;

import co.innovafeed.model.Emprendimiento;

/**
 * Resultado de una operación que puede disparar notificaciones:
 * el emprendimiento afectado y cuántos usuarios fueron notificados.
 */
public record ResultadoNotificado(Emprendimiento emprendimiento, int usuariosNotificados) {
}
