package dominio;

/**
 * Balón del partido. Puede llevar un tiro de poder cargado (vuela sin gravedad hasta golpear algo)
 * y puede ser falso: los balones falsos de la Ilusión desaparecen después de unos segundos.
 */
public class Balon extends ElementoMovil {

    public static final double TAMANIO = 24;
    public static final double DURACION_FALSO = 2.5;

    private final boolean real;
    private TiroDePoder tiroCargado;
    private Jugador lanzador;
    private double duracion;

    public Balon(boolean real) {
        super(TAMANIO, TAMANIO);
        this.real = real;
        duracion = real ? Double.POSITIVE_INFINITY : DURACION_FALSO;
    }

    @Override
    public void mover(double segundos) {
        if (!real) duracion -= segundos;
        boolean sinGravedad = !real || (tiroCargado != null && !tiroCargado.afectadoPorGravedad());
        if (sinGravedad) {
            x += velocidadX * segundos;
            y += velocidadY * segundos;
        } else {
            super.mover(segundos);
        }
    }

    @Override
    public double getRebote() {
        return 0.75;
    }

    /** Un tiro de poder que llega a un borde se pierde. */
    @Override
    public void chocarConBorde() {
        descargarTiro();
    }

    public void cargarTiro(TiroDePoder tiro, Jugador lanzador) {
        tiroCargado = tiro;
        this.lanzador = lanzador;
    }

    public void descargarTiro() {
        tiroCargado = null;
        lanzador = null;
    }

    /** El tiro golpea a cualquier jugador que no sea quien lo lanzó. */
    public boolean puedeGolpear(Jugador jugador) {
        return tiroCargado != null && jugador != lanzador && colisionaCon(jugador);
    }

    public void golpear(Jugador rival) {
        TiroDePoder tiro = tiroCargado;
        Jugador quienLanzo = lanzador;
        descargarTiro();
        tiro.golpear(rival, quienLanzo);
        impulsar(-velocidadX * 0.3, -200);
    }

    public boolean haExpirado() {
        return !real && duracion <= 0;
    }

    public boolean esReal() {
        return real;
    }

    public boolean tieneTiroCargado() {
        return tiroCargado != null;
    }

    public TiroDePoder getTiroCargado() {
        return tiroCargado;
    }
}
