package dominio;

/** Jugador controlado con el teclado: la presentación llama a sus métodos de movimiento. */
public class JugadorHumano extends Jugador {

    public JugadorHumano(Lado lado, Personaje personaje, Cuenta cuenta) {
        super(lado, personaje, cuenta);
    }
}
