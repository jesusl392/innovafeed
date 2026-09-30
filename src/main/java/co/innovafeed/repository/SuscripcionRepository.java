package co.innovafeed.repository;

import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    List<Suscripcion> findByEmprendimientoOrderByFechaInicioDesc(Emprendimiento emprendimiento);

    /** La suscripción vigente (la que termina más tarde y aún no ha vencido). */
    Optional<Suscripcion> findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(Emprendimiento emprendimiento,
                                                                                      LocalDateTime ahora);

    void deleteByEmprendimiento(Emprendimiento emprendimiento);
}
