package dominio;

/** Gana el primero que llegue a un número de goles, sin límite de tiempo. */
public class PartidoPorGoles extends Partido {

    private final int golesParaGanar;

    public PartidoPorGoles(Resolucion resolucion, Jugador jugador1, Jugador jugador2, int golesParaGanar) {
        super(resolucion, jugador1, jugador2);
        this.golesParaGanar = golesParaGanar;
    }

    @Override
    protected boolean haTerminado() {
        return getJugador(1).getGoles() >= golesParaGanar || getJugador(2).getGoles() >= golesParaGanar;
    }

    public int getGolesParaGanar() {
        return golesParaGanar;
    }

    @Override
    public String getDescripcionLimite() {
        return "Gana con " + golesParaGanar + (golesParaGanar == 1 ? " gol" : " goles");
    }
}
