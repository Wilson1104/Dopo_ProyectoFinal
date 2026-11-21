package dominio;

import java.util.Random;

/**
 * Estados Unidos: el balón se divide en 8 balones idénticos; 7 son falsos y desaparecen después de
 * {@link Balon#DURACION_FALSO} segundos. Si golpea al rival, lo confunde.
 */
public class Ilusion extends TiroDePoder {

    public static final int BALONES_FALSOS = 7;

    private final Random azar = new Random();

    public Ilusion() {
        super("Ilusión", 30);
    }

    @Override
    public void lanzar(Partido partido, Jugador lanzador) {
        Balon balon = prepararBalon(partido, lanzador);
        balon.impulsar(haciaRival(lanzador) * 700, -300 + azar.nextInt(7) * 100);
        partido.crearBalonesFalsos(BALONES_FALSOS);
    }

    @Override
    protected Efecto crearEfecto() {
        return new Confusion();
    }
}
