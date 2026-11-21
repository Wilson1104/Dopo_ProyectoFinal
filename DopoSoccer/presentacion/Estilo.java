package presentacion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import dominio.Personaje;
import dominio.RegistroDeErrores;
import dominio.TiroDePoder;

/** Colores, letras, imágenes y piezas visuales que comparten las pantallas. */
final class Estilo {

    static final Color FONDO_ARRIBA = new Color(16, 32, 64);
    static final Color FONDO_ABAJO = new Color(20, 92, 60);
    static final Color ACENTO = new Color(255, 196, 0);
    static final Color TEXTO = Color.WHITE;
    static final Color TEXTO_SUAVE = new Color(205, 215, 230);
    static final Color CONTORNO = new Color(15, 20, 45);

    private static final Map<String, BufferedImage> IMAGENES = new HashMap<>();

    /** Impact viene con Windows; si el equipo no la tiene, los títulos usan SansSerif negrita. */
    private static final boolean HAY_IMPACT = Arrays.asList(
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains("Impact");
    /** Impact no trae versión itálica, así que la letra se inclina con esta transformación. */
    private static final AffineTransform INCLINACION = AffineTransform.getShearInstance(-0.2, 0);

    private Estilo() {
    }

    /** Letra para textos pequeños y campos donde lo importante es que se lea fácil. */
    static Font letra(int estilo, float tamanio) {
        return new Font("SansSerif", estilo, Math.round(tamanio));
    }

    /** Letra del juego, parecida a la de Head Soccer: Impact itálica. Va en títulos, botones, nombres y marcador. */
    static Font titulo(float tamanio) {
        Font base = HAY_IMPACT ? new Font("Impact", Font.PLAIN, Math.round(tamanio))
            : new Font("SansSerif", Font.BOLD, Math.round(tamanio));
        return base.deriveFont(INCLINACION);
    }

    static JLabel etiqueta(String texto, int estilo, float tamanio) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(letra(estilo, tamanio));
        etiqueta.setForeground(TEXTO);
        return etiqueta;
    }

