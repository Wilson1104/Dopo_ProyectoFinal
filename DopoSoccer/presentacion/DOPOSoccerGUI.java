package presentacion;

import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import dominio.DOPOSoccer;
import dominio.ExcepcionDOPOSoccer;
import dominio.Jugador;
import dominio.Partido;
import dominio.RegistroDeErrores;
import dominio.Resolucion;

/**
 * Ventana principal de DOPOSoccer. Cambia entre los nombres de los jugadores, la preparación (Tienda,
 * Personaje, Jugar), la presentación del partido y el juego. Es la única que muestra mensajes de error al usuario.
 */
public class DOPOSoccerGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String NOMBRES = "nombres";
    private static final String CONFIGURACION = "configuracion";
    private static final String ENFRENTAMIENTO = "enfrentamiento";
    private static final String JUEGO = "juego";
    /** Dónde se guarda la partida: en la carpeta desde donde se ejecuta el juego. */
    static final File ARCHIVO_PARTIDA = new File("partida-guardada.dat");
    private static final int ANCHO_MENU = 980;
    private static final int ALTO_MENU = 650;

    private final DOPOSoccer soccer = new DOPOSoccer();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final PanelNombres panelNombres;
    private final PanelConfiguracion panelConfiguracion;
    private final PanelEnfrentamiento panelEnfrentamiento;
    private PanelJuego panelJuego;

    public DOPOSoccerGUI() {
        super("DOPOSoccer");
        panelNombres = new PanelNombres(this, soccer);
        panelConfiguracion = new PanelConfiguracion(this, soccer);
        panelEnfrentamiento = new PanelEnfrentamiento(this, soccer);
        contenido.add(panelNombres, NOMBRES);
        contenido.add(panelConfiguracion, CONFIGURACION);
        contenido.add(panelEnfrentamiento, ENFRENTAMIENTO);
        setContentPane(contenido);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
        mostrarNombres();
    }

    /** Primera pantalla: los nombres del jugador 1 y el jugador 2. */
    void mostrarNombres() {
        quitarJuego();
        panelNombres.preparar();
        tarjetas.show(contenido, NOMBRES);
        tamanioDeMenu();
    }

    void mostrarConfiguracion() {
        quitarJuego();
        panelConfiguracion.refrescar();
        tarjetas.show(contenido, CONFIGURACION);
        tamanioDeMenu();
    }

    /** Banderas, nombres y botón Jugar. {@code perfilMaquina} es null cuando juegan dos personas. */
    void mostrarPresentacion(String pais1, String pais2, String perfilMaquina) {
        panelEnfrentamiento.preparar(pais1, pais2, perfilMaquina);
        tarjetas.show(contenido, ENFRENTAMIENTO);
    }

    /** El partido ya fue creado en el dominio; aquí solo se muestra y arranca el ciclo del juego. */
    void mostrarJuego(Resolucion resolucion, boolean contraMaquina, String perfilMaquina) {
        quitarJuego();
        panelJuego = new PanelJuego(this, soccer, resolucion, contraMaquina, perfilMaquina);
        contenido.add(panelJuego, JUEGO);
        tarjetas.show(contenido, JUEGO);
        pack();
        setLocationRelativeTo(null);
        panelJuego.iniciar();
    }

    /** Anuncia el ganador y los puntos obtenidos, y vuelve a la configuración. */
    void partidoTerminado() {
        Partido partido = soccer.getPartido();
        Jugador ganador = partido.getGanador();
        StringBuilder mensaje = new StringBuilder();
        if (ganador == null) {
            mensaje.append("¡Empate!\n\n");
        } else {
            mensaje.append("¡Ganó ").append(ganador.getCuenta().getNombre())
                   .append(" con ").append(ganador.getPersonaje().getPais()).append("!\n\n");
        }
        for (int i = 1; i <= 2; i++) {
            Jugador jugador = partido.getJugador(i);
            int puntos = jugador == ganador ? Partido.PUNTOS_GANADOR : Partido.PUNTOS_PERDEDOR;
            mensaje.append(jugador.getCuenta().getNombre()).append(": ").append(jugador.getGoles())
                   .append(jugador.getGoles() == 1 ? " gol, +" : " goles, +").append(puntos)
                   .append(" puntos (total ").append(jugador.getCuenta().getPuntos()).append(")\n");
        }
        JOptionPane.showMessageDialog(this, mensaje.toString(), "Fin del partido", JOptionPane.INFORMATION_MESSAGE);
        guardarPartida();
        mostrarConfiguracion();
    }

    /** Errores del juego: mensaje claro. Errores inesperados: además se guardan en el log. */
    void manejarError(Exception e) {
        if (e instanceof ExcepcionDOPOSoccer) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "DOPOSoccer", JOptionPane.WARNING_MESSAGE);
        } else {
            RegistroDeErrores.registrar(e);
            JOptionPane.showMessageDialog(this,
                "Ocurrió un error inesperado: " + e.getMessage() + "\nEl detalle quedó en " + RegistroDeErrores.ARCHIVO + ".",
                "DOPOSoccer", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Guarda la partida sin preguntar. Si algo falla, se avisa y el juego sigue. */
    void guardarPartida() {
        try {
            soccer.guardar(ARCHIVO_PARTIDA);
        } catch (ExcepcionDOPOSoccer e) {
            manejarError(e);
        }
    }

    /** Carga la partida guardada. Si había un partido a medias, vuelve a la cancha en pausa; si no, a la configuración. */
    void cargarPartidaGuardada() {
        try {
            soccer.abrir(ARCHIVO_PARTIDA);
            Partido partido = soccer.getPartido();
            if (partido == null) {
                mostrarConfiguracion();
            } else {
                String perfil = soccer.getPerfilMaquina();
                mostrarJuego(partido.getCancha().getResolucion(), perfil != null, perfil);
            }
        } catch (Exception e) {
            manejarError(e);
        }
    }

    private void salir() {
        if (panelJuego != null) panelJuego.pausarSiJuega();
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres salir de DOPOSoccer?", "Salir",
            JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            guardarPartida();
            dispose();
            System.exit(0);
        }
    }

    private void quitarJuego() {
        if (panelJuego == null) return;
        panelJuego.detener();
        contenido.remove(panelJuego);
        panelJuego = null;
    }

    private void tamanioDeMenu() {
        setSize(ANCHO_MENU, ALTO_MENU);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) ->
            RegistroDeErrores.registrar(error instanceof Exception ? (Exception) error : new Exception(error)));
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                RegistroDeErrores.registrar(e);
            }
            new DOPOSoccerGUI().setVisible(true);
        });
    }
}
