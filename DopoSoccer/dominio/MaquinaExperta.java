package dominio;

/**
 * Perfil experto: decide según el estado del partido. Si va ganando o el balón está en su mitad,
 * defiende; si va perdiendo o empatado y el balón está lejos de su arco, ataca.
 */
public class MaquinaExperta extends Maquina {

    public MaquinaExperta(Lado lado, Personaje personaje) {
        super(lado, personaje);
    }

    @Override
    public void decidirAccion(Partido partido) {
        Jugador rival = partido.getJugador(1) == this ? partido.getJugador(2) : partido.getJugador(1);
        boolean vaGanando = getGoles() > rival.getGoles();
        if (vaGanando || balonEnSuMitad(partido.getBalonReal())) defender(partido);
        else atacar(partido);
    }
}
