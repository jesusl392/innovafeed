package co.innovafeed.service;

import co.innovafeed.dto.EmprendimientoDTO;
import co.innovafeed.model.*;
import co.innovafeed.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static co.innovafeed.DatosDePrueba.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Emprendimientos: catálogo, permisos y disparo de notificaciones")
class EmprendimientoServiceTest {

    @Mock private EmprendimientoRepository emprendimientoRepository;
    @Mock private CategoriaService categoriaService;
    @Mock private PlanService planService;
    @Mock private FavoritoRepository favoritoRepository;
    @Mock private VisitaRepository visitaRepository;
    @Mock private NotificacionRepository notificacionRepository;
    @Mock private SuscripcionRepository suscripcionRepository;
    @Mock private BoostRepository boostRepository;
    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private EmprendimientoService servicio;

    // ------------------------ Catálogo ------------------------

    @Test
    @DisplayName("El catálogo aplica el orden boost > plan > fecha a lo que devuelve la base de datos")
    void catalogoOrdenado() {
        var gratis = emprendimiento(1L, "Gratis", planGratis(), 0);
        var impulso = emprendimiento(2L, "Impulso", planImpulso(), 5);
        var visibleConBoost = emprendimiento(3L, "VisibleConBoost", planVisible(), 9);
        when(emprendimientoRepository.buscarPorTexto(EstadoEmprendimiento.ACTIVO, "%%"))
                .thenReturn(List.of(gratis, impulso, visibleConBoost));
        when(boostRepository.idsConBoostActivo(any())).thenReturn(Set.of(3L));

        List<Emprendimiento> resultado = servicio.buscarCatalogo(null, null);

        assertThat(resultado).extracting(Emprendimiento::getNombre)
                .containsExactly("VisibleConBoost", "Impulso", "Gratis");
    }

    @Test
    @DisplayName("La búsqueda ignora mayúsculas y espacios, y filtra por categoría si se envía")
    void busquedaNormalizaElTexto() {
        when(emprendimientoRepository.buscarPorCategoriaYTexto(EstadoEmprendimiento.ACTIVO, 3L, "%eco%"))
                .thenReturn(List.of());

        servicio.buscarCatalogo(3L, "  ECO ");

        verify(emprendimientoRepository).buscarPorCategoriaYTexto(EstadoEmprendimiento.ACTIVO, 3L, "%eco%");
    }

    @Test
    @DisplayName("Un visitante no puede ver un emprendimiento que no está activo")
    void perfilNoActivoOcultoParaVisitantes() {
        var pendiente = emprendimiento(1L, "Pendiente", planGratis(), 0);
        pendiente.setEstado(EstadoEmprendimiento.PENDIENTE);
        when(emprendimientoRepository.findById(1L)).thenReturn(Optional.of(pendiente));

        assertThatThrownBy(() -> servicio.buscarPerfilPublico(1L, usuario(5L, "Ana", Rol.USUARIO)))
                .isInstanceOf(RecursoNoEncontradoException.class);
        // pero su dueño sí lo ve
        assertThat(servicio.buscarPerfilPublico(1L, pendiente.getPropietario())).isEqualTo(pendiente);
    }

    // ------------------------ Crear y permisos ------------------------

    @Test
    @DisplayName("Un emprendimiento nuevo entra PENDIENTE y con plan GRATIS")
    void crearEntraPendienteYGratis() {
        Usuario laura = usuario(1L, "Laura", Rol.EMPRENDEDOR);
        EmprendimientoDTO dto = new EmprendimientoDTO();
        dto.setNombre("AgroSensor");
        dto.setDescripcion("Sensores para pequeños agricultores");
        dto.setCategoriaId(1L);
        dto.setEtapa(EtapaEmprendimiento.MVP);
        dto.setCiudad("Cali");
        dto.setFundador("Laura");
        dto.setVideoPitchUrl("https://video.example");   // se ignora: el plan Gratis no lo permite
        when(planService.buscarPorCodigo(CodigoPlan.GRATIS)).thenReturn(planGratis());
        when(categoriaService.buscarPorId(1L)).thenReturn(categoria(1L, "Tecnología"));
        when(emprendimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Emprendimiento e = servicio.crear(dto, laura);

        assertThat(e.getEstado()).isEqualTo(EstadoEmprendimiento.PENDIENTE);
        assertThat(e.getPlan().getCodigo()).isEqualTo(CodigoPlan.GRATIS);
        assertThat(e.getPropietario()).isEqualTo(laura);
        assertThat(e.getVideoPitchUrl()).isNull();
    }

    @Test
    @DisplayName("Un emprendedor no puede editar el emprendimiento de otro (403)")
    void noSePuedeEditarUnoAjeno() {
        var ajeno = emprendimiento(1L, "DeCarlos", planGratis(), 0);
        when(emprendimientoRepository.findById(1L)).thenReturn(Optional.of(ajeno));
        Usuario laura = usuario(2L, "Laura", Rol.EMPRENDEDOR);

        assertThatThrownBy(() -> servicio.actualizar(1L, new EmprendimientoDTO(), laura))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(notificacionService);
    }

    @Test
    @DisplayName("El admin puede gestionar cualquier emprendimiento")
    void adminGestionaTodo() {
        var e = emprendimiento(1L, "Cualquiera", planGratis(), 0);
        assertThat(servicio.puedeGestionar(e, usuario(9L, "Admin", Rol.ADMIN))).isTrue();
        assertThat(servicio.puedeGestionar(e, null)).isFalse();
    }

    // ------------------------ Cambio de estado → notificaciones ------------------------

    @Test
    @DisplayName("Pasar de PENDIENTE a ACTIVO dispara la notificación a los interesados")
    void activarNotifica() {
        var e = emprendimiento(1L, "FinFlow", planGratis(), 0);
        e.setEstado(EstadoEmprendimiento.PENDIENTE);
        when(emprendimientoRepository.findById(1L)).thenReturn(Optional.of(e));
        when(notificacionService.notificarNuevoEmprendimiento(e)).thenReturn(2);

        ResultadoNotificado r = servicio.cambiarEstado(1L, EstadoEmprendimiento.ACTIVO);

        assertThat(e.getEstado()).isEqualTo(EstadoEmprendimiento.ACTIVO);
        assertThat(r.usuariosNotificados()).isEqualTo(2);
    }

    @Test
    @DisplayName("Si ya estaba ACTIVO, volver a ponerlo ACTIVO no repite la notificación")
    void reactivarNoRepite() {
        var e = emprendimiento(1L, "EcoPackCo", planImpulso(), 0);
        when(emprendimientoRepository.findById(1L)).thenReturn(Optional.of(e));

        ResultadoNotificado r = servicio.cambiarEstado(1L, EstadoEmprendimiento.ACTIVO);

        assertThat(r.usuariosNotificados()).isZero();
        verifyNoInteractions(notificacionService);
    }

    @Test
    @DisplayName("Suspender no notifica a nadie")
    void suspenderNoNotifica() {
        var e = emprendimiento(1L, "EcoPackCo", planImpulso(), 0);
        when(emprendimientoRepository.findById(1L)).thenReturn(Optional.of(e));

        servicio.cambiarEstado(1L, EstadoEmprendimiento.SUSPENDIDO);

        assertThat(e.getEstado()).isEqualTo(EstadoEmprendimiento.SUSPENDIDO);
        verifyNoInteractions(notificacionService);
    }
}
