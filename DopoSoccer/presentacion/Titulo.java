package presentacion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * Texto grande con la letra del juego, contorno oscuro y relleno de un color o en degradado de arriba hacia
 * abajo (como el logo de Head Soccer). Se usa en los títulos de las pantallas.
 */
class Titulo extends JComponent {

    private static final long serialVersionUID = 1L;

    private final String texto;
    private final Font letra;
    private final Color arriba;
    private final Color abajo;
    private final float grosor;

    /** Título de un solo color. */
    Titulo(String texto, float tamanio, Color color) {
        this(texto, tamanio, color, color);
    }

    /** Título con el relleno en degradado: {@code arriba} en la parte de arriba de las letras y {@code abajo} en la base. */
    Titulo(String texto, float tamanio, Color arriba, Color abajo) {
        this.texto = texto;
        this.letra = Estilo.titulo(tamanio);
        this.arriba = arriba;
        this.abajo = abajo;
        this.grosor = Math.max(4f, tamanio * 0.12f);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics m = getFontMetrics(letra);
        int margen = Math.round(grosor) + 2;
        // la inclinación hace que la última letra sobresalga un poco a la derecha
        int inclinacion = Math.round(letra.getSize2D() * 0.2f);
        return new Dimension(m.stringWidth(texto) + inclinacion + 2 * margen, m.getAscent() + m.getDescent() + 2 * margen);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Rectangle2D letras = letra.createGlyphVector(g2.getFontRenderContext(), "H").getVisualBounds();
        double base = getHeight() / 2.0 + letras.getHeight() / 2;
        GradientPaint relleno = new GradientPaint(0, (float) (base - letras.getHeight()), arriba, 0, (float) base, abajo);
        Estilo.textoContorno(g2, texto, letra, getWidth() / 2.0, base, relleno, grosor);
        g2.dispose();
    }
}
