# innovaFeed 🌱

> **Hacemos crecer lo que nace.**

innovaFeed es una plataforma digital colombiana donde los emprendimientos pequeños se publican y la
plataforma los hace visibles, **recomendándolos a las personas según sus intereses** mediante
notificaciones personalizadas. La plataforma lleva el emprendimiento al usuario, no al revés.

Este repositorio contiene el **Producto Mínimo Viable (PMV)**, desarrollado como proyecto académico de
Ingeniería de Sistemas.

---

## Contenido

1. [Tecnologías](#1-tecnologías)
2. [Cómo ejecutar el proyecto](#2-cómo-ejecutar-el-proyecto)
3. [Credenciales de prueba](#3-credenciales-de-prueba)
4. [Funcionalidades por rol](#4-funcionalidades-por-rol)
5. [Reglas de negocio](#5-reglas-de-negocio)
6. [Arquitectura y estructura](#6-arquitectura-y-estructura)
7. [Modelo de datos](#7-modelo-de-datos)
8. [Pruebas](#8-pruebas)
9. [Producción con PostgreSQL](#9-producción-con-postgresql)
10. [Protección de datos (Ley 1581 de 2012)](#10-protección-de-datos-ley-1581-de-2012)
11. [Guion de demostración](#11-guion-de-demostración)
12. [Decisiones técnicas](#12-decisiones-técnicas)

---

## 1. Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.5 (Web, Data JPA, Security, Validation) |
| Vistas | Thymeleaf + CSS propio (sin frameworks de JavaScript) |
| Base de datos | H2 en memoria (desarrollo) · PostgreSQL (producción) |
| Seguridad | Spring Security + BCrypt |
| Pruebas | JUnit 5 + Mockito + Spring Test (MockMvc) |
| Construcción | Maven |

---

## 2. Cómo ejecutar el proyecto

### Requisitos
- **JDK 17 o superior** (probado con JDK 21). Verificar con `java -version`.
- **Maven 3.9+**. Verificar con `mvn -v`.

### Ejecutar en modo desarrollo (H2)

```bash
mvn spring-boot:run
```

Abrir **http://localhost:8080**.

- La base de datos H2 vive **en memoria**: al apagar la aplicación se borra, y al volver a iniciarla
  se cargan de nuevo los datos de prueba.
- Consola de la base de datos: **http://localhost:8080/h2-console**
  (JDBC URL `jdbc:h2:mem:innovafeed`, usuario `sa`, contraseña vacía).

### Generar el ejecutable `.jar`

```bash
mvn clean package
java -jar target/innovafeed-0.0.1-SNAPSHOT.jar
```

---

## 3. Credenciales de prueba

Las crea automáticamente el `DataLoader` al iniciar (solo si la base está vacía).
Se usa el dominio `.test`, reservado para pruebas, para no apuntar a correos reales.

| Rol | Correo | Contraseña | Qué tiene |
|---|---|---|---|
| **Admin** | `admin@innovafeed.test` | `Admin123*` | Gestión de toda la plataforma |
| **Emprendedora** | `laura@innovafeed.test` | `Emprende123*` | EcoPackCo (Impulso), AprendeXR y FoodRoute (Gratis) |
| **Emprendedor** | `carlos@innovafeed.test` | `Emprende123*` | MediIA (Visible, con boost activo) y FinFlow (**Pendiente**) |
| Usuario | `ana@innovafeed.test` | `Usuario123*` | Intereses: Tecnología, Fintech · Bogotá |
| Usuario | `diego@innovafeed.test` | `Usuario123*` | Intereses: Sostenibilidad, Gastronomía · Medellín |
| Usuario | `sofia@innovafeed.test` | `Usuario123*` | Intereses: Salud, Educación, Tecnología · Cali |
| Usuario | `mateo@innovafeed.test` | `Usuario123*` | Intereses: Sostenibilidad, Fintech · Barranquilla |

**Datos de ejemplo incluidos:** 6 categorías, 3 planes, 5 emprendimientos, 2 suscripciones simuladas,
1 boost activo, ~85 visitas repartidas en la última semana, favoritos y notificaciones iniciales.

---

## 4. Funcionalidades por rol

### Visitante (sin sesión)
- Inicio con buscador, sección **Recomendados** y emprendimientos destacados.
- **Catálogo público** con búsqueda por texto (nombre, descripción, tags, ciudad) y filtro por categoría.
- Perfil de cada emprendimiento, página de **planes** y política de **tratamiento de datos**.
- Registro como *usuario* o como *emprendedor*.

### Usuario
- **Intereses:** elegir una o varias categorías.
- **Notificaciones:** bandeja con leídas/no leídas, contador en el menú y "marcar todas como leídas".
- **Favoritos:** guardar y quitar emprendimientos.
- **Mi espacio:** resumen y "Recomendados para ti" según sus intereses.
- **Perfil:** editar nombre y ciudad, y ver qué datos personales guarda la plataforma.

### Emprendedor
- **Publicar** emprendimientos (entran como *Pendiente*), editarlos y eliminarlos (solo los suyos).
- **Plan y boost:** suscribirse a Visible/Impulso (**pago simulado**), volver a Gratis, activar boosts
  y ver el historial de suscripciones.
- **Estadísticas** según su plan: visitas totales, guardados, semana, ciudad e intereses.

### Administrador
- **Dashboard** con métricas: emprendimientos, usuarios activos, notificaciones sin leer, vistas totales
  y top 5 más visitados.
- **Emprendimientos:** ver todos (filtro por estado), activar/suspender, editar y eliminar.
- **Usuarios:** listar por rol y activar/desactivar cuentas.
- **Categorías:** CRUD completo (no se puede borrar una categoría en uso, solo desactivarla).
- **Planes:** configurar precio, prioridad, boosts por mes, badge y beneficios.

---

## 5. Reglas de negocio

### Planes de visibilidad (configurables por el admin)

| Beneficio | Gratis ($0) | Visible ($9/mes) | Impulso ($19/mes) |
|---|:-:|:-:|:-:|
| Perfil en el catálogo | ✓ | ✓ | ✓ |
| Estadísticas mínimas (visitas totales, guardados) | ✓ | ✓ | ✓ |
| Aparece antes en el catálogo (prioridad) | 1 | 2 | 3 |
| Badge | — | "En crecimiento" | "Destacado" |
| Notificar a interesados al actualizar el perfil | — | ✓ | ✓ |
| Visitas de la semana | — | ✓ | ✓ |
| Boosts de 48 h por mes | 0 | 1 | 3 |
| Sección "Recomendados" | — | — | ✓ |
| Estadísticas por ciudad e intereses | — | — | ✓ |
| Video pitch o link externo | — | — | ✓ |

- El pago es **simulado**: el botón "Suscribirme" cambia el plan y registra fechas y monto.
- Cada suscripción dura **30 días** (`innovafeed.suscripcion.dias`). Una tarea programada revisa cada hora
  y devuelve a *Gratis* los planes vencidos.

### Orden del catálogo
**boost activo → plan (Impulso > Visible > Gratis) → más recientes**
(implementado en `OrdenCatalogo.java`).

### Notificaciones automáticas
1. Cuando un emprendimiento **pasa a Activo**, se notifica a todos los usuarios interesados en su categoría.
2. Cuando un emprendimiento activo **actualiza su perfil**, se notifica solo si su plan lo incluye
   (Visible o Impulso).

### Métricas
- Se registra **una visita por sesión de navegador** (recargar no infla los números).
- No cuentan las visitas del propio dueño ni del admin.

---

## 6. Arquitectura y estructura

Arquitectura por capas: **Controller → Service → Repository → Base de datos**.

```
src/main/java/co/innovafeed
├── InnovaFeedApplication.java   Punto de entrada
├── config/        Seguridad, datos de prueba y tareas programadas
│                  (SecurityConfig, DetalleUsuarioService, RedireccionPorRolHandler, DataLoader, TareasProgramadas)
├── controller/    Reciben las peticiones HTTP y eligen la vista
│                  (Home, Auth, Catalogo, Usuario, Emprendedor, Perfil, Planes, Admin*, ManejadorErrores, DatosGlobales)
├── dto/           Objetos de formularios y de estadísticas (RegistroDTO, EmprendimientoDTO, PlanDTO, Conteo...)
├── model/         Entidades JPA (tablas) y enums
├── repository/    Acceso a datos con Spring Data JPA
└── service/       Lógica de negocio (Emprendimiento, Notificacion, Suscripcion, Boost, Estadistica, OrdenCatalogo...)
    └── notificacion/   NotificationSender (interfaz) + InAppNotificationSender

src/main/resources
├── application.properties        Configuración de desarrollo (H2)
├── application-prod.properties   Configuración de producción (PostgreSQL)
├── messages.properties           Mensajes de error de conversión en español
├── static/css/estilos.css        Estilos (paleta verde, responsive, accesible)
└── templates/                    Vistas Thymeleaf por rol: admin/, emprendedor/, usuario/, catalogo/, auth/, error/

src/test/java/co/innovafeed       Pruebas unitarias y de integración
docs/diagrama-er.md               Diagrama entidad-relación (Mermaid)
```

---

## 7. Modelo de datos

10 tablas: `usuarios`, `categorias`, `intereses_usuario` (N:M), `emprendimientos`, `planes`,
`suscripciones`, `boosts`, `favoritos`, `visitas` y `notificaciones`.

El diagrama completo está en **[docs/diagrama-er.md](docs/diagrama-er.md)** (GitHub lo muestra como imagen).

---

## 8. Pruebas

```bash
mvn test
```

**46 pruebas** en 8 clases:

| Clase | Tipo | Qué prueba |
|---|---|---|
| `OrdenCatalogoTest` | Unitaria pura | Boost > Impulso > Visible > Gratis > recientes, desempates y prioridad configurable |
| `NotificacionServiceTest` | Unitaria (Mockito) | Notificar al activar, al actualizar según el plan, múltiples canales, bandeja |
| `EmprendimientoServiceTest` | Unitaria (Mockito) | Orden aplicado al catálogo, búsqueda, permisos, estado → notificaciones |
| `BoostServiceTest` | Unitaria (Mockito) | 48 h, límite mensual, un boost a la vez, solo emprendimientos activos |
| `SuscripcionServiceTest` | Unitaria (Mockito) | Pago simulado de 30 días, cambio de plan, volver a Gratis |
| `UsuarioServiceTest` | Unitaria (Mockito) | BCrypt, no registrarse como ADMIN, correo duplicado |
| `SuscripcionVencimientoTest` | Integración (H2) | Un plan vencido vuelve a Gratis |
| `SeguridadTest` | Integración (MockMvc) | Páginas públicas, redirección al login, 403 por rol, login por rol |

---

## 9. Producción con PostgreSQL

El perfil `prod` usa PostgreSQL. Las credenciales **se leen de variables de entorno**, nunca se escriben
en el código:

| Variable | Ejemplo |
|---|---|
| `DB_URL` | `jdbc:postgresql://host:5432/innovafeed` |
| `DB_USER` | `innovafeed` |
| `DB_PASSWORD` | *(secreta)* |
| `PORT` | `8080` (opcional) |

```bash
java -jar target/innovafeed-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

En producción la consola H2 queda desactivada y Hibernate usa `ddl-auto=update` (crea o ajusta las tablas
sin borrar datos).

> **Nota:** el `DataLoader` también corre en producción si la base está vacía, para tener datos de demostración.
> Antes de un uso real se deben cambiar las contraseñas de prueba o desactivarlo.

---

## 10. Protección de datos (Ley 1581 de 2012)

- **Autorización expresa:** casilla obligatoria en el registro, y se guarda la fecha de aceptación.
- **Política de tratamiento de datos** publicada en `/privacidad`.
- **Derecho a conocer y actualizar:** la página *Perfil* muestra los datos guardados y permite editarlos.
- **Contraseñas cifradas con BCrypt**: ni siquiera el admin puede verlas.
- **No se recogen datos sensibles.**
- **Mínimo acceso:** cada usuario solo ve su información. Por ejemplo, una notificación ajena responde 404,
  y los emprendedores solo ven estadísticas **agregadas** (nunca quién visitó su perfil).

---

## 11. Guion de demostración

1. **Visitante:** abrir el catálogo. MediIA aparece primero (boost) y EcoPackCo segundo (Impulso).
   Buscar "reciclaje".
2. **Admin** (`admin@innovafeed.test`): ver el dashboard, ir a *Emprendimientos → Pendiente* y
   **activar FinFlow**. El mensaje dice "Se notificó a 2 usuario(s)".
3. **Usuario** (`mateo@innovafeed.test`): ver el contador de notificaciones, abrir la de FinFlow y
   guardar EcoPackCo en favoritos.
4. **Emprendedora** (`laura@innovafeed.test`):
   - Editar EcoPackCo (Impulso): "Avisamos a N personas".
   - Editar FoodRoute (Gratis): no avisa a nadie.
   - En AprendeXR → *Plan y boost*: suscribirse a Visible y activar un boost. AprendeXR pasa al primer
     lugar del catálogo.
   - Comparar las *Estadísticas* de EcoPackCo (completas) con las de FoodRoute (bloqueadas).
5. **Admin → Planes:** cambiar el precio de Visible y ver `/planes` actualizado sin tocar código.

---

## 12. Decisiones técnicas

| Decisión | Por qué |
|---|---|
| `Plan` es una **tabla** y el código pregunta por beneficios (`plan.isPermiteVideoPitch()`) | Precios y beneficios configurables sin cambiar código |
| `Rol` es un **enum** en `Usuario` | Cada persona tiene un solo rol; una tabla aparte no aporta nada |
| Formularios con **DTO** en lugar de entidades | Evita que el formulario modifique campos como `rol` o `estado` |
| Permisos verificados en controlador **y** en servicio | Defensa en profundidad: aunque se manipule la URL, se responde 403 |
| Orden del catálogo con un `Comparator` en Java | Más fácil de leer, explicar y probar que un `ORDER BY` con subconsultas |
| `NotificationSender` como interfaz + `List<NotificationSender>` | Agregar email = crear una clase nueva, sin tocar el servicio |
| Estadísticas no incluidas en el plan **no se calculan** | No se filtran datos manipulando la página |
| Menú móvil y gráficos **solo con CSS** | Sin frameworks de JS, funciona aunque JavaScript esté apagado |
| Botones #1DB87A con texto oscuro | Contraste 6.6:1 (WCAG AA); el texto blanco sobre ese verde daba 2.6:1 |
| Tarea programada cada hora para vencer planes | Una sola regla en un solo lugar, en vez de revisarlo en cada consulta |

---

*innovaFeed · Proyecto académico · Ingeniería de Sistemas · Colombia*
#   i n n o v a f e e d  
 