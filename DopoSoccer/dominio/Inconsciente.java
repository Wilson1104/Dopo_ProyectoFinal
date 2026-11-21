package dominio;

/** Tornado: el rival es lanzado al aire y durante 3 s no puede moverse, saltar ni patear. */
public class Inconsciente extends Efecto {

    public Inconsciente() {
        super(3);
    }

    @Override
    public void iniciar(Jugador jugador) {
        jugador.impulsar(0, -650);
    }

    @Override
    public boolean permiteMoverse() {
        return false;
    }

    @Override
    public boolean permiteSaltar() {
        return false;
    }

    @Override
    public boolean permitePatear() {
        return false;
    }
}
