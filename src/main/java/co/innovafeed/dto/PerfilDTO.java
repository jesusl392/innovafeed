package co.innovafeed.dto;

import co.innovafeed.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos editables del perfil. El correo y el rol no se cambian desde aquí.
 */
@Getter
@Setter
public class PerfilDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 80, message = "La ciudad no puede superar 80 caracteres")
    private String ciudad;

    public static PerfilDTO desde(Usuario u) {
        PerfilDTO dto = new PerfilDTO();
        dto.setNombre(u.getNombre());
        dto.setCiudad(u.getCiudad());
        return dto;
    }
}
