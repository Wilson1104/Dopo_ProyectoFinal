package dominio;

/** Dura un número de minutos; si al final van empatados, se juega muerte súbita (gana el siguiente gol). */
public class PartidoPorTiempo extends Partido {

    private final int minutos;
    private boolean muerteSubita;

    public PartidoPorTiempo(Resolucion resolucion, Jugador jugador1, Jugador jugador2, int minutos) {
        super(resolucion, jugador1, jugador2);
        this.minutos = minutos;
    }

    @Override
    protected boolean haTerminado() {
        if (getTiempoTranscurrido() < minutos * 60.0) return false;
        boolean empatados = getJugador(1).getGoles() == getJugador(2).getGoles();
        if (empatados) muerteSubita = true;
        return !empatados;
    }

    public double getTiempoRestante() {
        return Math.max(0, minutos * 60.0 - getTiempoTranscurrido());
    }

    public boolean isMuerteSubita() {
        return muerteSubita;
    }

    @Override
    public String getDescripcionLimite() {
        if (muerteSubita) return "MUERTE SÚBITA";
        int segundos = (int) Math.ceil(getTiempoRestante());
        return String.format("%d:%02d", segundos / 60, segundos % 60);
    }
}
