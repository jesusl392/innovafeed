package co.innovafeed.config;

import co.innovafeed.service.SuscripcionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Tareas automáticas. Hoy hay una sola: cada hora devuelve a GRATIS
 * los emprendimientos cuya suscripción de 30 días ya venció.
 */
@Configuration
@EnableScheduling
public class TareasProgramadas {

    private final SuscripcionService suscripcionService;

    public TareasProgramadas(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    /** Se ejecuta al minuto de iniciar y luego cada hora. */
    @Scheduled(initialDelayString = "PT1M", fixedRateString = "PT1H")
    public void revisarSuscripcionesVencidas() {
        suscripcionService.degradarVencidas();
    }
}
