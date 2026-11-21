package dominio;

/** Corea del Sur: tiro recto y horizontal a alta velocidad que empuja al rival hacia su arco. */
public class AuraAzul extends TiroDePoder {

    public AuraAzul() {
        super("Aura Azul", 40);
    }

    @Override
    public void lanzar(Partido partido, Jugador lanzador) {
        prepararBalon(partido, lanzador).impulsar(haciaRival(lanzador) * 1100, 0);
    }

    @Override
    protected Efecto crearEfecto() {
        return new Empuje();
    }
}