    /** Etiqueta con la letra del juego. El espacio a la derecha evita que se corte la última letra inclinada. */
    static JLabel etiquetaTitulo(String texto, float tamanio) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(titulo(tamanio));
        etiqueta.setForeground(TEXTO);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Math.round(tamanio * 0.2f)));
        return etiqueta;
    }

    /** Botón pequeño de los menús (Volver, tipo de partido, resolución). */
    static JButton boton(String texto) {
        return new BotonJuego(texto, BotonJuego.Tono.AZUL);
    }

    static void pintarFondo(Graphics g, Component c) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(new GradientPaint(0, 0, FONDO_ARRIBA, 0, c.getHeight(), FONDO_ABAJO));
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
    }

    /** Imagen de fondo de la pantalla de preparación. */
    static void pintarFondoImagen(Graphics g, Component c) {
        pintarCubriendo(g, c, imagen("fondo.jpg"), 0.5, false);
    }

    /**
     * Dibuja la imagen cubriendo todo el componente y recorta lo que sobre. {@code alineacionVertical} va de 0
     * (se ve la parte de arriba) a 1 (se ve la de abajo). Con {@code pixelado} no se suaviza, para el pixel art.
     * Si la imagen no existe, usa el degradado.
     */
    static void pintarCubriendo(Graphics g, Component c, BufferedImage imagen, double alineacionVertical, boolean pixelado) {
        if (imagen == null) {
            pintarFondo(g, c);
            return;
        }
        double escala = Math.max(c.getWidth() / (double) imagen.getWidth(), c.getHeight() / (double) imagen.getHeight());
        int ancho = (int) Math.ceil(imagen.getWidth() * escala);
        int alto = (int) Math.ceil(imagen.getHeight() * escala);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, pixelado
            ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int y = (int) Math.round((c.getHeight() - alto) * alineacionVertical);
        g2.drawImage(imagen, (c.getWidth() - ancho) / 2, y, ancho, alto, null);
    }

    /** Lee una imagen de la carpeta recursos una sola vez; devuelve null si no existe. */
    static BufferedImage imagen(String nombre) {
        if (IMAGENES.containsKey(nombre)) return IMAGENES.get(nombre);
        BufferedImage leida = null;
        for (File carpeta : carpetasDeRecursos()) {
            File archivo = new File(carpeta, nombre);
            if (!archivo.isFile()) continue;
            try {
                leida = ImageIO.read(archivo);
                break;
            } catch (IOException e) {
                RegistroDeErrores.registrar(e);
            }
        }
        IMAGENES.put(nombre, leida);
        return leida;
    }

    /**
     * Carpetas donde puede estar "recursos": junto a las clases compiladas (en BlueJ es la carpeta del proyecto;
     * en VS Code las clases quedan en bin, así que es la carpeta de arriba) y en la carpeta desde donde se ejecuta.
     */
    private static List<File> carpetasDeRecursos() {
        List<File> carpetas = new ArrayList<>();
        try {
            File clases = new File(Estilo.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            carpetas.add(new File(clases, "recursos"));
            carpetas.add(new File(clases.getParentFile(), "recursos"));
        } catch (Exception e) {
            // no se sabe dónde están las clases: se buscan solo las carpetas relativas
        }
        carpetas.add(new File("recursos"));
        carpetas.add(new File("DOPOSoccer", "recursos"));
        return carpetas;
    }

    /** Bandera del país del personaje (recursos/banderas). */
    static BufferedImage bandera(String pais) {
        if (Personaje.COREA_DEL_SUR.equals(pais)) return imagen("banderas/corea_del_sur.png");
        if (Personaje.CAMERUN.equals(pais)) return imagen("banderas/camerun.png");
        if (Personaje.NIGERIA.equals(pais)) return imagen("banderas/nigeria.png");
        if (Personaje.ESTADOS_UNIDOS.equals(pais)) return imagen("banderas/estados_unidos.png");
        return null;
    }

    /** Texto centrado en {@code cx} con contorno oscuro y una sombra leve, para que se lea sobre cualquier fondo. */
    static void textoContorno(Graphics2D g2, String texto, Font fuente, double cx, double y, Paint relleno, float grosor) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GlyphVector glifos = fuente.createGlyphVector(g2.getFontRenderContext(), texto);
        double x = cx - glifos.getLogicalBounds().getWidth() / 2;
        Shape forma = glifos.getOutline((float) x, (float) y);
        BasicStroke trazo = new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        AffineTransform antes = g2.getTransform();
        g2.translate(0, grosor * 0.6);
        g2.setColor(new Color(0, 0, 0, 120));
        g2.setStroke(trazo);
        g2.draw(forma);
        g2.setTransform(antes);
        g2.setColor(CONTORNO);
        g2.draw(forma);
        g2.setPaint(relleno);
        g2.fill(forma);
    }

    /** Color del cuerpo del personaje de cada país. */
    static Color colorPrincipal(String pais) {
        if (Personaje.COREA_DEL_SUR.equals(pais)) return new Color(215, 50, 60);
        if (Personaje.CAMERUN.equals(pais)) return new Color(245, 190, 20);
        if (Personaje.NIGERIA.equals(pais)) return new Color(40, 165, 75);
        if (Personaje.ESTADOS_UNIDOS.equals(pais)) return new Color(35, 80, 190);
        return Color.GRAY;
    }

    /** Color de la franja del cuerpo. */
    static Color colorSecundario(String pais) {
        if (Personaje.COREA_DEL_SUR.equals(pais)) return new Color(0, 71, 160);
        if (Personaje.CAMERUN.equals(pais)) return new Color(0, 130, 70);
        if (Personaje.NIGERIA.equals(pais)) return Color.WHITE;
        if (Personaje.ESTADOS_UNIDOS.equals(pais)) return new Color(200, 30, 60);
        return Color.DARK_GRAY;
    }

    /** Color del aura y del balón cargado con cada tiro de poder. */
    static Color colorTiro(TiroDePoder tiro) {
        if (tiro == null) return Color.WHITE;
        switch (tiro.getNombre()) {
            case "Aura Azul":
                return new Color(60, 150, 255);
            case "Rayo":
                return new Color(255, 225, 40);
            case "Tornado":
                return new Color(215, 215, 225);
            default:
                return new Color(235, 70, 200);
        }
    }
}
