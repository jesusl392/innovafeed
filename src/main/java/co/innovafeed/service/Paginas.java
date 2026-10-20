package co.innovafeed.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Utilidades de paginación.
 */
public final class Paginas {

    /** Tarjetas por página en el catálogo. */
    public static final int TAMANO_CATALOGO = 12;
    /** Filas por página en tablas y bandejas. */
    public static final int TAMANO_TABLAS = 20;

    private Paginas() {
    }

    /** Número de página seguro: nunca negativo (evita errores si alguien escribe ?page=-5). */
    public static PageRequest pedir(int pagina, int tamano) {
        return PageRequest.of(Math.max(0, pagina), tamano);
    }

    /**
     * Corta una lista YA ORDENADA en páginas. Se usa en el catálogo, que se ordena en Java
     * (boost > plan > fecha). Con ~1.000 emprendimientos esto toma milisegundos.
     * Si piden una página que no existe, devuelve la última.
     */
    public static <T> Page<T> de(List<T> listaOrdenada, int pagina, int tamano) {
        int total = listaOrdenada.size();
        int ultima = Math.max(0, (total - 1) / tamano);
        int numero = Math.min(Math.max(0, pagina), ultima);
        int desde = numero * tamano;
        int hasta = Math.min(desde + tamano, total);
        return new PageImpl<>(listaOrdenada.subList(desde, hasta), PageRequest.of(numero, tamano), total);
    }
}
