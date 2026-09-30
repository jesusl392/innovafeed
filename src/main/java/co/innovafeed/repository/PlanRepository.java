package co.innovafeed.repository;

import co.innovafeed.model.CodigoPlan;
import co.innovafeed.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByCodigo(CodigoPlan codigo);

    List<Plan> findAllByOrderByPrioridadAsc();
}
