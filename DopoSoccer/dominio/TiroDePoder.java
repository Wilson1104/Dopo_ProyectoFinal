package dominio;

import java.io.Serializable;

/**
 * Tiro de poder de un personaje. Cada tiro define cómo sale el balón ({@link #lanzar}) y qué efecto
 * le causa al rival si lo golpea ({@link #crearEfecto}).
 */
public abstract class TiroDePoder implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final int danioBase;

    protected TiroDePoder(String nombre, int danioBase) {
        this.nombre = nombre;
        this.danioBase = danioBase;
    }

    public abstract void lanzar(Partido partido, Jugador lanzador);

    /**
     * DañoPoder = DañoTiro x multiplicadorKick. El efecto se aplica antes del daño: si la vida llega a cero,
     * el aturdimiento reemplaza al efecto y al terminar restaura la vida.
     */
    public void golpear(Jugador rival, Jugador lanzador) {
        rival.aplicarEfecto(crearEfecto());
        rival.recibirDanio(danioBase * lanzador.multiplicadorPatada());
    }

    protected abstract Efecto crearEfecto();

    /** Por defecto el tiro vuela recto, sin gravedad, hasta golpear algo. */
    public boolean afectadoPorGravedad() {
        return false;
    }

    /** Pone el balón real frente al lanzador, mirando al arco rival, con este tiro cargado. */
    protected Balon prepararBalon(Partido partido, Jugador lanzador) {
        Balon balon = partido.getBalonReal();
        double bx = haciaRival(lanzador) > 0 ? lanzador.getX() + lanzador.getAncho() + 2
                                             : lanzador.getX() - balon.getAncho() - 2;
        balon.ubicar(bx, lanzador.centroY() - balon.getAlto() / 2);
        balon.cargarTiro(this, lanzador);
        return balon;
    }

    protected static int haciaRival(Jugador lanzador) {
        return -lanzador.haciaSuArco();
    }

    public String getNombre() {
        return nombre;
    }

    public int getDanioBase() {
        return danioBase;
    }
}
