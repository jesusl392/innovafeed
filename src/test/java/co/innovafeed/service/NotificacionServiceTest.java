package co.innovafeed.service;

import co.innovafeed.model.*;
import co.innovafeed.repository.NotificacionRepository;
import co.innovafeed.repository.UsuarioRepository;
import co.innovafeed.service.notificacion.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static co.innovafeed.DatosDePrueba.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas de las notificaciones automáticas con Mockito:
 * los repositorios y los canales de envío son "simulados" (mocks),
 * así probamos SOLO la lógica del servicio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Notificaciones automáticas")
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private NotificationSender canal;

    private NotificacionService servicio;

    private Usuario ana;
    private Usuario mateo;

    @BeforeEach
    void preparar() {
        servicio = new NotificacionService(notificacionRepository, usuarioRepository, List.of(canal));
        ana = usuario(1L, "Ana", Rol.USUARIO);
        mateo = usuario(2L, "Mateo", Rol.USUARIO);
    }

    // ------------------------ Al activar ------------------------

    @Test
    @DisplayName("Al activarse, notifica a cada usuario interesado en la categoría")
    void alActivarseNotificaACadaInteresado() {
        Emprendimiento finflow = emprendimiento(10L, "FinFlow", planGratis(), 0);
        when(usuarioRepository.findInteresadosEn(finflow.getCategoria(), Rol.USUARIO)).thenReturn(List.of(ana, mateo));

        int notificados = servicio.notificarNuevoEmprendimiento(finflow);

        assertThat(notificados).isEqualTo(2);
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(canal, times(2)).enviar(captor.capture());

        List<Notificacion> enviadas = captor.getAllValues();
        assertThat(enviadas).extracting(Notificacion::getUsuario).containsExactly(ana, mateo);
        assertThat(enviadas).allSatisfy(n -> {
            assertThat(n.getTipo()).isEqualTo(TipoNotificacion.NUEVO_EMPRENDIMIENTO);
            assertThat(n.getEmprendimiento()).isEqualTo(finflow);
            assertThat(n.getMensaje()).contains("FinFlow").contains("Tecnología");
            assertThat(n.isLeida()).isFalse();
            assertThat(n.getFecha()).isNotNull();
        });
    }

    @Test
    @DisplayName("Un emprendimiento que no está ACTIVO no genera notificaciones")
    void noActivoNoNotifica() {
        Emprendimiento pendiente = emprendimiento(10L, "Pendiente", planImpulso(), 0);
        pendiente.setEstado(EstadoEmprendimiento.PENDIENTE);

        assertThat(servicio.notificarNuevoEmprendimiento(pendiente)).isZero();
        verifyNoInteractions(usuarioRepository, canal);
    }

    @Test
    @DisplayName("Si nadie está interesado en la categoría, no se envía nada")
    void sinInteresadosNoSeEnviaNada() {
        Emprendimiento e = emprendimiento(10L, "Solitario", planGratis(), 0);
        when(usuarioRepository.findInteresadosEn(any(), any())).thenReturn(List.of());

        assertThat(servicio.notificarNuevoEmprendimiento(e)).isZero();
        verifyNoInteractions(canal);
    }

    // ------------------------ Al actualizar ------------------------

    @Test
    @DisplayName("Al actualizar el perfil con plan Gratis NO se notifica (no es un beneficio del plan)")
    void actualizarConPlanGratisNoNotifica() {
        Emprendimiento gratis = emprendimiento(10L, "FoodRoute", planGratis(), 0);

        assertThat(servicio.notificarActualizacion(gratis)).isZero();
        verifyNoInteractions(usuarioRepository, canal);
    }

    @Test
    @DisplayName("Al actualizar el perfil con plan Visible SÍ se notifica a los interesados")
    void actualizarConPlanVisibleNotifica() {
        Emprendimiento visible = emprendimiento(10L, "MediIA", planVisible(), 0);
        when(usuarioRepository.findInteresadosEn(visible.getCategoria(), Rol.USUARIO)).thenReturn(List.of(ana));

        assertThat(servicio.notificarActualizacion(visible)).isEqualTo(1);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(canal).enviar(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoNotificacion.ACTUALIZACION_PERFIL);
        assertThat(captor.getValue().getMensaje()).contains("MediIA actualizó su perfil");
    }

    @Test
    @DisplayName("Actualizar un emprendimiento suspendido no notifica, aunque tenga plan pago")
    void actualizarSuspendidoNoNotifica() {
        Emprendimiento e = emprendimiento(10L, "Suspendido", planImpulso(), 0);
        e.setEstado(EstadoEmprendimiento.SUSPENDIDO);

        assertThat(servicio.notificarActualizacion(e)).isZero();
        verifyNoInteractions(canal);
    }

    // ------------------------ Canales ------------------------

    @Test
    @DisplayName("Cada notificación se entrega por TODOS los canales (app hoy, email en el futuro)")
    void seEntregaPorTodosLosCanales() {
        NotificationSender canalEmail = mock(NotificationSender.class);
        servicio = new NotificacionService(notificacionRepository, usuarioRepository, List.of(canal, canalEmail));
        Emprendimiento e = emprendimiento(10L, "EcoPackCo", planImpulso(), 0);
        when(usuarioRepository.findInteresadosEn(any(), any())).thenReturn(List.of(ana, mateo));

        servicio.notificarNuevoEmprendimiento(e);

        verify(canal, times(2)).enviar(any());
        verify(canalEmail, times(2)).enviar(any());
    }

    // ------------------------ Bandeja ------------------------

    @Test
    @DisplayName("Marcar como leída una notificación de OTRO usuario responde 'no encontrada'")
    void noSePuedeMarcarNotificacionAjena() {
        when(notificacionRepository.findByIdAndUsuario(99L, ana)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.marcarLeida(99L, ana))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("Marcar como leída cambia el estado de la notificación")
    void marcarLeida() {
        Notificacion n = new Notificacion();
        n.setUsuario(ana);
        when(notificacionRepository.findByIdAndUsuario(5L, ana)).thenReturn(Optional.of(n));

        servicio.marcarLeida(5L, ana);

        assertThat(n.isLeida()).isTrue();
    }

    @Test
    @DisplayName("'Marcar todas como leídas' devuelve cuántas se marcaron")
    void marcarTodas() {
        when(notificacionRepository.marcarTodasLeidas(ana)).thenReturn(3);

        assertThat(servicio.marcarTodasLeidas(ana)).isEqualTo(3);
    }
}
