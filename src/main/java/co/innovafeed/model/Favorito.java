package co.innovafeed.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Un usuario guardó un emprendimiento. La restricción única impide guardarlo dos veces.
 */
@Entity
@Table(name = "favoritos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "emprendimiento_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "emprendimiento_id")
    private Emprendimiento emprendimiento;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public Favorito(Usuario usuario, Emprendimiento emprendimiento) {
        this.usuario = usuario;
        this.emprendimiento = emprendimiento;
        this.fecha = LocalDateTime.now();
    }
}
