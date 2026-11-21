package dominio;

/** Camerún: tiro recto cubierto de electricidad que electrocuta al rival. */
public class Rayo extends TiroDePoder {

    public Rayo() {
        super("Rayo", 60);
    }

    @Override
    public void lanzar(Partido partido, Jugador lanzador) {
        prepararBalon(partido, lanzador).impulsar(haciaRival(lanzador) * 950, 0);
    }

    @Override
    protected Efecto crearEfecto() {
        return new Electrocutado();
    }
}
