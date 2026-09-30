package co.innovafeed.model;

/**
 * Estado de publicación. Solo los ACTIVO aparecen en el catálogo público.
 * Todo emprendimiento nuevo entra como PENDIENTE y el admin lo activa.
 */
public enum EstadoEmprendimiento {
    PENDIENTE("Pendiente"),
    EN_REVISION("En revisión"),
    ACTIVO("Activo"),
    SUSPENDIDO("Suspendido");

    private final String etiqueta;

    EstadoEmprendimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
