package co.innovafeed.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Persona registrada en la plataforma (admin, emprendedor o usuario).
 * La contraseña se guarda SIEMPRE cifrada con BCrypt (Ley 1581 de 2012).
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Hash BCrypt, nunca la contraseña en texto plano. */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 80)
    private String ciudad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    /** El admin puede desactivar cuentas; un usuario inactivo no puede iniciar sesión. */
    @Column(nullable = false)
    private boolean activo = true;

    /** Autorización de tratamiento de datos personales (Ley 1581 de 2012). */
    @Column(nullable = false)
    private boolean aceptaTratamientoDatos;

    private LocalDateTime fechaAceptacionDatos;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /** Categorías que le interesan al usuario (tabla intermedia intereses_usuario). */
    @ManyToMany
    @JoinTable(
            name = "intereses_usuario",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> intereses = new HashSet<>();

    @PrePersist
    void alCrear() {
        fechaRegistro = LocalDateTime.now();
    }
}
