package co.innovafeed.config;

import co.innovafeed.model.*;
import co.innovafeed.repository.*;
import co.innovafeed.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Carga datos de prueba al iniciar la aplicación, SOLO si la base está vacía.
 * Así en desarrollo (H2) siempre hay datos, y en producción no se duplican.
 *
 * Credenciales de prueba (dominio .test, reservado para pruebas):
 *   admin@innovafeed.test      / Admin123*      (en producción: variable ADMIN_PASSWORD)
 *   laura@innovafeed.test      / Emprende123*   (emprendedora)
 *   carlos@innovafeed.test     / Emprende123*   (emprendedor)
 *   ana@innovafeed.test, diego@..., sofia@..., mateo@...  / Usuario123*
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PlanRepository planRepository;
    private final EmprendimientoRepository emprendimientoRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificacionService notificacionService;
    private final BoostRepository boostRepository;
    private final VisitaRepository visitaRepository;
    private final FavoritoRepository favoritoRepository;
    /** Contraseña del admin: "Admin123*" en desarrollo, variable ADMIN_PASSWORD en producción. */
    private final String adminPassword;

    public DataLoader(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                      PlanRepository planRepository, EmprendimientoRepository emprendimientoRepository,
                      SuscripcionRepository suscripcionRepository, PasswordEncoder passwordEncoder,
                      NotificacionService notificacionService, BoostRepository boostRepository,
                      VisitaRepository visitaRepository, FavoritoRepository favoritoRepository,
                      @Value("${innovafeed.admin.password}") String adminPassword) {
        this.adminPassword = adminPassword;
        this.notificacionService = notificacionService;
        this.visitaRepository = visitaRepository;
        this.favoritoRepository = favoritoRepository;
        this.boostRepository = boostRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.planRepository = planRepository;
        this.emprendimientoRepository = emprendimientoRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("La base de datos ya tiene datos; no se cargan datos de prueba.");
            return;
        }

        Map<String, Categoria> categorias = crearCategorias();
        Map<CodigoPlan, Plan> planes = crearPlanes();

        // --- Usuarios ---
        crearUsuario("Administrador innovaFeed", "admin@innovafeed.test", adminPassword, "Bogotá", Rol.ADMIN, Set.of());
        Usuario laura = crearUsuario("Laura Gómez", "laura@innovafeed.test", "Emprende123*", "Medellín", Rol.EMPRENDEDOR, Set.of());
        Usuario carlos = crearUsuario("Carlos Pérez", "carlos@innovafeed.test", "Emprende123*", "Bogotá", Rol.EMPRENDEDOR, Set.of());

        Usuario ana = crearUsuario("Ana Martínez", "ana@innovafeed.test", "Usuario123*", "Bogotá", Rol.USUARIO,
                Set.of(categorias.get("Tecnología"), categorias.get("Fintech")));
        Usuario diego = crearUsuario("Diego Rojas", "diego@innovafeed.test", "Usuario123*", "Medellín", Rol.USUARIO,
                Set.of(categorias.get("Sostenibilidad"), categorias.get("Gastronomía")));
        Usuario sofia = crearUsuario("Sofía Herrera", "sofia@innovafeed.test", "Usuario123*", "Cali", Rol.USUARIO,
                Set.of(categorias.get("Salud"), categorias.get("Educación"), categorias.get("Tecnología")));
        Usuario mateo = crearUsuario("Mateo Castro", "mateo@innovafeed.test", "Usuario123*", "Barranquilla", Rol.USUARIO,
                Set.of(categorias.get("Sostenibilidad"), categorias.get("Fintech")));

        // --- Emprendimientos ---
        Emprendimiento ecoPack = crearEmprendimiento("EcoPackCo",
                "Empaques biodegradables hechos con residuos agrícolas para restaurantes y tiendas.",
                categorias.get("Sostenibilidad"), EtapaEmprendimiento.EARLY_STAGE, "Medellín", "Laura Gómez",
                "empaques, reciclaje, b2b", "https://ecopackco.example", EstadoEmprendimiento.ACTIVO,
                laura, planes.get(CodigoPlan.IMPULSO));
        ecoPack.setVideoPitchUrl("https://www.youtube.com/watch?v=ejemplo");

        Emprendimiento mediIA = crearEmprendimiento("MediIA",
                "Asistente con inteligencia artificial que ayuda a priorizar citas médicas en centros de salud.",
                categorias.get("Salud"), EtapaEmprendimiento.MVP, "Bogotá", "Carlos Pérez",
                "ia, salud, citas", "https://mediia.example", EstadoEmprendimiento.ACTIVO,
                carlos, planes.get(CodigoPlan.VISIBLE));

        Emprendimiento aprendeXR = crearEmprendimiento("AprendeXR",
                "Laboratorios de ciencias en realidad aumentada para colegios públicos.",
                categorias.get("Educación"), EtapaEmprendimiento.IDEA, "Medellín", "Laura Gómez",
                "realidad aumentada, colegios, stem", "https://aprendexr.example", EstadoEmprendimiento.ACTIVO,
                laura, planes.get(CodigoPlan.GRATIS));

        // Queda PENDIENTE para demostrar la activación por parte del admin
        crearEmprendimiento("FinFlow",
                "App para que tenderos lleven sus cuentas y accedan a microcréditos.",
                categorias.get("Fintech"), EtapaEmprendimiento.MVP, "Bogotá", "Carlos Pérez",
                "finanzas, tenderos, microcrédito", "https://finflow.example", EstadoEmprendimiento.PENDIENTE,
                carlos, planes.get(CodigoPlan.GRATIS));

        Emprendimiento foodRoute = crearEmprendimiento("FoodRoute",
                "Rutas gastronómicas guiadas por cocineras tradicionales de cada barrio.",
                categorias.get("Gastronomía"), EtapaEmprendimiento.CRECIMIENTO, "Medellín", "Laura Gómez",
                "turismo, cocina tradicional, barrios", "https://foodroute.example", EstadoEmprendimiento.ACTIVO,
                laura, planes.get(CodigoPlan.GRATIS));

        // Pagos simulados de los emprendimientos con plan pago
        crearSuscripcion(ecoPack);
        crearSuscripcion(mediIA);

        // MediIA (plan Visible) usa su boost del mes: aparecerá incluso antes que EcoPackCo (Impulso)
        Boost boost = new Boost();
        boost.setEmprendimiento(mediIA);
        boost.setFechaInicio(LocalDateTime.now());
        boost.setFechaFin(LocalDateTime.now().plusHours(48));
        boostRepository.save(boost);

        // Notificaciones iniciales: se generan con la MISMA regla que usa la app al activar
        emprendimientoRepository.findByEstadoOrderByFechaCreacionDesc(EstadoEmprendimiento.ACTIVO)
                .forEach(notificacionService::notificarNuevoEmprendimiento);

        // Métricas de ejemplo: visitas de los últimos 7 días y algunos favoritos
        List<Usuario> visitantes = Arrays.asList(ana, diego, sofia, mateo, null);   // null = sin sesión
        crearVisitas(ecoPack, 40, visitantes);
        crearVisitas(mediIA, 25, visitantes);
        crearVisitas(aprendeXR, 12, visitantes);
        crearVisitas(foodRoute, 8, visitantes);
        favoritoRepository.save(new Favorito(diego, ecoPack));
        favoritoRepository.save(new Favorito(mateo, ecoPack));
        favoritoRepository.save(new Favorito(sofia, mediIA));

        log.info("Datos de prueba cargados: {} usuarios, {} categorías, {} planes, {} emprendimientos.",
                usuarioRepository.count(), categoriaRepository.count(),
                planRepository.count(), emprendimientoRepository.count());
    }

    private Map<String, Categoria> crearCategorias() {
        Map<String, Categoria> mapa = new HashMap<>();
        String[][] datos = {
                {"Tecnología", "Software, hardware e innovación digital"},
                {"Salud", "Bienestar, medicina y servicios de salud"},
                {"Sostenibilidad", "Medio ambiente, reciclaje y economía circular"},
                {"Educación", "Aprendizaje, formación y edtech"},
                {"Fintech", "Servicios financieros y pagos digitales"},
                {"Gastronomía", "Comida, bebidas y experiencias culinarias"}
        };
        for (String[] d : datos) {
            mapa.put(d[0], categoriaRepository.save(new Categoria(d[0], d[1])));
        }
        return mapa;
    }

    private Map<CodigoPlan, Plan> crearPlanes() {
        Map<CodigoPlan, Plan> mapa = new HashMap<>();

        Plan gratis = new Plan();
        gratis.setCodigo(CodigoPlan.GRATIS);
        gratis.setNombre("Gratis");
        gratis.setDescripcion("Perfil básico, aparece en el catálogo y estadísticas mínimas.");
        gratis.setPrecioMensual(BigDecimal.ZERO);
        gratis.setPrioridad(1);
        gratis.setBoostsPorMes(0);
        mapa.put(CodigoPlan.GRATIS, planRepository.save(gratis));

        Plan visible = new Plan();
        visible.setCodigo(CodigoPlan.VISIBLE);
        visible.setNombre("Visible");
        visible.setDescripcion("Aparece antes que los gratis, notifica al actualizar y muestra visitas de la semana.");
        visible.setPrecioMensual(new BigDecimal("9.00"));
        visible.setPrioridad(2);
        visible.setBoostsPorMes(1);
        visible.setBadge("En crecimiento");
        visible.setVerVisitasSemana(true);
        visible.setNotificarAlActualizar(true);
        mapa.put(CodigoPlan.VISIBLE, planRepository.save(visible));

        Plan impulso = new Plan();
        impulso.setCodigo(CodigoPlan.IMPULSO);
        impulso.setNombre("Impulso");
        impulso.setDescripcion("Todo lo de Visible + Recomendados, estadísticas avanzadas, 3 boosts y video pitch.");
        impulso.setPrecioMensual(new BigDecimal("19.00"));
        impulso.setPrioridad(3);
        impulso.setBoostsPorMes(3);
        impulso.setBadge("Destacado");
        impulso.setVerVisitasSemana(true);
        impulso.setNotificarAlActualizar(true);
        impulso.setEstadisticasAvanzadas(true);
        impulso.setApareceEnRecomendados(true);
        impulso.setPermiteVideoPitch(true);
        mapa.put(CodigoPlan.IMPULSO, planRepository.save(impulso));

        return mapa;
    }

    private Usuario crearUsuario(String nombre, String email, String password, String ciudad,
                                 Rol rol, Set<Categoria> intereses) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setCiudad(ciudad);
        u.setRol(rol);
        u.setAceptaTratamientoDatos(true);
        u.setFechaAceptacionDatos(LocalDateTime.now());
        u.getIntereses().addAll(intereses);
        return usuarioRepository.save(u);
    }

    private Emprendimiento crearEmprendimiento(String nombre, String descripcion, Categoria categoria,
                                               EtapaEmprendimiento etapa, String ciudad, String fundador,
                                               String tags, String url, EstadoEmprendimiento estado,
                                               Usuario propietario, Plan plan) {
        Emprendimiento e = new Emprendimiento();
        e.setNombre(nombre);
        e.setDescripcion(descripcion);
        e.setCategoria(categoria);
        e.setEtapa(etapa);
        e.setCiudad(ciudad);
        e.setFundador(fundador);
        e.setTags(tags);
        e.setUrl(url);
        e.setEstado(estado);
        e.setPropietario(propietario);
        e.setPlan(plan);
        return emprendimientoRepository.save(e);
    }

    /**
     * Crea visitas repartidas en los últimos 7 días. Usa una semilla fija (42)
     * para que los datos de prueba salgan siempre iguales.
     */
    private void crearVisitas(Emprendimiento e, int cantidad, List<Usuario> visitantes) {
        Random azar = new Random(42 + e.getNombre().length());
        for (int i = 0; i < cantidad; i++) {
            Usuario u = visitantes.get(azar.nextInt(visitantes.size()));
            Visita v = new Visita();
            v.setEmprendimiento(e);
            v.setUsuario(u);
            v.setCiudad(u != null ? u.getCiudad() : null);
            v.setFecha(LocalDateTime.now().minusDays(azar.nextInt(7)).minusHours(azar.nextInt(12)));
            visitaRepository.save(v);
        }
    }

    private void crearSuscripcion(Emprendimiento e) {
        Suscripcion s = new Suscripcion();
        s.setEmprendimiento(e);
        s.setPlan(e.getPlan());
        s.setFechaInicio(LocalDateTime.now());
        s.setFechaFin(LocalDateTime.now().plusDays(30));
        s.setMontoSimulado(e.getPlan().getPrecioMensual());
        suscripcionRepository.save(s);
    }
}
