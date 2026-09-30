package co.innovafeed.service;

import co.innovafeed.dto.CategoriaDTO;
import co.innovafeed.model.Categoria;
import co.innovafeed.model.Usuario;
import co.innovafeed.repository.CategoriaRepository;
import co.innovafeed.repository.EmprendimientoRepository;
import co.innovafeed.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD de categorías (solo admin) y consultas para formularios y filtros.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final EmprendimientoRepository emprendimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            EmprendimientoRepository emprendimientoRepository,
                            UsuarioRepository usuarioRepository) {
        this.categoriaRepository = categoriaRepository;
        this.emprendimientoRepository = emprendimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarActivas() {
        return categoriaRepository.findByActivaTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }

    @Transactional
    public Categoria crear(CategoriaDTO dto) {
        String nombre = dto.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
        }
        return categoriaRepository.save(new Categoria(nombre, dto.getDescripcion()));
    }

    @Transactional
    public Categoria actualizar(Long id, CategoriaDTO dto) {
        Categoria categoria = buscarPorId(id);
        String nombre = dto.getNombre().trim();
        categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
                });
        categoria.setNombre(nombre);
        categoria.setDescripcion(dto.getDescripcion());
        return categoria;
    }

    /** Activa/desactiva. Una categoría inactiva no aparece en formularios ni filtros. */
    @Transactional
    public void cambiarActiva(Long id, boolean activa) {
        buscarPorId(id).setActiva(activa);
    }

    /**
     * Solo se puede borrar si ningún emprendimiento la usa; si no, se sugiere desactivarla.
     * También se quita de los intereses de los usuarios.
     */
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        if (emprendimientoRepository.existsByCategoria(categoria)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar: hay emprendimientos en esta categoría. Puedes desactivarla.");
        }
        for (Usuario u : usuarioRepository.findByInteresesContaining(categoria)) {
            u.getIntereses().remove(categoria);
        }
        categoriaRepository.delete(categoria);
    }
}
