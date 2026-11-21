package presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import dominio.Cuenta;
import dominio.DOPOSoccer;
import dominio.ExcepcionDOPOSoccer;
import dominio.Personaje;

/**
 * Pantalla de preparación: la imagen de fondo y, abajo, tres casillas: Tienda, Personaje y Jugar.
 * Personaje abre un menú con el país de cada jugador y el rival (el jugador 2 o una máquina).
 * Jugar lleva al panel de presentación del partido.
 */
class PanelConfiguracion extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DOPOSoccerGUI ventana;
    private final DOPOSoccer soccer;

    private final Casilla tienda = new Casilla("Tienda", BotonJuego.Tono.NARANJA, false);
    private final Casilla personaje = new Casilla("Personaje", BotonJuego.Tono.VERDE, false);
    private final Casilla jugar = new Casilla("Jugar", BotonJuego.Tono.ROJO, true);

    private boolean contraMaquina;
    private String perfilMaquina = "Ofensiva";
    private String pais1 = Personaje.COREA_DEL_SUR;
    private String pais2 = Personaje.CAMERUN;

    PanelConfiguracion(DOPOSoccerGUI ventana, DOPOSoccer soccer) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.soccer = soccer;
        setOpaque(true);

        JButton volver = Estilo.boton("← Volver");
        volver.addActionListener(e -> ventana.mostrarNombres());
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 14));
        arriba.setOpaque(false);
        arriba.add(volver);
        add(arriba, BorderLayout.NORTH);

        JPanel casillas = new JPanel(new GridLayout(1, 3, 22, 0));
        casillas.setOpaque(false);
        casillas.setBorder(BorderFactory.createEmptyBorder(0, 50, 10, 50));
        casillas.add(tienda);
        casillas.add(personaje);
        casillas.add(jugar);
        add(casillas, BorderLayout.SOUTH);

        tienda.addActionListener(e -> abrirTienda());
        personaje.addActionListener(e -> mostrar(menuPersonaje(), personaje));
        jugar.addActionListener(e -> ventana.mostrarPresentacion(pais1, pais2, contraMaquina ? perfilMaquina : null));
    }

    /** Actualiza lo que dice cada casilla con los jugadores y las opciones elegidas. */
    void refrescar() {
        try {
            Cuenta c1 = cuenta(1);
            Cuenta c2 = cuenta(2);
            String rival = contraMaquina ? "Máquina" : c2.getNombre();
            tienda.detalle(c1.getNombre() + ": " + c1.getPuntos() + " pts", c2.getNombre() + ": " + c2.getPuntos() + " pts");
            personaje.detalle(c1.getNombre() + ": " + pais1, rival + ": " + pais2);
            jugar.detalle(c1.getNombre() + " vs " + rival, contraMaquina ? "Máquina " + perfilMaquina.toLowerCase() : "Jugador vs jugador");
        } catch (ExcepcionDOPOSoccer e) {
            ventana.manejarError(e);
            ventana.mostrarNombres();
        }
    }

    private void abrirTienda() {
        new DialogoMejoras(ventana, soccer).setVisible(true);
        refrescar();
    }

    /** País del jugador 1, país del rival y quién es el rival: el jugador 2 o una máquina con su perfil. */
    private JPopupMenu menuPersonaje() {
        JPopupMenu menu = new JPopupMenu();
        String nombre1 = "Jugador 1";
        String nombre2 = "Jugador 2";
        try {
            nombre1 = cuenta(1).getNombre();
            nombre2 = cuenta(2).getNombre();
        } catch (ExcepcionDOPOSoccer e) {
            // sin cuentas todavía: se dejan los nombres genéricos
        }
        menu.add(menuPaises(nombre1, pais1, true));
        menu.add(menuPaises(contraMaquina ? "Máquina" : nombre2, pais2, false));
        menu.addSeparator();

        JMenu rival = new JMenu("Rival: " + (contraMaquina ? "máquina " + perfilMaquina.toLowerCase() : nombre2));
        rival.setFont(Estilo.letra(Font.PLAIN, 14));
        ButtonGroup grupo = new ButtonGroup();
        JRadioButtonMenuItem jugador2 = radio(nombre2 + " (jugador 2)", !contraMaquina, grupo);
        jugador2.addActionListener(e -> {
            contraMaquina = false;
            refrescar();
        });
        rival.add(jugador2);
        rival.addSeparator();
        for (String perfil : DOPOSoccer.PERFILES_MAQUINA) {
            JRadioButtonMenuItem item = radio("Máquina " + perfil.toLowerCase(), contraMaquina && perfil.equals(perfilMaquina), grupo);
            item.addActionListener(e -> {
                contraMaquina = true;
                perfilMaquina = perfil;
                refrescar();
            });
            rival.add(item);
        }
        menu.add(rival);
        return menu;
    }

    private JMenu menuPaises(String titulo, String actual, boolean primero) {
        JMenu menu = new JMenu(titulo + "  (" + actual + ")");
        menu.setFont(Estilo.letra(Font.PLAIN, 14));
        ButtonGroup grupo = new ButtonGroup();
        for (String pais : Personaje.paisesDisponibles()) {
            JRadioButtonMenuItem item = radio(pais, pais.equals(actual), grupo);
            item.addActionListener(e -> {
                if (primero) pais1 = pais;
                else pais2 = pais;
                refrescar();
            });
            menu.add(item);
        }
        return menu;
    }

    private Cuenta cuenta(int numero) throws ExcepcionDOPOSoccer {
        return soccer.getRanura(soccer.getRanuraActual()).getCuenta(numero);
    }

    private void mostrar(JPopupMenu menu, Casilla casilla) {
        menu.show(casilla, 0, -menu.getPreferredSize().height - 6);
    }

    static JRadioButtonMenuItem radio(String texto, boolean elegido, ButtonGroup grupo) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(texto, elegido);
        item.setFont(Estilo.letra(Font.PLAIN, 14));
        grupo.add(item);
        return item;
    }

    /** La imagen de fondo y, abajo, una franja oscura para que se lea lo que dice debajo de cada botón. */
    @Override
    protected void paintComponent(Graphics g) {
        Estilo.pintarFondoImagen(g, this);
        Graphics2D g2 = (Graphics2D) g;
        int alto = 190;
        g2.setPaint(new GradientPaint(0, getHeight() - alto, new Color(0, 0, 0, 0), 0, getHeight() - 70,
            new Color(5, 10, 25, 215)));
        g2.fillRect(0, getHeight() - alto, getWidth(), alto);
    }
}
