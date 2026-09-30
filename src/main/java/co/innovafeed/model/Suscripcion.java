package co.innovafeed.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro de cada "pago simulado" de un plan para un emprendimiento.
 * Dura 30 días; al vencer, el emprendimiento vuelve al plan GRATIS.
 */
@Entity
@Table(name = "suscripciones")
@Getter
@Setter
@NoArgsConstructor
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "emprendimiento_id")
    private Emprendimiento emprendimiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    /** Precio del plan en el momento de suscribirse (no se cobra de verdad). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoSimulado;

    public boolean isVigente() {
        LocalDateTime ahora = LocalDateTime.now();
        return !ahora.isBefore(fechaInicio) && ahora.isBefore(fechaFin);
    }
}
