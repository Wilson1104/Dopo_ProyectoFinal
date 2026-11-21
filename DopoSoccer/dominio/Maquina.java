package dominio;

import java.util.Random;

/**
 * Jugador controlado por la computadora. Cada perfil decide distinto qué hacer en cada momento;
 * comparten: atacar y defender.
 */
public abstract class Maquina extends Jugador {

    private static final double MARGEN = 8;
    private static final double CERCA = 70;
    /** Probabilidad de patear en cada cuadro cuando tiene el balón cerca: así no patea sin parar. */
    private static final double PROBABILIDAD_PATADA = 0.06;
    /** Cuántos segundos hacia adelante calcula dónde va a estar el balón. */
    private static final double ANTICIPACION = 0.22;
    /** Distancia horizontal a la que ya está bajo el balón y puede saltar a cabecearlo. */
    private static final double BAJO_EL_BALON = 38;
    /** Distancia a la que salta para pasar por encima de un balón que está en el suelo. */
    private static final double SALTO_POR_ENCIMA = 80;

    protected final Random azar = new Random();

    protected Maquina(Lado lado, Personaje personaje) {
        super(lado, personaje, new Cuenta("Máquina"));
    }

    @Override
    public abstract void decidirAccion(Partido partido);

    /**
     * Si el balón está entre ella y el arco rival, camina hacia él empujándolo y patea al tenerlo cerca;
     * si no, rodea el balón para quedar detrás. Persigue el punto donde el balón va a estar, y solo salta
     * cuando el balón está al alcance de su salto (no antes, para no pasar por debajo sin tocarlo).
     */
    protected void atacar(Partido partido) {
        Balon balon = partido.getBalonReal();
        double objetivo = puntoDeEncuentro(balon);
        boolean detrasDelBalon = haciaSuArco() < 0 ? centroX() < objetivo : centroX() > objetivo;
        if (detrasDelBalon) {
            irHaciaSinParar(objetivo);
            if (cercaDe(balon)) {
                if (poderDisponible()) intentarPoder();
                if (azar.nextDouble() < PROBABILIDAD_PATADA) patear();
            }
        } else {
            // el balón está entre ella y su propio arco: pasa por encima de él en vez de empujarlo hacia su arco
            irHacia(objetivo + haciaSuArco() * (ancho + balon.getAncho()));
            if (Math.abs(balon.centroX() - centroX()) < SALTO_POR_ENCIMA && balon.centroY() > y + alto / 2) saltar();
        }
        if (debeSaltar(balon)) saltar();
    }

    /** Vuelve a cuidar su arco; si el balón está en su mitad, sale a despejarlo. */
    protected void defender(Partido partido) {
        Balon balon = partido.getBalonReal();
        if (balonEnSuMitad(balon)) {
            atacar(partido);
        } else {
            double casa = haciaSuArco() < 0 ? Cancha.ANCHO_ARCO + 70 : Cancha.ANCHO - Cancha.ANCHO_ARCO - 70;
            irHacia(casa);
        }
    }

    protected boolean balonEnSuMitad(Balon balon) {
        boolean izquierda = balon.centroX() < Cancha.ANCHO / 2;
        return (haciaSuArco() < 0) == izquierda;
    }

    protected boolean cercaDe(Balon balon) {
        return Math.abs(balon.centroX() - centroX()) < CERCA && Math.abs(balon.centroY() - centroY()) < CERCA * 1.5;
    }

    protected void irHacia(double objetivoX) {
        if (objetivoX < centroX() - MARGEN) moverIzquierda();
        else if (objetivoX > centroX() + MARGEN) moverDerecha();
        else detener();
    }

    protected void intentarPoder() {
        try {
            activarPoder();
        } catch (ExcepcionDOPOSoccer e) {
            // la barra no estaba llena: sigue jugando sin poder
        }
    }

    /** Camina hacia el objetivo sin detenerse al llegar, para que el cuerpo choque con el balón. */
    private void irHaciaSinParar(double objetivoX) {
        if (objetivoX < centroX()) moverIzquierda();
        else moverDerecha();
    }

    /** Dónde estará el balón en un instante, sin salirse de la cancha. */
    private double puntoDeEncuentro(Balon balon) {
        double x = balon.centroX() + balon.getVelocidadX() * ANTICIPACION;
        return Math.max(ancho / 2, Math.min(Cancha.ANCHO - ancho / 2, x));
    }

    /**
     * Salta cuando está debajo del balón y este está dentro de lo que alcanza su salto y ya no sube
     * rápido; así el cabezazo llega en el momento correcto y no cuando el balón ya pasó.
     */
    private boolean debeSaltar(Balon balon) {
        double distanciaArriba = y - (balon.getY() + balon.getAlto());
        boolean porEncimaDeLaCabeza = balon.centroY() < y;
        boolean debajo = Math.abs(balon.centroX() + balon.getVelocidadX() * 0.1 - centroX()) < BAJO_EL_BALON;
        boolean alAlcance = distanciaArriba < alturaDeSalto() * 0.85;
        return porEncimaDeLaCabeza && debajo && alAlcance && balon.getVelocidadY() > -150;
    }
}
