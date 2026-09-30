# innovaFeed — Diagrama entidad-relación

```mermaid
erDiagram
    USUARIOS ||--o{ EMPRENDIMIENTOS : "es propietario de"
    USUARIOS }o--o{ CATEGORIAS : "intereses_usuario"
    CATEGORIAS ||--o{ EMPRENDIMIENTOS : "clasifica"
    PLANES ||--o{ EMPRENDIMIENTOS : "plan actual"
    PLANES ||--o{ SUSCRIPCIONES : "se contrata en"
    EMPRENDIMIENTOS ||--o{ SUSCRIPCIONES : "historial de pagos simulados"
    EMPRENDIMIENTOS ||--o{ BOOSTS : "recibe"
    EMPRENDIMIENTOS ||--o{ VISITAS : "recibe"
    USUARIOS |o--o{ VISITAS : "hace (opcional)"
    USUARIOS ||--o{ FAVORITOS : "guarda"
    EMPRENDIMIENTOS ||--o{ FAVORITOS : "es guardado"
    USUARIOS ||--o{ NOTIFICACIONES : "recibe"
    EMPRENDIMIENTOS ||--o{ NOTIFICACIONES : "origina"

    USUARIOS {
        bigint id PK
        string nombre
        string email UK
        string password "BCrypt"
        string ciudad
        string rol "ADMIN | EMPRENDEDOR | USUARIO"
        boolean activo
        boolean acepta_tratamiento_datos "Ley 1581"
        datetime fecha_aceptacion_datos
        datetime fecha_registro
    }
    CATEGORIAS {
        bigint id PK
        string nombre UK
        string descripcion
        boolean activa
    }
    PLANES {
        bigint id PK
        string codigo UK "GRATIS | VISIBLE | IMPULSO"
        string nombre
        decimal precio_mensual
        int prioridad
        int boosts_por_mes
        string badge
        boolean ver_visitas_semana
        boolean estadisticas_avanzadas
        boolean notificar_al_actualizar
        boolean aparece_en_recomendados
        boolean permite_video_pitch
    }
    EMPRENDIMIENTOS {
        bigint id PK
        string nombre
        string descripcion
        bigint categoria_id FK
        string etapa "IDEA | MVP | EARLY_STAGE | CRECIMIENTO"
        string ciudad
        string fundador
        string tags
        string url
        string video_pitch_url
        string estado "PENDIENTE | EN_REVISION | ACTIVO | SUSPENDIDO"
        bigint propietario_id FK
        bigint plan_id FK
        datetime fecha_creacion
        datetime fecha_actualizacion
    }
    SUSCRIPCIONES {
        bigint id PK
        bigint emprendimiento_id FK
        bigint plan_id FK
        datetime fecha_inicio
        datetime fecha_fin "inicio + 30 dias"
        decimal monto_simulado
    }
    BOOSTS {
        bigint id PK
        bigint emprendimiento_id FK
        datetime fecha_inicio
        datetime fecha_fin "inicio + 48 h"
    }
    VISITAS {
        bigint id PK
        bigint emprendimiento_id FK
        bigint usuario_id FK "nullable"
        string ciudad "nullable"
        datetime fecha
    }
    FAVORITOS {
        bigint id PK
        bigint usuario_id FK
        bigint emprendimiento_id FK
        datetime fecha
    }
    NOTIFICACIONES {
        bigint id PK
        bigint usuario_id FK
        bigint emprendimiento_id FK
        string tipo "NUEVO_EMPRENDIMIENTO | ACTUALIZACION_PERFIL"
        string mensaje
        boolean leida
        datetime fecha
    }
```

## Decisiones clave
- **Rol** es un enum guardado en `usuarios.rol`: cada persona tiene un solo rol.
- **Plan** es una tabla: precio y beneficios se cambian sin tocar código.
- **intereses_usuario** es una tabla intermedia N:M (sin entidad propia).
- **emprendimientos.plan_id** guarda el plan vigente; **suscripciones** guarda el historial.
- Orden del catálogo: boost activo > `planes.prioridad` > `fecha_creacion` más reciente.
