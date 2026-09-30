package co.innovafeed;

import co.innovafeed.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fábrica de objetos para las pruebas unitarias (sin base de datos).
 * Los planes replican la configuración del DataLoader.
 */
public final class DatosDePrueba {

    private DatosDePrueba() {
    }

    public static Plan planGratis() {
        return plan(CodigoPlan.GRATIS, "Gratis", "0", 1, 0, false);
    }

    public static Plan planVisible() {
        Plan p = plan(CodigoPlan.VISIBLE, "Visible", "9.00", 2, 1, true);
        p.setVerVisitasSemana(true);
        return p;
    }

    public static Plan planImpulso() {
        Plan p = plan(CodigoPlan.IMPULSO, "Impulso", "19.00", 3, 3, true);
        p.setVerVisitasSemana(true);
        p.setEstadisticasAvanzadas(true);
        p.setApareceEnRecomendados(true);
        p.setPermiteVideoPitch(true);
        return p;
    }

    private static Plan plan(CodigoPlan codigo, String nombre, String precio, int prioridad,
                             int boosts, boolean notificarAlActualizar) {
        Plan p = new Plan();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setPrecioMensual(new BigDecimal(precio));
        p.setPrioridad(prioridad);
        p.setBoostsPorMes(boosts);
        p.setNotificarAlActualizar(notificarAlActualizar);
        return p;
    }

    public static Categoria categoria(Long id, String nombre) {
        Categoria c = new Categoria(nombre, "Descripción de " + nombre);
        c.setId(id);
        return c;
    }

    public static Usuario usuario(Long id, String nombre, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre(nombre);
        u.setEmail(nombre.toLowerCase() + "@innovafeed.test");
        u.setCiudad("Bogotá");
        u.setRol(rol);
        return u;
    }

    /**
     * Emprendimiento ACTIVO. "haceDias" define su fecha de creación (0 = hoy, 5 = hace 5 días).
     */
    public static Emprendimiento emprendimiento(Long id, String nombre, Plan plan, int haceDias) {
        Emprendimiento e = new Emprendimiento();
        e.setId(id);
        e.setNombre(nombre);
        e.setDescripcion("Descripción de " + nombre);
        e.setCiudad("Medellín");
        e.setFundador("Fundador de " + nombre);
        e.setEtapa(EtapaEmprendimiento.MVP);
        e.setEstado(EstadoEmprendimiento.ACTIVO);
        e.setCategoria(categoria(1L, "Tecnología"));
        e.setPropietario(usuario(100L, "Dueno", Rol.EMPRENDEDOR));
        e.setPlan(plan);
        e.setFechaCreacion(LocalDateTime.now().minusDays(haceDias));
        return e;
    }
}
