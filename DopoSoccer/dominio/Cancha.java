package dominio;

import java.io.Serializable;

/**
 * Campo de juego de ANCHO x ALTO unidades con un arco en cada lado. La resolución solo dice
 * de qué tamaño se dibuja en pantalla.
 */
public class Cancha implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final double ANCHO = 800;
    public static final double ALTO = 450;
    /** Altura (y) del césped donde se apoyan jugadores y balón. */
    public static final double SUELO = 400;
    public static final double ANCHO_ARCO = 50;
    public static final double ALTO_ARCO = 150;

    private final Resolucion resolucion;
    private final Arco arcoIzquierdo;
    private final Arco arcoDerecho;

    public Cancha(Resolucion resolucion) {
        this.resolucion = resolucion;
        arcoIzquierdo = new Arco(Lado.IZQUIERDO, 0, SUELO - ALTO_ARCO, ANCHO_ARCO, ALTO_ARCO);
        arcoDerecho = new Arco(Lado.DERECHO, ANCHO - ANCHO_ARCO, SUELO - ALTO_ARCO, ANCHO_ARCO, ALTO_ARCO);
    }

    public double getAncho() {
        return ANCHO;
    }

    public double getAlto() {
        return ALTO;
    }

    /** Mantiene el elemento dentro de la cancha: rebota en el suelo, el techo, los lados y los travesaños. */
    public void rebotarEnBordes(ElementoMovil e) {
        arcoIzquierdo.rebotar(e);
        arcoDerecho.rebotar(e);
        double rebote = e.getRebote();
        if (e.y + e.alto > SUELO) {
            e.y = SUELO - e.alto;
            if (e.velocidadY > 0) e.velocidadY = -e.velocidadY * rebote;
            if (Math.abs(e.velocidadY) < 40) {
                e.velocidadY = 0;
                e.velocidadX *= 0.985;
            }
        }
        if (e.y < 0) {
            e.y = 0;
            if (e.velocidadY < 0) e.velocidadY = -e.velocidadY * rebote;
        }
        if (e.x < 0) {
            e.x = 0;
            if (e.velocidadX < 0) e.velocidadX = -e.velocidadX * rebote;
            e.chocarConBorde();
        }
        if (e.x + e.ancho > ANCHO) {
            e.x = ANCHO - e.ancho;
            if (e.velocidadX > 0) e.velocidadX = -e.velocidadX * rebote;
            e.chocarConBorde();
        }
    }

    /** @return el arco donde entró el balón, o null si no hubo gol */
    public Arco arcoConGol(Balon balon) {
        if (arcoIzquierdo.esGol(balon)) return arcoIzquierdo;
        if (arcoDerecho.esGol(balon)) return arcoDerecho;
        return null;
    }

    public Resolucion getResolucion() {
        return resolucion;
    }

    public Arco getArcoIzquierdo() {
        return arcoIzquierdo;
    }

    public Arco getArcoDerecho() {
        return arcoDerecho;
    }
}
