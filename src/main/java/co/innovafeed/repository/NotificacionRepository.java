package co.innovafeed.repository;

import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.Notificacion;
import co.innovafeed.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioOrderByFechaDesc(Usuario usuario);

    List<Notificacion> findTop5ByUsuarioOrderByFechaDesc(Usuario usuario);

    /** Busca por id Y dueño: así nadie puede tocar notificaciones de otro usuario. */
    Optional<Notificacion> findByIdAndUsuario(Long id, Usuario usuario);

    long countByUsuarioAndLeidaFalse(Usuario usuario);

    long countByLeidaFalse();

    /** "Marcar todas como leídas" en una sola sentencia UPDATE. */
    @Modifying
    @Query("update Notificacion n set n.leida = true where n.usuario = :usuario and n.leida = false")
    int marcarTodasLeidas(@Param("usuario") Usuario usuario);

    void deleteByEmprendimiento(Emprendimiento emprendimiento);
}
