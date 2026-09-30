package co.innovafeed.service;

import co.innovafeed.model.*;
import co.innovafeed.repository.BoostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static co.innovafeed.DatosDePrueba.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Boosts de 48 horas")
class BoostServiceTest {

    @Mock private BoostRepository boostRepository;
    @Mock private EmprendimientoService emprendimientoService;

    private BoostService servicio;
    private Usuario laura;

    @BeforeEach
    void preparar() {
        servicio = new BoostService(boostRepository, emprendimientoService, 48);
        laura = usuario(1L, "Laura", Rol.EMPRENDEDOR);
    }

    private Emprendimiento propio(Plan plan) {
        Emprendimiento e = emprendimiento(7L, "EcoPackCo", plan, 0);
        when(emprendimientoService.buscarPorId(7L)).thenReturn(e);
        when(emprendimientoService.puedeGestionar(e, laura)).thenReturn(true);
        return e;
    }

    @Test
    @DisplayName("Con plan Visible se activa un boost que dura 48 horas")
    void activaBoostDe48Horas() {
        propio(planVisible());
        when(boostRepository.findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(boostRepository.countByEmprendimientoAndFechaInicioGreaterThanEqual(any(), any())).thenReturn(0L);
        when(boostRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Boost b = servicio.activar(7L, laura);

        assertThat(Duration.between(b.getFechaInicio(), b.getFechaFin())).isEqualTo(Duration.ofHours(48));
        assertThat(b.isActivo()).isTrue();
    }

    @Test
    @DisplayName("El plan Gratis no incluye boosts")
    void gratisNoTieneBoosts() {
        propio(planGratis());
        when(boostRepository.findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.activar(7L, laura))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no incluye boosts");
        verify(boostRepository, never()).save(any());
    }

    @Test
    @DisplayName("Visible tiene 1 boost al mes: el segundo se rechaza")
    void respetaElLimiteMensual() {
        Emprendimiento e = propio(planVisible());
        when(boostRepository.findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(boostRepository.countByEmprendimientoAndFechaInicioGreaterThanEqual(eq(e), any())).thenReturn(1L);

        assertThatThrownBy(() -> servicio.activar(7L, laura))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya usaste");
    }

    @Test
    @DisplayName("No se puede activar un boost mientras otro sigue activo")
    void unBoostALaVez() {
        propio(planImpulso());
        Boost activo = new Boost();
        activo.setFechaInicio(LocalDateTime.now().minusHours(1));
        activo.setFechaFin(LocalDateTime.now().plusHours(47));
        when(boostRepository.findFirstByEmprendimientoAndFechaFinAfterOrderByFechaFinDesc(any(), any()))
                .thenReturn(Optional.of(activo));

        assertThatThrownBy(() -> servicio.activar(7L, laura))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya tienes un boost activo");
    }

    @Test
    @DisplayName("Solo se pueden impulsar emprendimientos activos")
    void soloActivos() {
        Emprendimiento e = propio(planImpulso());
        e.setEstado(EstadoEmprendimiento.PENDIENTE);

        assertThatThrownBy(() -> servicio.activar(7L, laura))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("activos");
    }

    @Test
    @DisplayName("Impulso: 3 boosts al mes, si usó 1 le quedan 2")
    void disponiblesEsteMes() {
        Emprendimiento e = emprendimiento(7L, "EcoPackCo", planImpulso(), 0);
        when(boostRepository.countByEmprendimientoAndFechaInicioGreaterThanEqual(eq(e), any())).thenReturn(1L);

        assertThat(servicio.disponiblesEsteMes(e)).isEqualTo(2);
    }
}
