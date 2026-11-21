package dominio;

/** Arco de un lado de la cancha. Su travesaño es la parte de arriba, donde rebotan el balón y los jugadores. */
public class Arco {

    public static final double GROSOR_TRAVESANO = 6;

    private final Lado lado;
    private final double x;
    private final double y;
    private final double ancho;
    private final double alto;

    public Arco(Lado lado, double x, double y, double ancho, double alto) {
        this.lado = lado;
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
    }

    /** Es gol cuando el balón pasa completo la línea del arco por debajo del travesaño. */
    public boolean esGol(Balon balon) {
        boolean debajoDelTravesano = balon.getY() >= y;
        if (lado == Lado.IZQUIERDO) return debajoDelTravesano && balon.getX() + balon.getAncho() <= x + ancho;
        return debajoDelTravesano && balon.getX() >= x;
    }

    /** Si el elemento toca el travesaño, lo deja encima o debajo de él según de dónde venga. */
    public void rebotar(ElementoMovil elemento) {
        double arriba = y - GROSOR_TRAVESANO;
        boolean tocaEnX = elemento.x < x + ancho && x < elemento.x + elemento.ancho;
        boolean tocaEnY = elemento.y < y && arriba < elemento.y + elemento.alto;
        if (!tocaEnX || !tocaEnY) return;
        if (elemento.centroY() < arriba + GROSOR_TRAVESANO / 2) {
            elemento.y = arriba - elemento.alto;
            if (elemento.velocidadY > 0) elemento.velocidadY = -elemento.velocidadY * elemento.getRebote();
        } else {
            elemento.y = y;
            if (elemento.velocidadY < 0) elemento.velocidadY = -elemento.velocidadY * elemento.getRebote();
        }
    }

    public Lado getLado() {
        return lado;
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
}
