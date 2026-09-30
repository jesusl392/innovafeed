package co.innovafeed.dto;

/**
 * Par "etiqueta → cantidad" para estadísticas (ej: "Medellín" → 12 visitas).
 * Las consultas JPQL lo crean directamente con "select new co.innovafeed.dto.Conteo(...)".
 */
public record Conteo(String etiqueta, long cantidad) {

    /** Porcentaje respecto a un máximo, para dibujar la barra en la vista (0-100). */
    public long porcentajeDe(long maximo) {
        return maximo == 0 ? 0 : Math.round(cantidad * 100.0 / maximo);
    }
}
