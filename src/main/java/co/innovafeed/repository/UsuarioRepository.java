package co.innovafeed.repository;

import co.innovafeed.model.Categoria;
import co.innovafeed.model.Rol;
import co.innovafeed.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla usuarios. Spring Data genera la consulta SQL
 * a partir del nombre del método (p. ej. findByEmailIgnoreCase).
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByActivoTrue();

    List<Usuario> findAllByOrderByFechaRegistroDesc();

    List<Usuario> findByRol(Rol rol);

    /** Usuarios que tienen esta categoría entre sus intereses. */
    List<Usuario> findByInteresesContaining(Categoria categoria);

    /** Destinatarios de notificaciones: usuarios ACTIVOS de un rol, interesados en la categoría. */
    @Query("""
            select distinct u from Usuario u join u.intereses c
            where c = :categoria and u.activo = true and u.rol = :rol
            """)
    List<Usuario> findInteresadosEn(@Param("categoria") Categoria categoria, @Param("rol") Rol rol);
}
