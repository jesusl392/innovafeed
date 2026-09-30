package co.innovafeed.service;

/**
 * Se lanza cuando se busca algo que no existe (se mostrará como página 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
