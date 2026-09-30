package co.innovafeed.dto;

import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.EtapaEmprendimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

/**
 * Datos del formulario de emprendimiento (crear y editar).
 * No incluye estado, plan ni propietario: esos campos no los decide el formulario.
 */
@Getter
@Setter
public class EmprendimientoDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 20, max = 2000, message = "La descripción debe tener entre 20 y 2000 caracteres")
    private String descripcion;

    @NotNull(message = "Elige una categoría")
    private Long categoriaId;

    @NotNull(message = "Elige la etapa del emprendimiento")
    private EtapaEmprendimiento etapa;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 80, message = "La ciudad no puede superar 80 caracteres")
    private String ciudad;

    @NotBlank(message = "El nombre del fundador es obligatorio")
    @Size(max = 100, message = "El fundador no puede superar 100 caracteres")
    private String fundador;

    @Size(max = 500, message = "Los tags no pueden superar 500 caracteres")
    private String tags;

    @URL(message = "Ingresa una URL válida (ej: https://miemprendimiento.co)")
    @Size(max = 255, message = "La URL no puede superar 255 caracteres")
    private String url;

    /** Solo se guarda si el plan del emprendimiento lo permite (Impulso). */
    @URL(message = "Ingresa una URL válida para el video pitch")
    @Size(max = 255, message = "La URL no puede superar 255 caracteres")
    private String videoPitchUrl;

    /** Convierte una entidad en DTO para precargar el formulario de edición. */
    public static EmprendimientoDTO desde(Emprendimiento e) {
        EmprendimientoDTO dto = new EmprendimientoDTO();
        dto.setNombre(e.getNombre());
        dto.setDescripcion(e.getDescripcion());
        dto.setCategoriaId(e.getCategoria().getId());
        dto.setEtapa(e.getEtapa());
        dto.setCiudad(e.getCiudad());
        dto.setFundador(e.getFundador());
        dto.setTags(e.getTags());
        dto.setUrl(e.getUrl());
        dto.setVideoPitchUrl(e.getVideoPitchUrl());
        return dto;
    }
}
