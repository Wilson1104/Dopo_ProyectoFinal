package dominio;

import java.io.Serializable;

/**
 * Todo lo que se mueve en la cancha (jugadores y balones): posición, velocidad, tamaño y gravedad.
 * Las coordenadas crecen hacia la derecha (x) y hacia abajo (y).
 */
public abstract class ElementoMovil implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Aceleración hacia abajo, en unidades de cancha por segundo al cuadrado. */
    public static final double GRAVEDAD = 900;

    protected double x;
    protected double y;
    protected double velocidadX;
    protected double velocidadY;
    protected double ancho;
    protected double alto;

    protected ElementoMovil(double ancho, double alto) {
        this.ancho = ancho;
        this.alto = alto;
    }

    public void mover(double segundos) {
        velocidadY += GRAVEDAD * segundos;
        x += velocidadX * segundos;
        y += velocidadY * segundos;
    }

    public void impulsar(double velocidadX, double velocidadY) {
        this.velocidadX = velocidadX;
        this.velocidadY = velocidadY;
    }

    /** Pone el elemento en una posición y lo deja quieto. */
    public void ubicar(double x, double y) {
        this.x = x;
        this.y = y;
        velocidadX = 0;
        velocidadY = 0;
    }

    public boolean colisionaCon(ElementoMovil otro) {
        return x < otro.x + otro.ancho && otro.x < x + ancho && y < otro.y + otro.alto && otro.y < y + alto;
    }

    /** Qué fracción de la velocidad conserva al rebotar en el suelo o en un borde (0 = no rebota). */
    public double getRebote() {
        return 0;
    }

    /** Se llama cuando el elemento choca con un borde lateral de la cancha. */
    public void chocarConBorde() {
    }

    public double centroX() {
        return x + ancho / 2;
    }

    public double centroY() {
        return y + alto / 2;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAncho() {
        return ancho;
    }

    public double getAlto() {
        return alto;
    }

    public double getVelocidadX() {
        return velocidadX;
    }

    public double getVelocidadY() {
        return velocidadY;
    }
}
