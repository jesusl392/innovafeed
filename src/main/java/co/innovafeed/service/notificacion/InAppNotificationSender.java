package co.innovafeed.service.notificacion;

import co.innovafeed.model.Notificacion;
import co.innovafeed.repository.NotificacionRepository;
import org.springframework.stereotype.Component;

/**
 * Canal "dentro de la app": guarda la notificación en la base de datos
 * para que aparezca en la bandeja del usuario.
 */
@Component
public class InAppNotificationSender implements NotificationSender {

    private final NotificacionRepository notificacionRepository;

    public InAppNotificationSender(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    public void enviar(Notificacion notificacion) {
        notificacionRepository.save(notificacion);
    }
}
