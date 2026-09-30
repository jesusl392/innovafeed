package co.innovafeed.service;

import co.innovafeed.dto.RegistroDTO;
import co.innovafeed.model.Rol;
import co.innovafeed.model.Usuario;
import co.innovafeed.repository.CategoriaRepository;
import co.innovafeed.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Registro de usuarios (seguridad y Ley 1581)")
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CategoriaRepository categoriaRepository;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
    private UsuarioService servicio;

    @BeforeEach
    void preparar() {
        servicio = new UsuarioService(usuarioRepository, categoriaRepository, bcrypt);
    }

    private RegistroDTO registro(Rol tipo) {
        RegistroDTO dto = new RegistroDTO();
        dto.setNombre("  Paula Ríos ");
        dto.setEmail("  Paula@Correo.CO ");
        dto.setPassword("Secreta123*");
        dto.setConfirmarPassword("Secreta123*");
        dto.setCiudad("Pasto");
        dto.setTipoCuenta(tipo);
        dto.setAceptaTratamientoDatos(true);
        return dto;
    }

    @Test
    @DisplayName("La contraseña se guarda cifrada con BCrypt, nunca en texto plano")
    void cifraLaContrasena() {
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario u = servicio.registrar(registro(Rol.EMPRENDEDOR));

        assertThat(u.getPassword()).isNotEqualTo("Secreta123*").startsWith("$2a$");
        assertThat(bcrypt.matches("Secreta123*", u.getPassword())).isTrue();
        assertThat(u.getEmail()).isEqualTo("paula@correo.co");
        assertThat(u.getNombre()).isEqualTo("Paula Ríos");
        assertThat(u.getRol()).isEqualTo(Rol.EMPRENDEDOR);
        assertThat(u.isAceptaTratamientoDatos()).isTrue();
        assertThat(u.getFechaAceptacionDatos()).isNotNull();
    }

    @Test
    @DisplayName("Nadie puede registrarse como ADMIN desde el formulario")
    void noSePuedeRegistrarComoAdmin() {
        assertThatThrownBy(() -> servicio.registrar(registro(Rol.ADMIN)))
                .isInstanceOf(ReglaNegocioException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("No se permiten dos cuentas con el mismo correo")
    void correoDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase("paula@correo.co")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(registro(Rol.USUARIO)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe una cuenta");
    }

    @Test
    @DisplayName("Las contraseñas deben coincidir")
    void contrasenasDistintas() {
        RegistroDTO dto = registro(Rol.USUARIO);
        dto.setConfirmarPassword("OtraCosa1*");

        assertThatThrownBy(() -> servicio.registrar(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no coinciden");
    }
}
