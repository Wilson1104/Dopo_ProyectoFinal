package dominio;

/**
 * Barra de poder: se llena sola en 10 segundos y sube un cuarto cuando el jugador recibe un gol.
 * La carga va de 0 (vacía) a 1 (llena).
 */
public class BarraDePoder {

    public static final double TIEMPO_DE_LLENADO = 10;

    private double carga;

    public void cargar(double segundos) {
        carga = Math.min(1, carga + segundos / TIEMPO_DE_LLENADO);
    }

    public void sumarCuarto() {
        carga = Math.min(1, carga + 0.25);
    }

    public boolean estaLlena() {
        return carga >= 1;
    }

    public void vaciar() {
        carga = 0;
    }

    public double getCarga() {
        return carga;
    }
}
