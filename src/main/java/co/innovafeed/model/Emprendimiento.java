package co.innovafeed.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Emprendimiento publicado por un emprendedor. Es el centro del modelo.
 */
@Entity
@Table(name = "emprendimientos")
@Getter
@Setter
@NoArgsConstructor
public class Emprendimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtapaEmprendimiento etapa;

    @Column(nullable = false, length = 80)
    private String ciudad;

    /** Nombre del fundador tal como se muestra en el perfil público. */
    @Column(nullable = false, length = 100)
    private String fundador;

    /** Etiquetas separadas por comas, p. ej. "reciclaje, empaques, b2b". */
    @Column(length = 500)
    private String tags;

    @Column(length = 255)
    private String url;

    /** Link a video pitch o externo. Solo se muestra si el plan lo permite. */
    @Column(length = 255)
    private String videoPitchUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEmprendimiento estado = EstadoEmprendimiento.PENDIENTE;

    /** Cuenta del emprendedor dueño del emprendimiento. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "propietario_id")
    private Usuario propietario;

    /** Plan vigente. El historial de pagos simulados está en Suscripcion. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    void alCrear() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    void alActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }

    /** Devuelve los tags como lista, útil para pintarlos en la vista. */
    public List<String> getListaTags() {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .toList();
    }
}
