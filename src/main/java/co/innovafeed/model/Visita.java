package co.innovafeed.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Cada vez que alguien abre el perfil de un emprendimiento se guarda una visita.
 * Usuario y ciudad son opcionales: los visitantes sin sesión también cuentan.
 */
@Entity
@Table(name = "visitas")
@Getter
@Setter
@NoArgsConstructor
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "emprendimiento_id")
    private Emprendimiento emprendimiento;

    /** Null si el visitante no inició sesión. */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** Ciudad del usuario al momento de la visita (null si es anónimo). */
    @Column(length = 80)
    private String ciudad;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
