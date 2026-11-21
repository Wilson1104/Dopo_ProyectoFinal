package dominio;

/** Progreso de un jugador: su nombre, los puntos acumulados y sus estadísticas. */
public class Cuenta {

    private final String nombre;
    private int puntos;
    private final Estadisticas estadisticas = new Estadisticas();

    public Cuenta(String nombre) {
        this.nombre = nombre;
    }

    public void ganarPuntos(int puntos) {
        if (puntos > 0) this.puntos += puntos;
    }

    public void comprarMejora(TipoEstadistica tipo) throws ExcepcionDOPOSoccer {
        int precio = estadisticas.precioMejora(tipo);
        if (puntos < precio) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.PUNTOS_INSUFICIENTES);
        estadisticas.mejorar(tipo);
        puntos -= precio;
    }

    public Estadisticas getEstadisticas() {
        return estadisticas;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntos() {
        return puntos;
    }
}
