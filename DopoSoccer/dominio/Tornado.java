package dominio;

/** Nigeria: remolino a ras de suelo que rebota rápidamente y deja al rival inconsciente. */
public class Tornado extends TiroDePoder {

    public Tornado() {
        super("Tornado", 40);
    }

    @Override
    public void lanzar(Partido partido, Jugador lanzador) {
        Balon balon = prepararBalon(partido, lanzador);
        balon.ubicar(balon.getX(), Cancha.SUELO - balon.getAlto());
        balon.impulsar(haciaRival(lanzador) * 800, -260);
    }

    /** El remolino sí cae y rebota en el suelo. */
    @Override
    public boolean afectadoPorGravedad() {
        return true;
    }

    @Override
    protected Efecto crearEfecto() {
        return new Inconsciente();
    }
}
