package co.innovafeed.controller;

import co.innovafeed.service.RecursoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Convierte excepciones de los controladores en páginas amigables.
 * (Los errores 403 los maneja Spring Security; los 500 inesperados, Spring Boot con error/500.html.)
 */
@ControllerAdvice
public class ManejadorErrores {

    /** Algo que no existe, o una URL con un id inválido (ej: /emprendimientos/abc) → 404. */
    @ExceptionHandler({RecursoNoEncontradoException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado() {
        return "error/404";
    }
}
