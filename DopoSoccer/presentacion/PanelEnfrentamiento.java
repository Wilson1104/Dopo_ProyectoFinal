package presentacion;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import dominio.DOPOSoccer;
import dominio.ExcepcionDOPOSoccer;
import dominio.Resolucion;

/**
 * Presentación del partido antes de empezar: el estadio de fondo, la bandera de cada país
 * con el nombre del jugador debajo y el botón Jugar en el medio.
 * Debajo del botón se elige el tipo de partido y la resolución de la cancha.
 */
class PanelEnfrentamiento extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int[] MINUTOS = {1, 2, 3, 5};
    private static final int[] GOLES = {1, 3, 5, 10};
    /** Pantalla del marcador del estadio en la imagen de fondo. */
    private static final double MARCADOR_X = 372;
    private static final double MARCADOR_Y = 296;
    private static final double ALINEACION_FONDO = 0.75;

    private final DOPOSoccerGUI ventana;
    private final DOPOSoccer soccer;
    private final Casilla jugar = new Casilla("Jugar", BotonJuego.Tono.ROJO, true);
    private final JButton partido = Estilo.boton("");
    private final JButton pantalla = Estilo.boton("");

    private String pais1;
    private String pais2;
    private String perfilMaquina;
    private Resolucion resolucion = Resolucion.BAJA;
    private boolean porTiempo = true;
    private int limite = 2;

    PanelEnfrentamiento(DOPOSoccerGUI ventana, DOPOSoccer soccer) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.soccer = soccer;

        JButton volver = Estilo.boton("← Volver");
        volver.addActionListener(e -> ventana.mostrarConfiguracion());
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        arriba.setOpaque(false);
        arriba.add(volver);
        add(arriba, BorderLayout.NORTH);

        jugar.setPreferredSize(new Dimension(250, 90));
        jugar.addActionListener(e -> jugar());
        partido.addActionListener(e -> menuPartido().show(partido, 0, partido.getHeight()));
        pantalla.addActionListener(e -> menuPantalla().show(pantalla, 0, pantalla.getHeight()));
        JPanel opciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        opciones.setOpaque(false);
        opciones.add(partido);
        opciones.add(pantalla);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        centro.add(jugar, c);
        c.gridy = 1;
        c.insets = new Insets(12, 0, 0, 0);
        centro.add(opciones, c);
        add(centro, BorderLayout.CENTER);
        actualizarOpciones();
    }

    /** Recibe lo elegido en la preparación: los dos países y el perfil de la máquina (null si juegan dos personas). */
    void preparar(String pais1, String pais2, String perfilMaquina) {
        this.pais1 = pais1;
        this.pais2 = pais2;
        this.perfilMaquina = perfilMaquina;
        actualizarOpciones();
        repaint();
    }

    private void jugar() {
        try {
            soccer.nuevoPartido(resolucion, pais1, pais2, perfilMaquina, porTiempo, limite);
            ventana.mostrarJuego(resolucion, perfilMaquina != null, perfilMaquina);
        } catch (Exception e) {
            ventana.manejarError(e);
        }
    }

    // ------------------------------------------------------------------ tipo de partido y resolución

    private JPopupMenu menuPartido() {
        JPopupMenu menu = new JPopupMenu();
        ButtonGroup grupo = new ButtonGroup();
        JMenu tiempo = submenu("Por tiempo");
        for (int minutos : MINUTOS) {
            JRadioButtonMenuItem item = PanelConfiguracion.radio(minutos + (minutos == 1 ? " minuto" : " minutos"),
                porTiempo && limite == minutos, grupo);
            item.addActionListener(e -> elegirLimite(true, minutos));
            tiempo.add(item);
        }
        tiempo.add(otro(true));
        JMenu goles = submenu("Por goles");
        for (int cantidad : GOLES) {
            JRadioButtonMenuItem item = PanelConfiguracion.radio(cantidad + (cantidad == 1 ? " gol" : " goles"),
                !porTiempo && limite == cantidad, grupo);
            item.addActionListener(e -> elegirLimite(false, cantidad));
            goles.add(item);
        }
        goles.add(otro(false));
        menu.add(tiempo);
        menu.add(goles);
        return menu;
    }

    private JPopupMenu menuPantalla() {
        JPopupMenu menu = new JPopupMenu();
        ButtonGroup grupo = new ButtonGroup();
        for (Resolucion r : Resolucion.values()) {
            JRadioButtonMenuItem item = PanelConfiguracion.radio(nombre(r) + " (" + r.getAncho() + " x " + r.getAlto() + ")",
                r == resolucion, grupo);
            item.addActionListener(e -> {
                resolucion = r;
                actualizarOpciones();
            });
            menu.add(item);
        }
        return menu;
    }

    private JMenuItem otro(boolean minutos) {
        JMenuItem item = new JMenuItem("Otro...");
        item.setFont(Estilo.letra(Font.PLAIN, 14));
        item.addActionListener(e -> {
            String texto = JOptionPane.showInputDialog(ventana,
                minutos ? "¿Cuántos minutos dura el partido?" : "¿A cuántos goles se gana?", "Tipo de partido",
                JOptionPane.QUESTION_MESSAGE);
            if (texto == null) return;
            try {
                int valor = Integer.parseInt(texto.trim());
                if (valor < 1 || valor > (minutos ? 60 : 99)) throw new NumberFormatException();
                elegirLimite(minutos, valor);
            } catch (NumberFormatException ex) {
                ventana.manejarError(new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.CONFIGURACION_INVALIDA));
            }
        });
        return item;
    }

    private void elegirLimite(boolean tiempo, int valor) {
        porTiempo = tiempo;
        limite = valor;
        actualizarOpciones();
    }

    private void actualizarOpciones() {
        String tipo = porTiempo ? limite + (limite == 1 ? " minuto" : " minutos") : "a " + limite + (limite == 1 ? " gol" : " goles");
        partido.setText("Partido: " + tipo + "  ▼");
        pantalla.setText("Pantalla: " + nombre(resolucion) + "  ▼");
    }

    private static JMenu submenu(String texto) {
        JMenu menu = new JMenu(texto);
        menu.setFont(Estilo.letra(Font.PLAIN, 14));
        return menu;
    }

    private static String nombre(Resolucion r) {
        switch (r) {
            case BAJA:
                return "Baja";
            case MEDIA:
                return "Media";
            default:
                return "Alta";
        }
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    protected void paintComponent(Graphics g) {
        BufferedImage fondo = Estilo.imagen("fondo_versus.jpg");
        Estilo.pintarCubriendo(g, this, fondo, ALINEACION_FONDO, true);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth();
        int h = getHeight();

        if (fondo != null) {
            double escala = Math.max(w / (double) fondo.getWidth(), h / (double) fondo.getHeight());
            double x0 = (w - fondo.getWidth() * escala) / 2;
            double y0 = (h - fondo.getHeight() * escala) * ALINEACION_FONDO;
            Estilo.textoContorno(g2, "VS", Estilo.titulo((float) (54 * escala)), x0 + MARCADOR_X * escala,
                y0 + MARCADOR_Y * escala + 16 * escala, Estilo.ACENTO, 6f);
        }

        double anchoBandera = Math.min(250, w * 0.26);
        double altoBandera = anchoBandera * 0.62;
        double yBandera = h * 0.36;
        dibujarJugador(g2, pais1, nombre(1), w * 0.19, yBandera, anchoBandera, altoBandera);
        dibujarJugador(g2, pais2, perfilMaquina != null ? "Máquina" : nombre(2), w * 0.81, yBandera, anchoBandera, altoBandera);
        g2.dispose();
    }

    /** Bandera dentro de un marco blanco y, debajo, el nombre con contorno. */
    private void dibujarJugador(Graphics2D g2, String pais, String nombre, double cx, double y, double anchoCaja, double altoCaja) {
        BufferedImage bandera = Estilo.bandera(pais);
        double ancho = anchoCaja;
        double alto = altoCaja;
        if (bandera != null) {
            double escala = Math.min(anchoCaja / bandera.getWidth(), altoCaja / bandera.getHeight());
            ancho = bandera.getWidth() * escala;
            alto = bandera.getHeight() * escala;
        }
        int x = (int) Math.round(cx - ancho / 2);
        int top = (int) Math.round(y + (altoCaja - alto) / 2);
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRect(x + 6, top + 8, (int) ancho, (int) alto);
        if (bandera != null) {
            g2.drawImage(bandera, x, top, (int) Math.round(ancho), (int) Math.round(alto), null);
        } else {
            g2.setColor(Color.GRAY);
            g2.fillRect(x, top, (int) ancho, (int) alto);
        }
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(5f));
        g2.drawRect(x - 2, top - 2, (int) Math.round(ancho) + 4, (int) Math.round(alto) + 4);
        g2.setColor(Estilo.CONTORNO);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x - 5, top - 5, (int) Math.round(ancho) + 10, (int) Math.round(alto) + 10);

        Estilo.textoContorno(g2, nombre, Estilo.titulo(40), cx, y + altoCaja + 56, Color.WHITE, 6f);
        Estilo.textoContorno(g2, pais == null ? "" : pais, Estilo.titulo(21), cx, y + altoCaja + 86,
            new Color(255, 230, 120), 4f);
    }

    private String nombre(int jugador) {
        try {
            return soccer.getRanura(soccer.getRanuraActual()).getCuenta(jugador).getNombre();
        } catch (ExcepcionDOPOSoccer e) {
            return "Jugador " + jugador;
        }
    }
}
