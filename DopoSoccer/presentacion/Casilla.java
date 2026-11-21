package presentacion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Botón grande de los menús (Tienda, Personaje, Jugar): la píldora arriba y, debajo, hasta dos líneas con lo
 * que está elegido. Sin líneas, la píldora ocupa toda la casilla. La casilla destacada (Jugar) es más grande.
 */
class Casilla extends BotonJuego {

    private static final long serialVersionUID = 1L;
    /** Alto de la parte de arriba, donde van las píldoras cuando hay texto debajo. */
    private static final int ALTO_PILDORAS = 72;

    private final boolean destacada;
    private String linea1 = "";
    private String linea2 = "";

    Casilla(String titulo, Tono tono, boolean destacada) {
        super(titulo, tono);
        this.destacada = destacada;
        setPreferredSize(new Dimension(200, 122));
    }

    void detalle(String linea1, String linea2) {
        this.linea1 = linea1;
        this.linea2 = linea2;
        repaint();
    }

    private boolean tieneDetalle() {
        return !linea1.isEmpty() || !linea2.isEmpty();
    }

    @Override
    protected Rectangle zona() {
        if (!tieneDetalle()) return super.zona();
        int alto = destacada ? ALTO_PILDORAS : ALTO_PILDORAS - 14;
        int margen = destacada ? 0 : 12;
        return new Rectangle(margen, (ALTO_PILDORAS - alto) / 2, getWidth() - 2 * margen, alto);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!tieneDetalle()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        Font letra = Estilo.titulo(17);
        Estilo.textoContorno(g2, recortar(linea1, letra), letra, getWidth() / 2.0, ALTO_PILDORAS + 21, Color.WHITE, 4f);
        Estilo.textoContorno(g2, recortar(linea2, letra), letra, getWidth() / 2.0, ALTO_PILDORAS + 43, Color.WHITE, 4f);
        g2.dispose();
    }

    /** Corta con "…" el texto que no cabe (por ejemplo, nombres muy largos). */
    private String recortar(String texto, Font letra) {
        FontMetrics m = getFontMetrics(letra);
        String mostrado = texto;
        while (m.stringWidth(mostrado) > getWidth() - 12 && mostrado.length() > 4) {
            mostrado = mostrado.substring(0, mostrado.length() - 2).trim() + "…";
        }
        return mostrado;
    }
}
