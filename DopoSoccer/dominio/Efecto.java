package dominio;

import java.io.Serializable;

/**
 * Efecto temporal que sufre un jugador. Por defecto no restringe nada; cada efecto concreto
 * dice qué impide y qué le pasa al empezar o al terminar.
 */
public abstract class Efecto implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double duracion;
    private double tiempoRestante;

    protected Efecto(double duracion) {
        this.duracion = duracion;
        tiempoRestante = duracion;
    }

    public void actualizar(double segundos) {
        tiempoRestante -= segundos;
    }

    public boolean haTerminado() {
        return tiempoRestante <= 0;
    }

    public void iniciar(Jugador jugador) {
    }

    public void finalizar(Jugador jugador) {
    }

    public boolean permiteMoverse() {
        return true;
    }

    public boolean permiteSaltar() {
        return true;
    }

    public boolean permitePatear() {
        return true;
    }

    public double factorVelocidad() {
        return 1;
    }

    public String getNombre() {
        return getClass().getSimpleName();
    }

    public double getDuracion() {
        return duracion;
    }

    public double getTiempoRestante() {
        return Math.max(0, tiempoRestante);
    }
}
