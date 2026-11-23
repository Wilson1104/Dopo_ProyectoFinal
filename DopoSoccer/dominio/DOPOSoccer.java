package dominio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Punto de entrada del dominio: la presentación solo habla con esta clase.
 * Maneja las dos ranuras de juego y el partido en curso.
 */
public class DOPOSoccer {

    public static final int RANURAS = 2;
    public static final String[] PERFILES_MAQUINA = {"Torpe", "Ofensiva", "Defensiva", "Experta"};

    private final Ranura[] ranuras = new Ranura[RANURAS];
    private int ranuraActual;
    private Partido partido;

    public DOPOSoccer() {
        for (int i = 0; i < RANURAS; i++) ranuras[i] = new Ranura(i + 1);
    }

    /** Crea (o reemplaza) las dos cuentas de una ranura y la deja como la ranura en juego. */
    public void nuevoJuego(int ranura, String nombre1, String nombre2) throws ExcepcionDOPOSoccer {
        getRanura(ranura).crearCuentas(nombre1, nombre2);
        ranuraActual = ranura;
        partido = null;
    }

    /** Continúa con las cuentas que ya tiene una ranura. */
    public void seleccionarRanura(int ranura) throws ExcepcionDOPOSoccer {
        if (getRanura(ranura).estaVacia()) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.RANURA_INVALIDA);
        ranuraActual = ranura;
        partido = null;
    }

    /**
     * @param perfilMaquina null para jugador vs jugador; si no, uno de {@link #PERFILES_MAQUINA}
     * @param porTiempo true: limite son minutos; false: limite son los goles para ganar
     */
    public void nuevoPartido(Resolucion resolucion, String pais1, String pais2, String perfilMaquina,
                             boolean porTiempo, int limite) throws ExcepcionDOPOSoccer {
        if (ranuraActual == 0) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.SIN_JUEGO);
        if (resolucion == null || limite <= 0) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.CONFIGURACION_INVALIDA);
        Personaje personaje1 = Personaje.crear(pais1);
        Personaje personaje2 = Personaje.crear(pais2);
        Ranura ranura = getRanura(ranuraActual);
        Jugador jugador1 = new JugadorHumano(Lado.IZQUIERDO, personaje1, ranura.getCuenta(1));
        Jugador jugador2 = perfilMaquina == null
            ? new JugadorHumano(Lado.DERECHO, personaje2, ranura.getCuenta(2))
            : crearMaquina(perfilMaquina, personaje2);
        partido = porTiempo ? new PartidoPorTiempo(resolucion, jugador1, jugador2, limite)
                            : new PartidoPorGoles(resolucion, jugador1, jugador2, limite);
    }

    public void moverIzquierda(int jugador) throws ExcepcionDOPOSoccer {
        partidoEnCurso().getJugador(jugador).moverIzquierda();
    }

    public void moverDerecha(int jugador) throws ExcepcionDOPOSoccer {
        partidoEnCurso().getJugador(jugador).moverDerecha();
    }

    public void detener(int jugador) throws ExcepcionDOPOSoccer {
        partidoEnCurso().getJugador(jugador).detener();
    }

    public void saltar(int jugador) throws ExcepcionDOPOSoccer {
        partidoEnCurso().getJugador(jugador).saltar();
    }

    public void patear(int jugador) throws ExcepcionDOPOSoccer {
        partidoEnCurso().getJugador(jugador).patear();
    }

    public void activarPoder(int jugador) throws ExcepcionDOPOSoccer {
        partidoEnCurso().getJugador(jugador).activarPoder();
    }

    public void actualizar(double segundos) throws ExcepcionDOPOSoccer {
        partidoEnCurso().actualizar(segundos);
    }

    public void pausar() throws ExcepcionDOPOSoccer {
        partidoEnCurso().pausar();
    }

    public void reanudar() throws ExcepcionDOPOSoccer {
        partidoEnCurso().reanudar();
    }

    public void terminar() throws ExcepcionDOPOSoccer {
        partidoEnCurso().terminar();
    }

    /** Compra un nivel de estadística con los puntos de la cuenta; solo antes de empezar un partido. */
    public void mejorarEstadistica(int jugador, TipoEstadistica tipo) throws ExcepcionDOPOSoccer {
        if (ranuraActual == 0) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.SIN_JUEGO);
        if (partido != null && !partido.isTerminado()) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.PARTIDO_EN_CURSO);
        getRanura(ranuraActual).getCuenta(jugador).comprarMejora(tipo);
    }

    /** Guarda las dos ranuras en el archivo. Si todavía no hay cuentas, no crea nada. */
    public void guardar(File archivo) throws ExcepcionDOPOSoccer {
        if (ranuras[0].estaVacia() && ranuras[1].estaVacia()) return;
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(archivo))) {
            salida.writeObject(ranuras);
            salida.writeInt(ranuraActual);
        } catch (IOException e) {
            throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.ERROR_DE_ARCHIVO);
        }
    }

    /** Carga la partida guardada. Si el archivo falta o está dañado, el juego no cambia. */
    public void abrir(File archivo) throws ExcepcionDOPOSoccer {
        if (!archivo.exists()) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.NO_HAY_PARTIDA);
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo))) {
            Ranura[] leidas = (Ranura[]) entrada.readObject();
            int actual = entrada.readInt();
            if (leidas == null || leidas.length != RANURAS) throw new IOException("Formato distinto");
            System.arraycopy(leidas, 0, ranuras, 0, RANURAS);
            ranuraActual = actual;
            partido = null;
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.ERROR_DE_ARCHIVO);
        }
    }

    public Partido getPartido() {
        return partido;
    }

    /** @param numero 1 o 2 */
    public Ranura getRanura(int numero) throws ExcepcionDOPOSoccer {
        if (numero < 1 || numero > RANURAS) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.RANURA_INVALIDA);
        return ranuras[numero - 1];
    }

    public int getRanuraActual() {
        return ranuraActual;
    }

    private Partido partidoEnCurso() throws ExcepcionDOPOSoccer {
        if (partido == null) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.SIN_PARTIDO);
        return partido;
    }

    private Maquina crearMaquina(String perfil, Personaje personaje) throws ExcepcionDOPOSoccer {
        switch (perfil) {
            case "Torpe":
                return new MaquinaTorpe(Lado.DERECHO, personaje);
            case "Ofensiva":
                return new MaquinaOfensiva(Lado.DERECHO, personaje);
            case "Defensiva":
                return new MaquinaDefensiva(Lado.DERECHO, personaje);
            case "Experta":
                return new MaquinaExperta(Lado.DERECHO, personaje);
            default:
                throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.PERFIL_DESCONOCIDO);
        }
    }
}
