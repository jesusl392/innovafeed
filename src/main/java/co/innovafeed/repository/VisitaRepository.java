package co.innovafeed.repository;

import co.innovafeed.dto.Conteo;
import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.Visita;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Consultas de estadísticas sobre las visitas a los perfiles.
 */
public interface VisitaRepository extends JpaRepository<Visita, Long> {

    long countByEmprendimiento(Emprendimiento emprendimiento);

    long countByEmprendimientoAndFechaGreaterThanEqual(Emprendimiento emprendimiento, LocalDateTime desde);

    /** Fechas de las visitas desde un momento dado (para agruparlas por día en Java). */
    @Query("select v.fecha from Visita v where v.emprendimiento = :e and v.fecha >= :desde")
    List<LocalDateTime> fechasDesde(@Param("e") Emprendimiento e, @Param("desde") LocalDateTime desde);

    /** Visitas agrupadas por ciudad del visitante (ciudad null = visitante sin sesión). */
    @Query("""
            select new co.innovafeed.dto.Conteo(v.ciudad, count(v))
            from Visita v where v.emprendimiento = :e
            group by v.ciudad order by count(v) desc
            """)
    List<Conteo> contarPorCiudad(@Param("e") Emprendimiento e);

    /**
     * Cuántos visitantes distintos (con sesión) tienen cada categoría entre sus intereses.
     * Responde: "¿qué le interesa a la gente que visita mi perfil?".
     */
    @Query("""
            select new co.innovafeed.dto.Conteo(c.nombre, count(distinct u.id))
            from Visita v join v.usuario u join u.intereses c
            where v.emprendimiento = :e
            group by c.nombre order by count(distinct u.id) desc
            """)
    List<Conteo> contarPorInteresesDeVisitantes(@Param("e") Emprendimiento e);

    /** Emprendimientos más visitados de la plataforma (para el dashboard del admin). */
    @Query("""
            select new co.innovafeed.dto.Conteo(v.emprendimiento.nombre, count(v))
            from Visita v
            group by v.emprendimiento.id, v.emprendimiento.nombre order by count(v) desc
            """)
    List<Conteo> masVisitados(Pageable limite);

    void deleteByEmprendimiento(Emprendimiento emprendimiento);
}
