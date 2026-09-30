package co.innovafeed.model;

/**
 * Roles de la plataforma. Cada usuario tiene exactamente uno.
 */
public enum Rol {
    ADMIN("Administrador"),
    EMPRENDEDOR("Emprendedor"),
    USUARIO("Usuario");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
