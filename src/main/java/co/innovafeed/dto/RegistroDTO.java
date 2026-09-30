package co.innovafeed.dto;

import co.innovafeed.model.Rol;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos del formulario de registro. Usamos un DTO (y no la entidad Usuario)
 * para que el formulario no pueda modificar campos como "rol=ADMIN" o "activo".
 */
@Getter
@Setter
public class RegistroDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String email;

    // Un solo mensaje: si viene vacía, también falla @Size
    @NotNull(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    private String password;

    @NotBlank(message = "Confirma tu contraseña")
    private String confirmarPassword;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 80, message = "La ciudad no puede superar 80 caracteres")
    private String ciudad;

    /** Solo se aceptan USUARIO o EMPRENDEDOR; el servicio rechaza ADMIN. */
    @NotNull(message = "Elige el tipo de cuenta")
    private Rol tipoCuenta = Rol.USUARIO;

    @AssertTrue(message = "Debes autorizar el tratamiento de tus datos personales (Ley 1581 de 2012)")
    private boolean aceptaTratamientoDatos;
}
