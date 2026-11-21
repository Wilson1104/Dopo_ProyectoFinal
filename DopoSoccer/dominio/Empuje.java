package dominio;

/** Aura Azul: el rival es lanzado hacia su propio arco y no controla su movimiento por 1.5 s. */
public class Empuje extends Efecto {

    public Empuje() {
        super(1.5);
    }

    @Override
    public void iniciar(Jugador jugador) {
        jugador.impulsar(jugador.haciaSuArco() * 450, -250);
    }

    @Override
    public boolean permiteMoverse() {
        return false;
    }
}
