package co.innovafeed.repository;

import co.innovafeed.model.Categoria;
import co.innovafeed.model.CodigoPlan;
import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.EstadoEmprendimiento;
import co.innovafeed.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a emprendimientos. El orden final del catálogo (boost > plan > fecha)
 * se aplica en EmprendimientoService con {@link co.innovafeed.service.OrdenCatalogo}.
 */
public interface EmprendimientoRepository extends JpaRepository<Emprendimiento, Long> {

    List<Emprendimiento> findByPropietarioOrderByFechaCreacionDesc(Usuario propietario);

    List<Emprendimiento> findAllByOrderByFechaCreacionDesc();

    List<Emprendimiento> findByEstadoOrderByFechaCreacionDesc(EstadoEmprendimiento estado);

    long countByEstado(EstadoEmprendimiento estado);

    boolean existsByCategoria(Categoria categoria);

    /** Candidatos para la sección "Recomendados": su plan tiene ese beneficio. */
    List<Emprendimiento> findByEstadoAndPlan_ApareceEnRecomendadosTrue(EstadoEmprendimiento estado);

    /**
     * Emprendimientos con plan pago que ya NO tienen una suscripción vigente
     * (se les venció el mes y deben volver a GRATIS).
     */
    @Query("""
            select e from Emprendimiento e
            where e.plan.codigo <> :gratis
              and not exists (select s from Suscripcion s
                              where s.emprendimiento = e and s.fechaFin > :ahora)
            """)
    List<Emprendimiento> buscarConPlanVencido(@Param("gratis") CodigoPlan gratis,
                                              @Param("ahora") LocalDateTime ahora);

    /**
     * Catálogo: emprendimientos en un estado cuyo nombre, descripción, tags o ciudad
     * contienen el texto. "texto" llega ya en minúsculas y con comodines: "%eco%".
     */
    @Query("""
            select e from Emprendimiento e
            where e.estado = :estado
              and (lower(e.nombre) like :texto
                   or lower(e.descripcion) like :texto
                   or lower(coalesce(e.tags, '')) like :texto
                   or lower(e.ciudad) like :texto)
            order by e.fechaCreacion desc
            """)
    List<Emprendimiento> buscarPorTexto(@Param("estado") EstadoEmprendimiento estado,
                                        @Param("texto") String texto);

    /** Igual que buscarPorTexto, pero además filtra por categoría. */
    @Query("""
            select e from Emprendimiento e
            where e.estado = :estado
              and e.categoria.id = :categoriaId
              and (lower(e.nombre) like :texto
                   or lower(e.descripcion) like :texto
                   or lower(coalesce(e.tags, '')) like :texto
                   or lower(e.ciudad) like :texto)
            order by e.fechaCreacion desc
            """)
    List<Emprendimiento> buscarPorCategoriaYTexto(@Param("estado") EstadoEmprendimiento estado,
                                                  @Param("categoriaId") Long categoriaId,
                                                  @Param("texto") String texto);
}
