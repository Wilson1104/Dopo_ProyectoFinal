package dominio;

/**
 * Tamaño en pantalla de la cancha. El dominio siempre trabaja con una cancha de
 * {@link Cancha#ANCHO} x {@link Cancha#ALTO} unidades; la presentación la escala a la resolución.
 */
public enum Resolucion {
    BAJA(800, 450), MEDIA(1280, 720), ALTA(1600, 900);

    private final int ancho;
    private final int alto;

    Resolucion(int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;
    }

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }
}
