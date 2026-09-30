package co.innovafeed.service.notificacion;

import co.innovafeed.model.Notificacion;

/**
 * "Canal" por el que se entrega una notificación.
 *
 * Hoy existe un solo canal: {@link InAppNotificationSender} (bandeja dentro de la app).
 * Para agregar email en el futuro basta con crear otra clase, por ejemplo
 * EmailNotificationSender implements NotificationSender, y marcarla con @Component.
 * NotificacionService recibe TODOS los canales y los usa sin cambiar su código.
 */
public interface NotificationSender {

    void enviar(Notificacion notificacion);
}
