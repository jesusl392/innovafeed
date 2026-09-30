package co.innovafeed.service;

import co.innovafeed.model.Emprendimiento;
import co.innovafeed.model.Favorito;
import co.innovafeed.model.Usuario;
import co.innovafeed.repository.FavoritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Emprendimientos guardados por los usuarios. Cuántas personas guardaron un
 * emprendimiento también es una métrica para el emprendedor.
 */
@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final EmprendimientoService emprendimientoService;

    public FavoritoService(FavoritoRepository favoritoRepository, EmprendimientoService emprendimientoService) {
        this.favoritoRepository = favoritoRepository;
        this.emprendimientoService = emprendimientoService;
    }

    /**
     * Si no estaba guardado lo guarda; si ya estaba, lo quita.
     * Devuelve true si quedó guardado. Solo se pueden guardar emprendimientos visibles.
     */
    @Transactional
    public boolean alternar(Usuario usuario, Long emprendimientoId) {
        Emprendimiento e = emprendimientoService.buscarPerfilPublico(emprendimientoId, usuario);
        var existente = favoritoRepository.findByUsuarioAndEmprendimiento(usuario, e);
        if (existente.isPresent()) {
            favoritoRepository.delete(existente.get());
            return false;
        }
        favoritoRepository.save(new Favorito(usuario, e));
        return true;
    }

    @Transactional
    public void quitar(Usuario usuario, Long emprendimientoId) {
        Emprendimiento e = emprendimientoService.buscarPorId(emprendimientoId);
        favoritoRepository.findByUsuarioAndEmprendimiento(usuario, e).ifPresent(favoritoRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<Favorito> listar(Usuario usuario) {
        return favoritoRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    @Transactional(readOnly = true)
    public boolean esFavorito(Usuario usuario, Emprendimiento e) {
        return usuario != null && favoritoRepository.existsByUsuarioAndEmprendimiento(usuario, e);
    }

    @Transactional(readOnly = true)
    public long contarGuardados(Emprendimiento e) {
        return favoritoRepository.countByEmprendimiento(e);
    }
}
