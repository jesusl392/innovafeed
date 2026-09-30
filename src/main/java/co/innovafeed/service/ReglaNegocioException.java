package co.innovafeed.service;

/**
 * Error esperado de negocio (p. ej. "el correo ya está registrado").
 * El mensaje está en español y se puede mostrar directamente al usuario.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
