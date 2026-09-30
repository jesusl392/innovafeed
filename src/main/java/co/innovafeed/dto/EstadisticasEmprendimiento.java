package co.innovafeed.dto;

import java.util.List;

/**
 * Estadísticas que ve el emprendedor. Las secciones que su plan no incluye
 * llegan vacías (el servicio ni siquiera las calcula): así no se filtran datos.
 *
 * @param verSemana     el plan permite ver las visitas de la semana (Visible, Impulso)
 * @param avanzadas     el plan permite ver ciudad e intereses (Impulso)
 */
public record EstadisticasEmprendimiento(
        long visitasTotales,
        long guardados,
        boolean verSemana,
        long visitasSemana,
        List<Conteo> ultimos7Dias,
        boolean avanzadas,
        List<Conteo> porCiudad,
        List<Conteo> porIntereses) {

    public long maximoPorDia() {
        return ultimos7Dias.stream().mapToLong(Conteo::cantidad).max().orElse(0);
    }

    public long maximoPorCiudad() {
        return porCiudad.stream().mapToLong(Conteo::cantidad).max().orElse(0);
    }

    public long maximoPorIntereses() {
        return porIntereses.stream().mapToLong(Conteo::cantidad).max().orElse(0);
    }
}
