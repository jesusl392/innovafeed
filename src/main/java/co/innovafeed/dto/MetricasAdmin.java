package co.innovafeed.dto;

import java.util.List;

/**
 * Métricas del dashboard del administrador.
 */
public record MetricasAdmin(
        long totalEmprendimientos,
        long emprendimientosActivos,
        long emprendimientosPendientes,
        long usuariosActivos,
        long notificacionesSinLeer,
        long visitasTotales,
        List<Conteo> masVisitados) {

    public long maximoVisitado() {
        return masVisitados.stream().mapToLong(Conteo::cantidad).max().orElse(0);
    }
}
