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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import dominio.Cuenta;
import dominio.DOPOSoccer;
import dominio.Estadisticas;
import dominio.ExcepcionDOPOSoccer;
import dominio.TipoEstadistica;

/** Tienda de mejoras: cada jugador gasta sus puntos para subir de nivel sus estadísticas. */
class DialogoMejoras extends JDialog {

    private static final long serialVersionUID = 1L;

    private final DOPOSoccerGUI ventana;
    private final DOPOSoccer soccer;
    private final JPanel columnas = new JPanel(new GridLayout(1, 2, 20, 0));

    DialogoMejoras(DOPOSoccerGUI ventana, DOPOSoccer soccer) {
        super(ventana, "Tienda", true);
        this.ventana = ventana;
        this.soccer = soccer;
        JPanel contenido = new JPanel(new BorderLayout(0, 12)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Estilo.pintarCubriendo(g, this, Estilo.imagen("fondo_tienda.jpg"), 0.5, true);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(5, 15, 35, 120));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 20, 14, 20));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(new Titulo("Tienda", 46, new Color(255, 240, 90), new Color(255, 128, 0)), BorderLayout.NORTH);
        JLabel explicacion = Estilo.etiqueta("Cada nivel suma un 10 % sobre el valor base. El precio se duplica con cada "
            + "compra: 500, 1000, 2000... Los niveles van de 1 a 10.", Font.PLAIN, 14);
        explicacion.setForeground(Estilo.TEXTO_SUAVE);
        explicacion.setHorizontalAlignment(SwingConstants.CENTER);
        arriba.add(explicacion, BorderLayout.SOUTH);
        contenido.add(arriba, BorderLayout.NORTH);

        columnas.setOpaque(false);
        contenido.add(columnas, BorderLayout.CENTER);

        BotonJuego cerrar = new BotonJuego("Cerrar", BotonJuego.Tono.AZUL);
        cerrar.setPreferredSize(new Dimension(150, 46));
        cerrar.addActionListener(e -> {
            dispose();
            ventana.guardarPartida();
        });
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        abajo.setOpaque(false);
        abajo.add(cerrar);
        contenido.add(abajo, BorderLayout.SOUTH);
        setContentPane(contenido);
        refrescar();
        setSize(920, 520);
        setLocationRelativeTo(ventana);
    }

    private void refrescar() {
        columnas.removeAll();
        try {
            for (int j = 1; j <= 2; j++) columnas.add(columna(j, soccer.getRanura(soccer.getRanuraActual()).getCuenta(j)));
        } catch (ExcepcionDOPOSoccer e) {
            ventana.manejarError(e);
        }
        columnas.revalidate();
        columnas.repaint();
    }

    /** Recuadro de un jugador: su nombre y puntos, y una fila por estadística con su nivel y el botón para mejorarla. */
    private JPanel columna(int jugador, Cuenta cuenta) throws ExcepcionDOPOSoccer {
        JPanel panel = new Recuadro(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 3;
        c.insets = new Insets(0, 0, 6, 0);
        panel.add(new Titulo(cuenta.getNombre() + "  ·  " + cuenta.getPuntos() + " pts", 26, jugador == 1
            ? new Color(255, 230, 90) : new Color(120, 220, 255)), c);

        Estadisticas estadisticas = cuenta.getEstadisticas();
        int fila = 1;
        for (TipoEstadistica tipo : TipoEstadistica.values()) {
            int nivel = estadisticas.getNivel(tipo);
            c = new GridBagConstraints();
            c.gridy = fila++;
            c.insets = new Insets(3, 2, 3, 8);
            c.anchor = GridBagConstraints.WEST;
            panel.add(Estilo.etiquetaTitulo(nombre(tipo), 17), c);
            c.gridx = 1;
            panel.add(Estilo.etiqueta("Nivel " + nivel + " / " + Estadisticas.NIVEL_MAXIMO, Font.BOLD, 13), c);
            c.gridx = 2;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            BotonJuego comprar;
            if (nivel >= Estadisticas.NIVEL_MAXIMO) {
                comprar = new BotonJuego("Nivel máximo", BotonJuego.Tono.GRIS);
                comprar.setEnabled(false);
            } else {
                comprar = new BotonJuego("Mejorar  " + estadisticas.precioMejora(tipo) + " pts", BotonJuego.Tono.VERDE);
                comprar.addActionListener(e -> comprar(jugador, tipo));
            }
            comprar.setPreferredSize(new Dimension(160, 40));
            panel.add(comprar, c);
        }
        return panel;
    }

    private void comprar(int jugador, TipoEstadistica tipo) {
        try {
            soccer.mejorarEstadistica(jugador, tipo);
        } catch (Exception e) {
            ventana.manejarError(e);
        }
        refrescar();
    }

    private static String nombre(TipoEstadistica tipo) {
        switch (tipo) {
            case VELOCIDAD:
                return "Velocidad (speed)";
            case SALTO:
                return "Salto (jump)";
            case PATADA:
                return "Patada (kick)";
            default:
                return "Supervivencia (vida)";
        }
    }

    /** Panel con fondo oscuro translúcido y borde dorado redondeado. */
    private static class Recuadro extends JPanel {

        private static final long serialVersionUID = 1L;

        Recuadro(LayoutManager distribucion) {
            super(distribucion);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(10, 20, 45, 232));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.setColor(Estilo.ACENTO);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 22, 22);
            g2.dispose();
        }
    }
}
