package dominio;

/** Vida en cero: durante 3 s no puede moverse, saltar ni patear; al terminar recupera toda su vida. */
public class Aturdimiento extends Efecto {

    public static final double DURACION = 3;

    public Aturdimiento() {
        super(DURACION);
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

    @Override
    public void finalizar(Jugador jugador) {
        jugador.restaurarVida();
    }
}
