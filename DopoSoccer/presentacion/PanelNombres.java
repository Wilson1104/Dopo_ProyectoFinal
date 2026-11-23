package presentacion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import dominio.Cuenta;
import dominio.DOPOSoccer;
import dominio.ExcepcionDOPOSoccer;
import dominio.Ranura;

/**
 * Primera pantalla: un título grande y los nombres del jugador 1 y el jugador 2.
 * Enter pasa del primer nombre al segundo y, en el segundo, continúa a la pantalla de preparación.
 */
class PanelNombres extends JPanel {

    private static final long serialVersionUID = 1L;
    /** El juego usa una sola ranura: las cuentas de los dos jugadores que están jugando. */
    private static final int RANURA = 1;

    private final DOPOSoccerGUI ventana;
    private final DOPOSoccer soccer;
    private final JTextField nombre1 = campo();
    private final JTextField nombre2 = campo();
    private final BotonJuego cargar = new BotonJuego("Cargar partida", BotonJuego.Tono.AZUL);
    private final JLabel sinPartida = Estilo.etiqueta(ExcepcionDOPOSoccer.NO_HAY_PARTIDA, Font.PLAIN, 14);

    PanelNombres(DOPOSoccerGUI ventana, DOPOSoccer soccer) {
        super(new GridBagLayout());
        this.ventana = ventana;
        this.soccer = soccer;

        Titulo juego = new Titulo("DOPOSoccer", 42, new Color(255, 240, 90), new Color(255, 128, 0));
        Titulo titulo = new Titulo("Pongan sus nombres", 68, Color.WHITE);
        JLabel ayuda = Estilo.etiqueta("Escribe el nombre de cada jugador y presiona Enter.", Font.PLAIN, 16);
        ayuda.setForeground(Estilo.TEXTO_SUAVE);
        ayuda.setHorizontalAlignment(SwingConstants.CENTER);

        BotonJuego continuar = new BotonJuego("Continuar", BotonJuego.Tono.VERDE);
        continuar.setPreferredSize(new Dimension(230, 58));
        continuar.addActionListener(e -> continuar());
        cargar.setPreferredSize(new Dimension(230, 58));
        cargar.addActionListener(e -> cargarPartida());
        sinPartida.setForeground(Estilo.TEXTO_SUAVE);
        sinPartida.setHorizontalAlignment(SwingConstants.CENTER);
        nombre1.addActionListener(e -> nombre2.requestFocusInWindow());
        nombre2.addActionListener(e -> continuar());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(0, 0, 0, 0);
        add(juego, c);
        c.insets = new Insets(0, 0, 4, 0);
        add(titulo, c);
        c.insets = new Insets(0, 0, 34, 0);
        add(ayuda, c);
        fila(1, "Jugador 1", nombre1, 3);
        fila(2, "Jugador 2", nombre2, 4);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        botones.setOpaque(false);
        botones.add(continuar);
        botones.add(cargar);
        c.gridy = 5;
        c.insets = new Insets(28, 0, 0, 0);
        add(botones, c);
        c.gridy = 6;
        c.insets = new Insets(12, 0, 0, 0);
        add(sinPartida, c);
    }

    /** Llena los campos con los nombres que ya había (al volver desde la preparación) y pone el foco en el primero. */
    void preparar() {
        boolean hayPartida = DOPOSoccerGUI.ARCHIVO_PARTIDA.exists();
        cargar.setEnabled(hayPartida);
        sinPartida.setVisible(!hayPartida);
        try {
            Ranura ranura = soccer.getRanura(RANURA);
            if (!ranura.estaVacia()) {
                nombre1.setText(ranura.getCuenta(1).getNombre());
                nombre2.setText(ranura.getCuenta(2).getNombre());
            }
        } catch (Exception e) {
            ventana.manejarError(e);
        }
        SwingUtilities.invokeLater(() -> {
            nombre1.requestFocusInWindow();
            nombre1.selectAll();
        });
    }

    /** Abre la partida guardada y pasa directo a la preparación. */
    private void cargarPartida() {
        try {
            soccer.abrir(DOPOSoccerGUI.ARCHIVO_PARTIDA);
            ventana.mostrarConfiguracion();
        } catch (Exception e) {
            ventana.manejarError(e);
        }
    }

    /**
     * Si los nombres son los mismos de antes se conservan las cuentas (y sus puntos); si cambian, se crean nuevas.
     * Un campo vacío toma el nombre "Jugador 1" o "Jugador 2".
     */
    private void continuar() {
        String n1 = nombre1.getText().trim().isEmpty() ? "Jugador 1" : nombre1.getText().trim();
        String n2 = nombre2.getText().trim().isEmpty() ? "Jugador 2" : nombre2.getText().trim();
        try {
            Ranura ranura = soccer.getRanura(RANURA);
            if (!ranura.estaVacia() && mismos(ranura.getCuenta(1), n1) && mismos(ranura.getCuenta(2), n2)) {
                soccer.seleccionarRanura(RANURA);
            } else {
                soccer.nuevoJuego(RANURA, n1, n2);
            }
            ventana.mostrarConfiguracion();
        } catch (Exception e) {
            ventana.manejarError(e);
        }
    }

    private static boolean mismos(Cuenta cuenta, String nombre) {
        return cuenta.getNombre().equalsIgnoreCase(nombre);
    }

    private void fila(int numero, String texto, JTextField campo, int fila) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(6, 0, 6, 14);
        c.anchor = GridBagConstraints.EAST;
        add(new Titulo(texto, 30, numero == 1 ? new Color(255, 230, 90) : new Color(120, 220, 255)), c);
        c.gridx = 1;
        c.insets = new Insets(8, 0, 8, 0);
        c.anchor = GridBagConstraints.WEST;
        add(campo, c);
    }

    private static JTextField campo() {
        JTextField campo = new JTextField(16);
        campo.setFont(Estilo.letra(Font.PLAIN, 20));
        campo.setPreferredSize(new Dimension(300, 40));
        campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilo.ACENTO, 2),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return campo;
    }

    /** La pelota de fondo, con una capa oscura encima para que se lean el título y los campos. */
    @Override
    protected void paintComponent(Graphics g) {
        Estilo.pintarCubriendo(g, this, Estilo.imagen("fondo_nombres.jpg"), 0.5, true);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(5, 15, 35, 160));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}
