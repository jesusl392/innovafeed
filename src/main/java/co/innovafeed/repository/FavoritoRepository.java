package co.innovafeed.repository;

import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.Favorito;
import co.innovafeed.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    boolean existsByUsuarioAndEmprendimiento(Usuario usuario, Emprendimiento emprendimiento);

    Optional<Favorito> findByUsuarioAndEmprendimiento(Usuario usuario, Emprendimiento emprendimiento);

    List<Favorito> findByUsuarioOrderByFechaDesc(Usuario usuario);

    long countByEmprendimiento(Emprendimiento emprendimiento);

    void deleteByEmprendimiento(Emprendimiento emprendimiento);
}
