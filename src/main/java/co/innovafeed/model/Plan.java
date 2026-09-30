package co.innovafeed.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Plan de visibilidad. Precio y beneficios son columnas de la tabla, así que se
 * pueden cambiar sin tocar el código: el código pregunta "¿este plan permite X?"
 * en lugar de preguntar "¿este plan es IMPULSO?".
 */
@Entity
@Table(name = "planes")
@Getter
@Setter
@NoArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private CodigoPlan codigo;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    /** Precio mensual en dólares (el pago es simulado). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    /** Mayor número = aparece antes en el catálogo (Impulso 3, Visible 2, Gratis 1). */
    @Column(nullable = false)
    private int prioridad;

    /** Cantidad de boosts de 48h permitidos por mes calendario. */
    @Column(nullable = false)
    private int boostsPorMes;

    /** Texto del badge ("En crecimiento", "Destacado"); null si no tiene. */
    @Column(length = 40)
    private String badge;

    // --- Beneficios (banderas) ---
    private boolean verVisitasSemana;
    private boolean estadisticasAvanzadas;   // por ciudad e intereses
    private boolean notificarAlActualizar;
    private boolean apareceEnRecomendados;
    private boolean permiteVideoPitch;
}
