package co.innovafeed.model;

/**
 * Etapa de madurez en la que se encuentra un emprendimiento.
 */
public enum EtapaEmprendimiento {
    IDEA("Idea"),
    MVP("MVP"),
    EARLY_STAGE("Early stage"),
    CRECIMIENTO("Crecimiento");

    private final String etiqueta;

    EtapaEmprendimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
