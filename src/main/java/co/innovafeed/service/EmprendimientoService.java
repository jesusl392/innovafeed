package co.innovafeed.service;

import co.innovafeed.dto.EmprendimientoDTO;
import co.innovafeed.model.*;
import co.innovafeed.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reglas de negocio de los emprendimientos:
 * - Todo emprendimiento nuevo entra como PENDIENTE y con plan GRATIS.
 * - El emprendedor solo modifica los suyos; el admin modifica todos.
 * - El catálogo público solo muestra emprendimientos ACTIVOS.
 */
@Service
public class EmprendimientoService {

    private final EmprendimientoRepository emprendimientoRepository;
    private final CategoriaService categoriaService;
    private final PlanService planService;
    private final FavoritoRepository favoritoRepository;
    private final VisitaRepository visitaRepository;
    private final NotificacionRepository notificacionRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final BoostRepository boostRepository;
    private final NotificacionService notificacionService;

    public EmprendimientoService(EmprendimientoRepository emprendimientoRepository,
                                 CategoriaService categoriaService, PlanService planService,
                                 FavoritoRepository favoritoRepository, VisitaRepository visitaRepository,
                                 NotificacionRepository notificacionRepository,
                                 SuscripcionRepository suscripcionRepository, BoostRepository boostRepository,
                                 NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
        this.emprendimientoRepository = emprendimientoRepository;
        this.categoriaService = categoriaService;
        this.planService = planService;
        this.favoritoRepository = favoritoRepository;
        this.visitaRepository = visitaRepository;
        this.notificacionRepository = notificacionRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.boostRepository = boostRepository;
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    /**
     * Catálogo público: solo ACTIVOS, con filtro opcional por categoría y texto,
     * ordenado por boost activo > plan > más reciente (ver {@link OrdenCatalogo}).
     */
    @Transactional(readOnly = true)
    public List<Emprendimiento> buscarCatalogo(Long categoriaId, String texto) {
        String patron = "%" + (texto == null ? "" : texto.trim().toLowerCase()) + "%";
        List<Emprendimiento> resultado = (categoriaId == null)
                ? emprendimientoRepository.buscarPorTexto(EstadoEmprendimiento.ACTIVO, patron)
                : emprendimientoRepository.buscarPorCategoriaYTexto(EstadoEmprendimiento.ACTIVO, categoriaId, patron);
        return ordenar(resultado);
    }

    /**
     * Sección "Recomendados": emprendimientos activos cuyo plan incluye ese beneficio (Impulso),
     * de las categorías que le interesan al usuario. Si no tiene intereses (o no hay sesión),
     * se muestran todos.
     */
    @Transactional(readOnly = true)
    public List<Emprendimiento> recomendados(Usuario usuario, int maximo) {
        List<Emprendimiento> candidatos =
                emprendimientoRepository.findByEstadoAndPlan_ApareceEnRecomendadosTrue(EstadoEmprendimiento.ACTIVO);
        if (usuario != null && !usuario.getIntereses().isEmpty()) {
            Set<Long> idsIntereses = usuario.getIntereses().stream().map(Categoria::getId).collect(Collectors.toSet());
            candidatos = candidatos.stream()
                    .filter(e -> idsIntereses.contains(e.getCategoria().getId()))
                    .toList();
        }
        return ordenar(candidatos).stream().limit(maximo).toList();
    }

    /** Ids de emprendimientos con boost vigente (la vista los marca con "Impulsado"). */
    @Transactional(readOnly = true)
    public Set<Long> idsConBoostActivo() {
        return boostRepository.idsConBoostActivo(LocalDateTime.now());
    }

    private List<Emprendimiento> ordenar(List<Emprendimiento> lista) {
        List<Emprendimiento> copia = new ArrayList<>(lista);
        copia.sort(OrdenCatalogo.comparador(idsConBoostActivo()));
        return copia;
    }

    @Transactional(readOnly = true)
    public Emprendimiento buscarPorId(Long id) {
        return emprendimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Emprendimiento no encontrado"));
    }

    /**
     * Perfil público: si no está ACTIVO, solo lo pueden ver su dueño y el admin.
     * Para los demás responde "no encontrado" (no revelamos que existe).
     */
    @Transactional(readOnly = true)
    public Emprendimiento buscarPerfilPublico(Long id, Usuario visitante) {
        Emprendimiento e = buscarPorId(id);
        if (e.getEstado() != EstadoEmprendimiento.ACTIVO && !puedeGestionar(e, visitante)) {
            throw new RecursoNoEncontradoException("Emprendimiento no encontrado");
        }
        return e;
    }

    @Transactional(readOnly = true)
    public List<Emprendimiento> listarDePropietario(Usuario propietario) {
        return emprendimientoRepository.findByPropietarioOrderByFechaCreacionDesc(propietario);
    }

    @Transactional(readOnly = true)
    public List<Emprendimiento> listarTodos(EstadoEmprendimiento estado) {
        return estado == null
                ? emprendimientoRepository.findAllByOrderByFechaCreacionDesc()
                : emprendimientoRepository.findByEstadoOrderByFechaCreacionDesc(estado);
    }

    // ---------------------------------------------------------------
    // Crear / editar / eliminar
    // ---------------------------------------------------------------

    @Transactional
    public Emprendimiento crear(EmprendimientoDTO dto, Usuario propietario) {
        if (propietario.getRol() != Rol.EMPRENDEDOR) {
            throw new AccessDeniedException("Solo los emprendedores pueden publicar emprendimientos");
        }
        Emprendimiento e = new Emprendimiento();
        e.setPropietario(propietario);
        e.setEstado(EstadoEmprendimiento.PENDIENTE);            // el admin lo activa después
        e.setPlan(planService.buscarPorCodigo(CodigoPlan.GRATIS)); // todos empiezan gratis
        copiarDatos(dto, e);
        return emprendimientoRepository.save(e);
    }

    /**
     * Guarda los cambios del perfil. Si el plan lo permite (Visible/Impulso) y está activo,
     * se notifica a los usuarios interesados en la categoría.
     */
    @Transactional
    public ResultadoNotificado actualizar(Long id, EmprendimientoDTO dto, Usuario usuario) {
        Emprendimiento e = buscarPorId(id);
        verificarPermiso(e, usuario);
        copiarDatos(dto, e);
        int notificados = notificacionService.notificarActualizacion(e);
        return new ResultadoNotificado(e, notificados);
    }

    /**
     * Borra el emprendimiento y todo lo que depende de él (favoritos, visitas, etc.).
     */
    @Transactional
    public void eliminar(Long id, Usuario usuario) {
        Emprendimiento e = buscarPorId(id);
        verificarPermiso(e, usuario);
        favoritoRepository.deleteByEmprendimiento(e);
        visitaRepository.deleteByEmprendimiento(e);
        notificacionRepository.deleteByEmprendimiento(e);
        suscripcionRepository.deleteByEmprendimiento(e);
        boostRepository.deleteByEmprendimiento(e);
        emprendimientoRepository.delete(e);
    }

    /** Solo el admin cambia el estado (Pendiente, En revisión, Activo, Suspendido). */
    /**
     * Si el emprendimiento PASA a ACTIVO (antes no lo estaba), se notifica
     * a todos los usuarios interesados en su categoría.
     */
    @Transactional
    public ResultadoNotificado cambiarEstado(Long id, EstadoEmprendimiento nuevoEstado) {
        Emprendimiento e = buscarPorId(id);
        boolean seActiva = e.getEstado() != EstadoEmprendimiento.ACTIVO
                && nuevoEstado == EstadoEmprendimiento.ACTIVO;
        e.setEstado(nuevoEstado);
        int notificados = seActiva ? notificacionService.notificarNuevoEmprendimiento(e) : 0;
        return new ResultadoNotificado(e, notificados);
    }

    // ---------------------------------------------------------------
    // Permisos
    // ---------------------------------------------------------------

    /** true si el usuario es admin o es el dueño del emprendimiento. */
    public boolean puedeGestionar(Emprendimiento e, Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        return usuario.getRol() == Rol.ADMIN || e.getPropietario().getId().equals(usuario.getId());
    }

    private void verificarPermiso(Emprendimiento e, Usuario usuario) {
        if (!puedeGestionar(e, usuario)) {
            // Spring Security convierte esta excepción en la página 403
            throw new AccessDeniedException("No puedes modificar este emprendimiento");
        }
    }

    /** Pasa los datos del formulario a la entidad. */
    private void copiarDatos(EmprendimientoDTO dto, Emprendimiento e) {
        e.setNombre(dto.getNombre().trim());
        e.setDescripcion(dto.getDescripcion().trim());
        e.setCategoria(categoriaService.buscarPorId(dto.getCategoriaId()));
        e.setEtapa(dto.getEtapa());
        e.setCiudad(dto.getCiudad().trim());
        e.setFundador(dto.getFundador().trim());
        e.setTags(vacioANull(dto.getTags()));
        e.setUrl(vacioANull(dto.getUrl()));
        // El video pitch es un beneficio del plan: si el plan no lo permite, se ignora
        if (e.getPlan() != null && e.getPlan().isPermiteVideoPitch()) {
            e.setVideoPitchUrl(vacioANull(dto.getVideoPitchUrl()));
        }
    }

    private String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
