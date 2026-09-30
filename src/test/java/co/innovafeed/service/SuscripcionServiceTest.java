package co.innovafeed.service;

import co.innovafeed.model.*;
import co.innovafeed.repository.EmprendimientoRepository;
import co.innovafeed.repository.SuscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static co.innovafeed.DatosDePrueba.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Suscripciones con pago simulado")
class SuscripcionServiceTest {

    @Mock private SuscripcionRepository suscripcionRepository;
    @Mock private EmprendimientoRepository emprendimientoRepository;
    @Mock private EmprendimientoService emprendimientoService;
    @Mock private PlanService planService;

    private SuscripcionService servicio;
    private Usuario laura;
    private Emprendimiento aprendeXR;

    @BeforeEach
    void preparar() {
        servicio = new SuscripcionService(suscripcionRepository, emprendimientoRepository,
                emprendimientoService, planService, 30);
        laura = usuario(1L, "Laura", Rol.EMPRENDEDOR);
        aprendeXR = emprendimiento(3L, "AprendeXR", planGratis(), 0);
        when(emprendimientoService.buscarPorId(3L)).thenReturn(aprendeXR);
        when(emprendimientoService.puedeGestionar(aprendeXR, laura)).thenReturn(true);
    }

    @Test
    @DisplayName("Suscribirse a Visible cambia el plan y registra 30 días con el monto del plan")
    void suscribirseAVisible() {
        when(planService.buscarPorCodigo(CodigoPlan.VISIBLE)).thenReturn(planVisible());
        when(suscripcionRepository.findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(any(), any()))
                .thenReturn(Optional.empty());

        String mensaje = servicio.cambiarPlan(3L, CodigoPlan.VISIBLE, laura);

        assertThat(aprendeXR.getPlan().getCodigo()).isEqualTo(CodigoPlan.VISIBLE);
        ArgumentCaptor<Suscripcion> captor = ArgumentCaptor.forClass(Suscripcion.class);
        verify(suscripcionRepository).save(captor.capture());
        Suscripcion s = captor.getValue();
        assertThat(Duration.between(s.getFechaInicio(), s.getFechaFin())).isEqualTo(Duration.ofDays(30));
        assertThat(s.getMontoSimulado()).isEqualByComparingTo(new BigDecimal("9.00"));
        assertThat(mensaje).contains("Pago simulado");
    }

    @Test
    @DisplayName("No se puede pagar dos veces el mismo plan mientras esté vigente")
    void mismoPlanVigenteSeRechaza() {
        aprendeXR.setPlan(planVisible());
        when(planService.buscarPorCodigo(CodigoPlan.VISIBLE)).thenReturn(planVisible());
        when(suscripcionRepository.findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(any(), any()))
                .thenReturn(Optional.of(suscripcion(10)));

        assertThatThrownBy(() -> servicio.cambiarPlan(3L, CodigoPlan.VISIBLE, laura))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya tienes el plan Visible");
        verify(suscripcionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Subir de Visible a Impulso cierra la suscripción anterior")
    void cambiarDePlanCierraLaAnterior() {
        aprendeXR.setPlan(planVisible());
        Suscripcion anterior = suscripcion(20);
        when(planService.buscarPorCodigo(CodigoPlan.IMPULSO)).thenReturn(planImpulso());
        when(suscripcionRepository.findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(any(), any()))
                .thenReturn(Optional.of(anterior));

        servicio.cambiarPlan(3L, CodigoPlan.IMPULSO, laura);

        assertThat(anterior.isVigente()).isFalse();
        assertThat(aprendeXR.getPlan().getCodigo()).isEqualTo(CodigoPlan.IMPULSO);
    }

    @Test
    @DisplayName("Volver a Gratis quita el plan pago de inmediato")
    void volverAGratis() {
        aprendeXR.setPlan(planImpulso());
        Suscripcion vigente = suscripcion(15);
        when(planService.buscarPorCodigo(CodigoPlan.GRATIS)).thenReturn(planGratis());
        when(suscripcionRepository.findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(any(), any()))
                .thenReturn(Optional.of(vigente));

        servicio.cambiarPlan(3L, CodigoPlan.GRATIS, laura);

        assertThat(aprendeXR.getPlan().getCodigo()).isEqualTo(CodigoPlan.GRATIS);
        assertThat(vigente.isVigente()).isFalse();
    }

    /** Suscripción que empezó hoy y vence dentro de "diasRestantes". */
    private Suscripcion suscripcion(int diasRestantes) {
        Suscripcion s = new Suscripcion();
        s.setFechaInicio(LocalDateTime.now().minusMinutes(1));
        s.setFechaFin(LocalDateTime.now().plusDays(diasRestantes));
        return s;
    }
}
