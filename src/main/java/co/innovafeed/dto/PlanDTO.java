package co.innovafeed.dto;

import co.innovafeed.model.Plan;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Formulario del admin para configurar precio y beneficios de un plan.
 * El código (GRATIS/VISIBLE/IMPULSO) no se edita.
 */
@Getter
@Setter
public class PlanDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Digits(integer = 6, fraction = 2, message = "Precio inválido (máximo 2 decimales)")
    private BigDecimal precioMensual;

    @NotNull(message = "La prioridad es obligatoria")
    @Min(value = 1, message = "La prioridad mínima es 1")
    @Max(value = 100, message = "La prioridad máxima es 100")
    private Integer prioridad;

    @NotNull(message = "Indica cuántos boosts incluye")
    @Min(value = 0, message = "No puede ser negativo")
    @Max(value = 30, message = "Máximo 30 boosts por mes")
    private Integer boostsPorMes;

    @Size(max = 40, message = "El badge no puede superar 40 caracteres")
    private String badge;

    private boolean verVisitasSemana;
    private boolean estadisticasAvanzadas;
    private boolean notificarAlActualizar;
    private boolean apareceEnRecomendados;
    private boolean permiteVideoPitch;

    public static PlanDTO desde(Plan p) {
        PlanDTO dto = new PlanDTO();
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecioMensual(p.getPrecioMensual());
        dto.setPrioridad(p.getPrioridad());
        dto.setBoostsPorMes(p.getBoostsPorMes());
        dto.setBadge(p.getBadge());
        dto.setVerVisitasSemana(p.isVerVisitasSemana());
        dto.setEstadisticasAvanzadas(p.isEstadisticasAvanzadas());
        dto.setNotificarAlActualizar(p.isNotificarAlActualizar());
        dto.setApareceEnRecomendados(p.isApareceEnRecomendados());
        dto.setPermiteVideoPitch(p.isPermiteVideoPitch());
        return dto;
    }
}
