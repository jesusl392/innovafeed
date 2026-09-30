package co.innovafeed.service;

import co.innovafeed.dto.PerfilDTO;
import co.innovafeed.dto.RegistroDTO;
import co.innovafeed.model.Categoria;
import co.innovafeed.model.Rol;
import co.innovafeed.model.Usuario;
import co.innovafeed.repository.CategoriaRepository;
import co.innovafeed.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio de las cuentas de usuario.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Reemplaza los intereses del usuario por las categorías elegidas.
     * Solo se aceptan categorías activas (se ignoran ids inválidos o desactivados).
     */
    @Transactional
    public void actualizarIntereses(Long usuarioId, List<Long> categoriaIds) {
        Usuario usuario = buscarPorId(usuarioId);
        List<Categoria> elegidas = categoriaIds == null ? List.of()
                : categoriaRepository.findAllById(categoriaIds).stream().filter(Categoria::isActiva).toList();
        usuario.getIntereses().clear();
        usuario.getIntereses().addAll(elegidas);
    }

    @Transactional
    public void actualizarPerfil(Long usuarioId, PerfilDTO dto) {
        Usuario usuario = buscarPorId(usuarioId);
        usuario.setNombre(dto.getNombre().trim());
        usuario.setCiudad(dto.getCiudad().trim());
    }

    /**
     * Registra un USUARIO o EMPRENDEDOR nuevo. La contraseña se cifra con BCrypt.
     */
    @Transactional
    public Usuario registrar(RegistroDTO dto) {
        if (dto.getTipoCuenta() == Rol.ADMIN) {
            // Nadie puede auto-registrarse como administrador
            throw new ReglaNegocioException("Tipo de cuenta no permitido");
        }
        if (!dto.getPassword().equals(dto.getConfirmarPassword())) {
            throw new ReglaNegocioException("Las contraseñas no coinciden");
        }
        String email = dto.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setCiudad(dto.getCiudad().trim());
        usuario.setRol(dto.getTipoCuenta());
        usuario.setAceptaTratamientoDatos(true);
        usuario.setFechaAceptacionDatos(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    /**
     * Usuario con sesión iniciada, o null si es un visitante anónimo.
     * "principal" es el objeto con el que Spring Security representa al usuario logueado.
     */
    @Transactional(readOnly = true)
    public Usuario usuarioActual(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarioRepository.findByEmailIgnoreCase(principal.getName()).orElse(null);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAllByOrderByFechaRegistroDesc();
    }

    /** El admin activa o desactiva una cuenta. No puede desactivarse a sí mismo. */
    @Transactional
    public void cambiarEstado(Long id, boolean activo, String emailAdmin) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getEmail().equalsIgnoreCase(emailAdmin)) {
            throw new ReglaNegocioException("No puedes desactivar tu propia cuenta");
        }
        usuario.setActivo(activo);
    }
}
