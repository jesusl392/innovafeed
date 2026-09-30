package co.innovafeed.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Impulso temporal (48h) que pone un emprendimiento de primero en el catálogo.
 */
@Entity
@Table(name = "boosts")
@Getter
@Setter
@NoArgsConstructor
public class Boost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "emprendimiento_id")
    private Emprendimiento emprendimiento;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    public boolean isActivo() {
        LocalDateTime ahora = LocalDateTime.now();
        return !ahora.isBefore(fechaInicio) && ahora.isBefore(fechaFin);
    }
}
