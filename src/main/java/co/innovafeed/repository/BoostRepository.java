package co.innovafeed.repository;

import co.innovafeed.model.Boost;
import co.innovafeed.model.Emprendimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BoostRepository extends JpaRepository<Boost, Long> {

    List<Boost> findByEmprendimientoOrderByFechaInicioDesc(Emprendimiento emprendimiento);

    /** Ids de todos los emprendimientos con un boost vigente en este momento. */
    @Query("select b.emprendimiento.id from Boost b where b.fechaInicio <= :ahora and b.fechaFin > :ahora")
    Set<Long> idsConBoostActivo(@Param("ahora") LocalDateTime ahora);

    /** El boost vigente de un emprendimiento (si tiene). */
    Optional<Boost> findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(Emprendimiento emprendimiento,
                                                                               LocalDateTime ahora);

    /** Cuántos boosts ha usado desde una fecha (se usa con el inicio del mes). */
    long countByEmprendimientoAndFechaInicioGreaterThanEqual(Emprendimiento emprendimiento, LocalDateTime desde);

    void deleteByEmprendimiento(Emprendimiento emprendimiento);
}
