package dominio;

import java.io.Serializable;

/**
 * Estadísticas de una cuenta. Todas empiezan en 1 y suben hasta 10; cada nivel por encima de 1
 * suma un 10 % sobre el valor base. Comprar un nivel cuesta el doble que el anterior (500, 1000, 2000...).
 */
public class Estadisticas implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int NIVEL_MAXIMO = 10;
    public static final int PRECIO_BASE = 500;

    private int velocidad = 1;
    private int salto = 1;
    private int patada = 1;
    private int supervivencia = 1;

    public int getNivel(TipoEstadistica tipo) {
        switch (tipo) {
            case VELOCIDAD:
                return velocidad;
            case SALTO:
                return salto;
            case PATADA:
                return patada;
            default:
                return supervivencia;
        }
    }

    /** Precio del siguiente nivel: 500 para pasar de 1 a 2, 1000 de 2 a 3, y así. */
    public int precioMejora(TipoEstadistica tipo) throws ExcepcionDOPOSoccer {
        int nivel = getNivel(tipo);
        if (nivel >= NIVEL_MAXIMO) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.ESTADISTICA_MAXIMA);
        return PRECIO_BASE * (1 << (nivel - 1));
    }

    public void mejorar(TipoEstadistica tipo) throws ExcepcionDOPOSoccer {
        int nivel = getNivel(tipo);
        if (nivel >= NIVEL_MAXIMO) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.ESTADISTICA_MAXIMA);
        switch (tipo) {
            case VELOCIDAD:
                velocidad++;
                break;
            case SALTO:
                salto++;
                break;
            case PATADA:
                patada++;
                break;
            default:
                supervivencia++;
        }
    }

    public double factorVelocidad() {
        return factor(velocidad);
    }

    public double factorSalto() {
        return factor(salto);
    }

    /** multiplicadorKick = 1 + 0,10 * (Kick - 1) */
    public double multiplicadorPatada() {
        return factor(patada);
    }

    /** vidaMax = 100 * (1 + 0,10 * (Survival - 1)) */
    public double vidaMaxima() {
        return 100 * factor(supervivencia);
    }

    private static double factor(int nivel) {
        return 1 + 0.10 * (nivel - 1);
    }
}
