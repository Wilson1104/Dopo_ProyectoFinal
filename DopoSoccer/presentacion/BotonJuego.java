package presentacion;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.ButtonModel;
import javax.swing.JButton;

/**
 * Botón con la forma de los del juego original: una píldora con un aro claro, un borde oscuro grueso, el cuerpo
 * con degradado, un brillo en la mitad de arriba y el texto en Impact itálica con contorno. Al pasar el mouse se
 * aclara y al presionarlo se hunde un poco. Deshabilitado se ve gris.
 */
class BotonJuego extends JButton {

    /** Colores de cada botón: arriba y abajo del degradado, y el aro de afuera. */
    enum Tono {
        NARANJA(new Color(255, 186, 60), new Color(230, 100, 0), new Color(255, 244, 214)),
        VERDE(new Color(140, 226, 70), new Color(36, 148, 28), new Color(238, 255, 220)),
        ROJO(new Color(255, 98, 74), new Color(194, 14, 24), new Color(255, 206, 40)),
        AZUL(new Color(92, 182, 255), new Color(18, 90, 205), new Color(225, 242, 255)),
        GRIS(new Color(190, 194, 204), new Color(112, 118, 130), new Color(226, 229, 234));

        final Color arriba;
        final Color abajo;
        final Color aro;

        Tono(Color arriba, Color abajo, Color aro) {
            this.arriba = arriba;
            this.abajo = abajo;
            this.aro = aro;
        }
    }

    private static final long serialVersionUID = 1L;
    /** Espacio debajo de la píldora para la sombra. */
    protected static final int SOMBRA = 4;
    /** Alto de la píldora cuando el botón toma el tamaño de su texto. */
    private static final int ALTO_NORMAL = 40;

    private final Tono tono;

    BotonJuego(String texto, Tono tono) {
        super(texto);
        this.tono = tono;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Sin un tamaño fijo, el botón mide lo que su texto más los bordes redondos. */
    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) return super.getPreferredSize();
        FontMetrics m = getFontMetrics(Estilo.titulo(tamanioLetra(ALTO_NORMAL)));
        return new Dimension(m.stringWidth(getText()) + ALTO_NORMAL + 16, ALTO_NORMAL + SOMBRA);
    }

    /** Evita que un layout con poco espacio aplaste la píldora. */
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /** Solo la píldora responde al mouse, no las esquinas. */
    @Override
    public boolean contains(int x, int y) {
        Rectangle z = zona();
        return pildora(z.x, z.y, z.width, z.height - SOMBRA, 0).contains(x, y);
    }

    /** Parte del botón donde va la píldora (con su sombra). Casilla la cambia para dejar texto debajo. */
    protected Rectangle zona() {
        return new Rectangle(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle z = zona();
        ButtonModel modelo = getModel();
        boolean presionado = modelo.isArmed() && modelo.isPressed();
        boolean encima = modelo.isRollover() && isEnabled();
        Tono t = isEnabled() ? tono : Tono.GRIS;
        double alto = z.height - SOMBRA;
        double y = z.y + (presionado ? 2 : 0);

        g2.setColor(new Color(0, 0, 0, presionado ? 55 : 95));
        g2.fill(pildora(z.x, z.y + SOMBRA, z.width, alto, 0));

        double aro = Math.max(2, alto * 0.07);
        double borde = Math.max(2, alto * 0.075);
        g2.setColor(t.aro);
        g2.fill(pildora(z.x, y, z.width, alto, 0));
        g2.setColor(Estilo.CONTORNO);
        g2.fill(pildora(z.x, y, z.width, alto, aro));

        Shape cuerpo = pildora(z.x, y, z.width, alto, aro + borde);
        Rectangle2D c = cuerpo.getBounds2D();
        Color arriba = encima ? aclarar(t.arriba) : t.arriba;
        Color abajo = encima ? aclarar(t.abajo) : (presionado ? t.abajo.darker() : t.abajo);
        g2.setPaint(new GradientPaint(0, (float) c.getY(), arriba, 0, (float) c.getMaxY(), abajo));
        g2.fill(cuerpo);

        // brillo de la mitad de arriba
        Shape recorteAntes = g2.getClip();
        g2.clip(cuerpo);
        double altoBrillo = c.getHeight() * 0.44;
        Shape brillo = new RoundRectangle2D.Double(c.getX() + c.getHeight() * 0.28, c.getY() + c.getHeight() * 0.07,
            c.getWidth() - c.getHeight() * 0.56, altoBrillo, altoBrillo, altoBrillo);
        g2.setPaint(new GradientPaint(0, (float) c.getY(), new Color(255, 255, 255, 150), 0,
            (float) (c.getY() + altoBrillo), new Color(255, 255, 255, 20)));
        g2.fill(brillo);
        g2.setClip(recorteAntes);

        // texto blanco con contorno, centrado por la altura de las mayúsculas
        float tamanio = tamanioLetra(alto);
        Font letra = Estilo.titulo(tamanio);
        double disponible = c.getWidth() - c.getHeight() * 0.5;
        while (tamanio > 9 && getFontMetrics(letra).stringWidth(getText()) > disponible) {
            tamanio -= 1;
            letra = Estilo.titulo(tamanio);
        }
        double mayusculas = letra.createGlyphVector(g2.getFontRenderContext(), "H").getVisualBounds().getHeight();
        double base = c.getCenterY() + mayusculas / 2;
        Estilo.textoContorno(g2, getText(), letra, c.getCenterX(), base, isEnabled() ? Color.WHITE : new Color(238, 238, 238),
            Math.max(2.5f, tamanio * 0.2f));
        g2.dispose();
    }

    /** El texto ocupa más o menos la mitad de la altura de la píldora. */
    private static float tamanioLetra(double altoPildora) {
        return (float) (altoPildora * 0.5);
    }

    private static Shape pildora(double x, double y, double ancho, double alto, double margen) {
        double a = alto - 2 * margen - 2;
        return new RoundRectangle2D.Double(x + margen + 1, y + margen + 1, ancho - 2 * margen - 2, a, a, a);
    }

    private static Color aclarar(Color c) {
        return new Color(c.getRed() + (255 - c.getRed()) / 5, c.getGreen() + (255 - c.getGreen()) / 5,
            c.getBlue() + (255 - c.getBlue()) / 5);
    }
}
