package co.innovafeed.model;

/**
 * Identificador fijo de cada plan. OJO: aquí solo está el "nombre clave";
 * el precio y los beneficios viven en la tabla {@link Plan} y son configurables.
 */
public enum CodigoPlan {
    GRATIS,
    VISIBLE,
    IMPULSO
}
