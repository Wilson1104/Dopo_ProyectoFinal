package dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * Un partido de dos jugadores. Cada subclase define cuándo termina (por tiempo o por goles).
 * {@link #actualizar(double)} avanza el juego un instante: es el ciclo que llama la presentación.
 */
public abstract class Partido {

    public static final int PUNTOS_GANADOR = 100;
    public static final int PUNTOS_PERDEDOR = 25;
    /** Paso máximo de simulación, para que nada atraviese a nada si un cuadro tarda mucho. */
    private static final double PASO_MAXIMO = 0.05;

    private final Cancha cancha;
    private final Jugador[] jugadores;
    private final List<Balon> balones = new ArrayList<>();
    private double tiempoTranscurrido;
    private boolean pausado;
    private boolean terminado;

    protected Partido(Resolucion resolucion, Jugador jugador1, Jugador jugador2) {
        cancha = new Cancha(resolucion);
        jugadores = new Jugador[] {jugador1, jugador2};
        balones.add(new Balon(true));
        reiniciarPosiciones();
    }

    public void actualizar(double segundos) {
        if (pausado || terminado || segundos <= 0) return;
        segundos = Math.min(segundos, PASO_MAXIMO);
        tiempoTranscurrido += segundos;
        for (Jugador jugador : jugadores) {
            jugador.decidirAccion(this);
            jugador.actualizar(segundos);
        }
        moverElementos(segundos);
        revisarChoques();
        revisarGoles();
        if (haTerminado()) terminar();
    }

    /** Suma el gol, le da al rival un cuarto de barra y todo vuelve a caer desde el centro. */
    public void registrarGol(Jugador anotador) {
        anotador.anotarGol();
        rivalDe(anotador).recibirGol();
        reiniciarPosiciones();
    }

    public void reiniciarPosiciones() {
        balones.removeIf(b -> !b.esReal());
        Balon balon = getBalonReal();
        balon.descargarTiro();
        balon.ubicar(Cancha.ANCHO / 2 - balon.getAncho() / 2, 60);
        jugadores[0].ubicar(150, Cancha.SUELO - Jugador.ALTO - 80);
        jugadores[1].ubicar(Cancha.ANCHO - 150 - Jugador.ANCHO, Cancha.SUELO - Jugador.ALTO - 80);
    }

    public void pausar() {
        pausado = true;
    }

    public void reanudar() {
        pausado = false;
    }

    public void terminar() {
        if (terminado) return;
        terminado = true;
        repartirPuntos();
    }

    /** @return el jugador con más goles, o null si van empatados */
    public Jugador getGanador() {
        if (jugadores[0].getGoles() > jugadores[1].getGoles()) return jugadores[0];
        if (jugadores[1].getGoles() > jugadores[0].getGoles()) return jugadores[1];
        return null;
    }

    /** 100 puntos al ganador y 25 al perdedor; si empatan, 25 para cada uno. */
    public void repartirPuntos() {
        Jugador ganador = getGanador();
        for (Jugador jugador : jugadores) jugador.ganarPuntos(jugador == ganador ? PUNTOS_GANADOR : PUNTOS_PERDEDOR);
    }

    /** @param numero 1 o 2 */
    public Jugador getJugador(int numero) {
        return jugadores[numero - 1];
    }

    public Balon getBalonReal() {
        for (Balon balon : balones) {
            if (balon.esReal()) return balon;
        }
        throw new IllegalStateException("El partido no tiene balón real");
    }

    /** Copias del balón real que salen en abanico (tiro Ilusión). */
    public void crearBalonesFalsos(int cantidad) {
        Balon real = getBalonReal();
        for (int i = 0; i < cantidad; i++) {
            Balon falso = new Balon(false);
            falso.ubicar(real.getX(), real.getY());
            falso.impulsar(real.getVelocidadX(), -300 + 600.0 * i / Math.max(1, cantidad - 1));
            balones.add(falso);
        }
    }

    protected abstract boolean haTerminado();

    /** Texto para el marcador: tiempo restante, goles para ganar o muerte súbita. */
    public abstract String getDescripcionLimite();

    private void moverElementos(double segundos) {
        for (Balon balon : balones) {
            balon.mover(segundos);
            cancha.rebotarEnBordes(balon);
        }
        balones.removeIf(Balon::haExpirado);
        for (Jugador jugador : jugadores) cancha.rebotarEnBordes(jugador);
    }

    private void revisarChoques() {
        separarJugadores();
        for (Jugador jugador : jugadores) {
            Jugador rival = rivalDe(jugador);
            if (jugador.estaPateando() && jugador.alcanzaA(rival)) {
                rival.recibirDanio(jugador.danioPatada());
                jugador.terminarPatada();
            }
            for (Balon balon : new ArrayList<>(balones)) {
                if (balon.puedeGolpear(jugador)) balon.golpear(jugador);
                else if (jugador.colisionaCon(balon)) jugador.tocarBalon(balon, this);
            }
        }
    }

    private void revisarGoles() {
        Arco arco = cancha.arcoConGol(getBalonReal());
        if (arco != null) registrarGol(anotadorEn(arco));
    }

    /** Los jugadores no se atraviesan: si se cruzan, cada uno retrocede la mitad. */
    private void separarJugadores() {
        Jugador a = jugadores[0];
        Jugador b = jugadores[1];
        if (!a.colisionaCon(b)) return;
        Jugador izquierdo = a.centroX() <= b.centroX() ? a : b;
        Jugador derecho = izquierdo == a ? b : a;
        double cruce = izquierdo.x + izquierdo.ancho - derecho.x;
        izquierdo.x -= cruce / 2;
        derecho.x += cruce / 2;
    }

    private Jugador rivalDe(Jugador jugador) {
        return jugador == jugadores[0] ? jugadores[1] : jugadores[0];
    }

    /** Anota quien ataca ese arco, es decir, el jugador del otro lado. */
    private Jugador anotadorEn(Arco arco) {
        return jugadores[0].getLado() != arco.getLado() ? jugadores[0] : jugadores[1];
    }

    public List<Balon> getBalones() {
        return new ArrayList<>(balones);
    }

    public Cancha getCancha() {
        return cancha;
    }

    public double getTiempoTranscurrido() {
        return tiempoTranscurrido;
    }

    public boolean isPausado() {
        return pausado;
    }

    public boolean isTerminado() {
        return terminado;
    }
}
