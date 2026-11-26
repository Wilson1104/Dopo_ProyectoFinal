package dominio;

/**
 * Errores propios del juego. Cada constante es un mensaje claro para mostrarle al usuario.
 */
public class ExcepcionDOPOSoccer extends Exception {

    private static final long serialVersionUID = 1L;

    public static final String PUNTOS_INSUFICIENTES = "No tienes puntos suficientes para comprar esta mejora.";
    public static final String ESTADISTICA_MAXIMA = "Esa estadística ya está en el nivel máximo (10).";
    public static final String PODER_NO_DISPONIBLE =
        "El poder no está disponible: la barra no está llena o ya tienes un poder activo.";
    public static final String PAIS_DESCONOCIDO = "El país elegido no existe.";
    public static final String CONFIGURACION_INVALIDA =
        "La configuración del partido no es válida: los minutos o goles deben ser mayores que cero.";
    public static final String PERFIL_DESCONOCIDO = "El perfil de máquina elegido no existe.";
    public static final String NOMBRE_INVALIDO = "Cada jugador necesita un nombre no vacío y distinto al del otro.";
    public static final String RANURA_INVALIDA = "La ranura elegida no existe o está vacía.";
    public static final String SIN_JUEGO = "Primero crea un juego nuevo o continúa uno de una ranura.";
    public static final String SIN_PARTIDO = "No hay un partido en curso.";
    public static final String PARTIDO_EN_CURSO = "Las estadísticas solo se pueden mejorar antes de empezar un partido.";
    public static final String ERROR_DE_ARCHIVO = "No se pudo guardar o abrir la partida.";
    public static final String NO_HAY_PARTIDA = "No hay ninguna partida guardada en este dispositivo.";

    public ExcepcionDOPOSoccer(String mensaje) {
        super(mensaje);
    }
}
