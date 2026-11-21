package presentacion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import dominio.Arco;
import dominio.Aturdimiento;
import dominio.Balon;
import dominio.Cancha;
import dominio.Confusion;
import dominio.DOPOSoccer;
import dominio.Efecto;
import dominio.Electrocutado;
import dominio.Empuje;
import dominio.ExcepcionDOPOSoccer;
import dominio.Inconsciente;
import dominio.Jugador;
import dominio.Lado;
import dominio.Partido;
import dominio.Resolucion;
import dominio.TipoEstadistica;

/**
 * Pantalla del partido: lee el teclado, llama al dominio en cada cuadro y dibuja la cancha.
 * Se dibuja en las unidades de la cancha (800 x 450) y se escala a la resolución elegida.
 */
class PanelJuego extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int MILISEGUNDOS_POR_CUADRO = 16;
    private static final double ALTO_MARCADOR = 56;
    private static final Color PIEL = new Color(255, 214, 170);

    private final DOPOSoccerGUI ventana;
    private final DOPOSoccer soccer;
    private final boolean contraMaquina;
    private final String perfilMaquina;
    private final Timer reloj;
    private final Set<Integer> teclas = new HashSet<>();
    private final double[][] publico;
    private long ultimoInstante;
    private double tiempoAnimacion;
    private String aviso;
    private double tiempoAviso;
    private boolean resultadoMostrado;

    PanelJuego(DOPOSoccerGUI ventana, DOPOSoccer soccer, Resolucion resolucion, boolean contraMaquina,
               String perfilMaquina) {
        this.ventana = ventana;
        this.soccer = soccer;
        this.contraMaquina = contraMaquina;
        this.perfilMaquina = perfilMaquina;
        setPreferredSize(new Dimension(resolucion.getAncho(), resolucion.getAlto()));
        setBackground(Color.BLACK);
        setFocusable(true);
        reloj = new Timer(MILISEGUNDOS_POR_CUADRO, e -> cuadro());
        publico = generarPublico();
        configurarTeclas();
    }

    void iniciar() {
        ultimoInstante = System.nanoTime();
        reloj.start();
        requestFocusInWindow();
    }

    void detener() {
        reloj.stop();
    }

    void pausarSiJuega() {
        try {
            Partido partido = soccer.getPartido();
            if (partido != null && !partido.isTerminado()) soccer.pausar();
        } catch (ExcepcionDOPOSoccer e) {
            // no hay partido: nada que pausar
        }
    }

    // ------------------------------------------------------------------ ciclo del juego

    private void cuadro() {
        long ahora = System.nanoTime();
        double segundos = (ahora - ultimoInstante) / 1e9;
        ultimoInstante = ahora;
        tiempoAnimacion += segundos;
        tiempoAviso -= segundos;
        try {
            Partido partido = soccer.getPartido();
            if (!partido.isPausado()) aplicarControles();
            soccer.actualizar(segundos);
            repaint();
            if (partido.isTerminado() && !resultadoMostrado) {
                resultadoMostrado = true;
                reloj.stop();
                SwingUtilities.invokeLater(ventana::partidoTerminado);
            }
        } catch (Exception e) {
            reloj.stop();
            ventana.manejarError(e);
            ventana.mostrarConfiguracion();
        }
    }

    /** Moverse depende de qué teclas siguen presionadas; saltar, patear y el poder van en {@link #presionar}. */
    private void aplicarControles() throws ExcepcionDOPOSoccer {
        mover(1, KeyEvent.VK_A, KeyEvent.VK_D);
        if (!contraMaquina) mover(2, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
    }

    private void mover(int jugador, int izquierda, int derecha) throws ExcepcionDOPOSoccer {
        boolean izq = teclas.contains(izquierda);
        boolean der = teclas.contains(derecha);
        if (izq && !der) soccer.moverIzquierda(jugador);
        else if (der && !izq) soccer.moverDerecha(jugador);
        else soccer.detener(jugador);
    }

    private void configurarTeclas() {
        InputMap entradas = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap acciones = getActionMap();
        int[] codigos = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_Q, KeyEvent.VK_LEFT,
            KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER, KeyEvent.VK_P, KeyEvent.VK_ESCAPE};
        for (int codigo : codigos) {
            entradas.put(KeyStroke.getKeyStroke(codigo, 0, false), "presionar" + codigo);
            entradas.put(KeyStroke.getKeyStroke(codigo, 0, true), "soltar" + codigo);
            acciones.put("presionar" + codigo, accion(() -> presionar(codigo)));
            acciones.put("soltar" + codigo, accion(() -> teclas.remove(codigo)));
        }
    }

    private void presionar(int codigo) {
        if (!teclas.add(codigo)) return;
        try {
            Partido partido = soccer.getPartido();
            if (partido == null || partido.isTerminado()) return;
            if (partido.isPausado() && codigo != KeyEvent.VK_P && codigo != KeyEvent.VK_ESCAPE) return;
            switch (codigo) {
                case KeyEvent.VK_W:
                    soccer.saltar(1);
                    break;
                case KeyEvent.VK_S:
                    soccer.patear(1);
                    break;
                case KeyEvent.VK_Q:
                    poder(1);
                    break;
                case KeyEvent.VK_UP:
                    if (!contraMaquina) soccer.saltar(2);
                    break;
                case KeyEvent.VK_DOWN:
                    if (!contraMaquina) soccer.patear(2);
                    break;
                case KeyEvent.VK_ENTER:
                    if (!contraMaquina) poder(2);
                    break;
                case KeyEvent.VK_P:
                    if (partido.isPausado()) soccer.reanudar();
                    else soccer.pausar();
                    break;
                case KeyEvent.VK_ESCAPE:
                    confirmarTerminar(partido);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            ventana.manejarError(e);
        }
    }

    /** Si la barra no está llena el dominio avisa con una excepción; en pleno juego basta un aviso corto. */
    private void poder(int jugador) {
        try {
            soccer.activarPoder(jugador);
        } catch (ExcepcionDOPOSoccer e) {
            avisar(soccer.getPartido().getJugador(jugador).tienePoderActivo()
                ? "Ya tienes el aura activa: toca el balón para lanzar el tiro"
                : "Jugador " + jugador + ": la barra de poder aún no está llena");
        }
    }

    private void confirmarTerminar(Partido partido) throws ExcepcionDOPOSoccer {
        boolean estabaPausado = partido.isPausado();
        soccer.pausar();
        teclas.clear();
        int respuesta = JOptionPane.showConfirmDialog(ventana,
            "¿Terminar el partido ahora? Los puntos se reparten con el marcador actual.", "Terminar partido",
            JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) soccer.terminar();
        else if (!estabaPausado) soccer.reanudar();
        ultimoInstante = System.nanoTime();
    }

    private void avisar(String texto) {
        aviso = texto;
        tiempoAviso = 1.6;
    }

    private static AbstractAction accion(Runnable tarea) {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                tarea.run();
            }
        };
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Partido partido = soccer.getPartido();
        if (partido == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double escala = Math.min(getWidth() / Cancha.ANCHO, getHeight() / Cancha.ALTO);
        g2.translate((getWidth() - Cancha.ANCHO * escala) / 2, (getHeight() - Cancha.ALTO * escala) / 2);
        g2.scale(escala, escala);

        dibujarFondo(g2);
        dibujarRedes(g2, partido.getCancha());
        for (int i = 1; i <= 2; i++) dibujarJugador(g2, partido.getJugador(i), i);
        for (Balon balon : partido.getBalones()) dibujarBalon(g2, balon);
        dibujarPostes(g2, partido.getCancha());
        dibujarMarcador(g2, partido);
        dibujarAyuda(g2);
        if (tiempoAviso > 0 && aviso != null) textoCentrado(g2, aviso, Cancha.ANCHO / 2, 94, Estilo.titulo(20), Estilo.ACENTO);
        if (partido.isPausado() && !partido.isTerminado()) dibujarPausa(g2);
        g2.dispose();
    }

    private void dibujarFondo(Graphics2D g2) {
        g2.setPaint(new GradientPaint(0, 0, new Color(110, 175, 235), 0, 250, new Color(190, 225, 250)));
        g2.fill(new java.awt.geom.Rectangle2D.Double(0, 0, Cancha.ANCHO, 250));
        g2.setColor(new Color(70, 75, 90));
        g2.fill(new java.awt.geom.Rectangle2D.Double(0, 70, Cancha.ANCHO, 165));
        for (double[] p : publico) {
            g2.setColor(new Color((int) p[2], (int) p[3], (int) p[4]));
            g2.fill(new Ellipse2D.Double(p[0], p[1], 7, 7));
            g2.fill(new RoundRectangle2D.Double(p[0] - 1, p[1] + 6, 9, 6, 3, 3));
        }
        g2.setColor(new Color(20, 40, 90));
        g2.fill(new java.awt.geom.Rectangle2D.Double(0, 232, Cancha.ANCHO, 18));
        g2.setFont(Estilo.titulo(14));
        g2.setColor(Estilo.ACENTO);
        for (int x = 20; x < Cancha.ANCHO; x += 200) g2.drawString("DOPOSoccer 2026-2", x, 246);

        double alto = Cancha.ALTO - 250;
        for (int i = 0; i < 10; i++) {
            g2.setColor(i % 2 == 0 ? new Color(52, 150, 70) : new Color(60, 165, 78));
            g2.fill(new java.awt.geom.Rectangle2D.Double(i * Cancha.ANCHO / 10, 250, Cancha.ANCHO / 10 + 1, alto));
        }
        g2.setColor(new Color(255, 255, 255, 170));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new java.awt.geom.Line2D.Double(Cancha.ANCHO / 2, 255, Cancha.ANCHO / 2, Cancha.ALTO));
        g2.draw(new Ellipse2D.Double(Cancha.ANCHO / 2 - 90, Cancha.SUELO - 18, 180, 36));
        g2.draw(new java.awt.geom.Line2D.Double(0, 255, Cancha.ANCHO, 255));
    }

    private void dibujarRedes(Graphics2D g2, Cancha cancha) {
        for (Arco arco : new Arco[] {cancha.getArcoIzquierdo(), cancha.getArcoDerecho()}) {
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fill(new java.awt.geom.Rectangle2D.Double(arco.getX(), arco.getY(), arco.getAncho(), arco.getAlto()));
            g2.setColor(new Color(255, 255, 255, 140));
            g2.setStroke(new BasicStroke(1f));
            for (double x = arco.getX(); x <= arco.getX() + arco.getAncho(); x += 8) {
                g2.draw(new java.awt.geom.Line2D.Double(x, arco.getY(), x, arco.getY() + arco.getAlto()));
            }
            for (double y = arco.getY(); y <= arco.getY() + arco.getAlto(); y += 8) {
                g2.draw(new java.awt.geom.Line2D.Double(arco.getX(), y, arco.getX() + arco.getAncho(), y));
            }
        }
    }

    private void dibujarPostes(Graphics2D g2, Cancha cancha) {
        g2.setColor(Color.WHITE);
        for (Arco arco : new Arco[] {cancha.getArcoIzquierdo(), cancha.getArcoDerecho()}) {
            g2.fill(new java.awt.geom.Rectangle2D.Double(arco.getX(), arco.getY() - Arco.GROSOR_TRAVESANO, arco.getAncho(),
                Arco.GROSOR_TRAVESANO));
            double poste = arco.getLado() == Lado.IZQUIERDO ? arco.getX() + arco.getAncho() - 4 : arco.getX();
            g2.fill(new java.awt.geom.Rectangle2D.Double(poste, arco.getY() - Arco.GROSOR_TRAVESANO, 4,
                arco.getAlto() + Arco.GROSOR_TRAVESANO));
        }
    }

    /** Cada jugador es un rectángulo pequeño del color de su país, con una franja y su etiqueta (J1 / J2 / CPU). */
    private void dibujarJugador(Graphics2D g2, Jugador jugador, int numero) {
        double x = jugador.getX();
        double y = jugador.getY();
        double w = jugador.getAncho();
        double h = jugador.getAlto();
        String pais = jugador.getPersonaje().getPais();
        Partido partido = soccer.getPartido();
        Color principal = Estilo.colorPrincipal(pais);
        boolean mismoPais = pais.equals(partido.getJugador(numero == 1 ? 2 : 1).getPersonaje().getPais());
        if (numero == 2 && mismoPais) principal = principal.darker();
        Color secundario = Estilo.colorSecundario(pais);
        int mira = -jugador.haciaSuArco();

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new Ellipse2D.Double(x - 2, Cancha.SUELO - 5, w + 4, 9));

        if (jugador.tienePoderActivo()) {
            Color aura = Estilo.colorTiro(jugador.getPersonaje().getTiroDePoder());
            double pulso = 7 + 3 * Math.sin(tiempoAnimacion * 9);
            RoundRectangle2D.Double marco = new RoundRectangle2D.Double(x - pulso, y - pulso, w + 2 * pulso, h + 2 * pulso, 14, 14);
            g2.setColor(new Color(aura.getRed(), aura.getGreen(), aura.getBlue(), 80));
            g2.fill(marco);
            g2.setColor(aura);
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(marco);
        }

        if (jugador.estaPateando()) {
            // la pierna sale a ras del suelo, por debajo del cuerpo: un pie plano y corto
            double px = mira > 0 ? x + w - 2 : x - 12;
            g2.setColor(new Color(30, 30, 30));
            g2.fill(new java.awt.geom.Rectangle2D.Double(px, y + h - 6, 14, 6));
        }

        g2.setColor(principal);
        g2.fill(new java.awt.geom.Rectangle2D.Double(x, y, w, h));
        g2.setColor(secundario);
        g2.fill(new java.awt.geom.Rectangle2D.Double(x, y + h * 0.38, w, 8));
        g2.setColor(new Color(20, 20, 20));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new java.awt.geom.Rectangle2D.Double(x, y, w, h));

        Color etiqueta = numero == 1 ? new Color(255, 230, 90) : new Color(120, 220, 255);
        textoCentrado(g2, contraMaquina && numero == 2 ? "CPU" : "J" + numero, x + w / 2, y - 6, Estilo.titulo(14), etiqueta);
        dibujarEfecto(g2, jugador, x, y, w);
    }
    private void dibujarEfecto(Graphics2D g2, Jugador jugador, double x, double y, double w) {
        Efecto efecto = jugador.getEfecto();
        if (efecto == null) return;
        double cx = x + w / 2;
        if (efecto instanceof Aturdimiento) {
            g2.setColor(new Color(255, 230, 60));
            for (int i = 0; i < 3; i++) {
                double angulo = tiempoAnimacion * 4 + i * 2 * Math.PI / 3;
                estrella(g2, cx + Math.cos(angulo) * 22, y - 2 + Math.sin(angulo) * 6, 5);
            }
        } else if (efecto instanceof Electrocutado) {
            g2.setColor(new Color(255, 240, 80));
            g2.setStroke(new BasicStroke(2f));
            if ((int) (tiempoAnimacion * 12) % 2 == 0) {
                Path2D rayo = new Path2D.Double();
                rayo.moveTo(x - 6, y + 10);
                rayo.lineTo(x + 4, y + 22);
                rayo.lineTo(x - 4, y + 28);
                rayo.lineTo(x + 6, y + 42);
                g2.draw(rayo);
                g2.translate(w + 2, 0);
                g2.draw(rayo);
                g2.translate(-(w + 2), 0);
            }
        } else if (efecto instanceof Inconsciente) {
            textoCentrado(g2, "z Z z", cx, y - 20, 14, Color.WHITE);
        } else if (efecto instanceof Empuje) {
            g2.setColor(new Color(60, 150, 255, 180));
            g2.setStroke(new BasicStroke(3f));
            double atras = jugador.haciaSuArco() < 0 ? x + w + 6 : x - 26;
            for (int i = 0; i < 3; i++) g2.draw(new java.awt.geom.Line2D.Double(atras, y + 12 + i * 12, atras + 20, y + 12 + i * 12));
        } else if (efecto instanceof Confusion) {
            textoCentrado(g2, "? ? ?", cx, y - 20, 15, new Color(235, 70, 200));
        }
        textoCentrado(g2, efecto.getNombre() + String.format(" %.1fs", efecto.getTiempoRestante()), cx, y - 34, 10,
            Color.WHITE);
    }

    private void dibujarBalon(Graphics2D g2, Balon balon) {
        double x = balon.getX();
        double y = balon.getY();
        double d = balon.getAncho();
        double sombra = Math.max(0.3, 1 - (Cancha.SUELO - y - d) / 300);
        g2.setColor(new Color(0, 0, 0, (int) (60 * sombra)));
        g2.fill(new Ellipse2D.Double(x + d * (1 - sombra) / 2, Cancha.SUELO - 4, d * sombra, 7));
        if (balon.tieneTiroCargado()) {
            Color color = Estilo.colorTiro(balon.getTiroCargado());
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 70));
            for (int i = 1; i <= 4; i++) {
                double atras = i * 0.018;
                g2.fill(new Ellipse2D.Double(x - balon.getVelocidadX() * atras, y - balon.getVelocidadY() * atras, d, d));
            }
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
            g2.fill(new Ellipse2D.Double(x - 6, y - 6, d + 12, d + 12));
        }
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(x, y, d, d));
        java.awt.geom.AffineTransform antes = g2.getTransform();
        g2.rotate(x / 10, x + d / 2, y + d / 2);
        g2.setColor(new Color(35, 35, 35));
        g2.fill(new Ellipse2D.Double(x + d / 2 - 4, y + d / 2 - 4, 8, 8));
        for (int i = 0; i < 5; i++) {
            double a = i * 2 * Math.PI / 5;
            g2.fill(new Ellipse2D.Double(x + d / 2 + Math.cos(a) * 9 - 2.5, y + d / 2 + Math.sin(a) * 9 - 2.5, 5, 5));
        }
        g2.setTransform(antes);
        g2.setColor(new Color(0, 0, 0, 150));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new Ellipse2D.Double(x, y, d, d));
    }

    private void dibujarMarcador(Graphics2D g2, Partido partido) {
        g2.setColor(new Color(10, 20, 45, 215));
        g2.fill(new java.awt.geom.Rectangle2D.Double(0, 0, Cancha.ANCHO, ALTO_MARCADOR));
        dibujarDatosJugador(g2, partido.getJugador(1), 1, 10, true);
        dibujarDatosJugador(g2, partido.getJugador(2), 2, Cancha.ANCHO - 10, false);

        g2.setColor(new Color(255, 255, 255, 30));
        g2.fill(new RoundRectangle2D.Double(Cancha.ANCHO / 2 - 62, 4, 124, 48, 12, 12));
        String marcador = partido.getJugador(1).getGoles() + "  -  " + partido.getJugador(2).getGoles();
        textoCentrado(g2, marcador, Cancha.ANCHO / 2, 32, Estilo.titulo(29), Color.WHITE);
        textoCentrado(g2, partido.getDescripcionLimite(), Cancha.ANCHO / 2, 47, 11, Estilo.ACENTO);
    }

    /** Nombre, país, puntos, vida, barra de poder, aura y estadísticas de un jugador. */
    private void dibujarDatosJugador(Graphics2D g2, Jugador jugador, int numero, double borde, boolean izquierda) {
        double ancho = 250;
        double x = izquierda ? borde : borde - ancho;
        String nombre = contraMaquina && numero == 2 ? "Máquina " + perfilMaquina : jugador.getCuenta().getNombre();
        String puntos = contraMaquina && numero == 2 ? "" : "  ·  " + jugador.getCuenta().getPuntos() + " pts";
        g2.setFont(Estilo.titulo(14));
        g2.setColor(Color.WHITE);
        texto(g2, nombre + "  (" + jugador.getPersonaje().getPais() + ")" + puntos, x, 15, ancho, izquierda);

        double vida = jugador.getVida() / jugador.getVidaMaxima();
        barra(g2, x, 19, ancho, 9, vida, vida > 0.3 ? new Color(70, 200, 90) : new Color(230, 60, 50));
        g2.setFont(Estilo.letra(Font.BOLD, 8));
        g2.setColor(Color.WHITE);
        texto(g2, String.format("VIDA %.0f / %.0f", jugador.getVida(), jugador.getVidaMaxima()), x + 4, 27, ancho - 8, izquierda);

        Color colorPoder = Estilo.colorTiro(jugador.getPersonaje().getTiroDePoder());
        barra(g2, x, 31, ancho, 7, jugador.getCargaBarra(), colorPoder);
        String tecla = numero == 1 ? "Q" : "Enter";
        String estado;
        if (jugador.tienePoderActivo()) estado = "AURA ACTIVA: toca el balón (" + jugador.getPersonaje().getTiroDePoder().getNombre() + ")";
        else if (jugador.getCargaBarra() >= 1) estado = "¡PODER LISTO!" + (contraMaquina && numero == 2 ? "" : " (" + tecla + ")");
        else estado = "Poder " + Math.round(jugador.getCargaBarra() * 100) + " %";
        g2.setFont(Estilo.letra(Font.BOLD, 9));
        g2.setColor(jugador.tienePoderActivo() || jugador.getCargaBarra() >= 1 ? Estilo.ACENTO : Estilo.TEXTO_SUAVE);
        texto(g2, estado, x, 48, ancho, izquierda);

        g2.setFont(Estilo.letra(Font.PLAIN, 9));
        g2.setColor(Estilo.TEXTO_SUAVE);
        texto(g2, "V" + nivel(jugador, TipoEstadistica.VELOCIDAD) + " S" + nivel(jugador, TipoEstadistica.SALTO)
            + " P" + nivel(jugador, TipoEstadistica.PATADA) + " R" + nivel(jugador, TipoEstadistica.SUPERVIVENCIA),
            x, 48, ancho, !izquierda);
    }

    private static int nivel(Jugador jugador, TipoEstadistica tipo) {
        return jugador.getCuenta().getEstadisticas().getNivel(tipo);
    }

    private void dibujarAyuda(Graphics2D g2) {
        String j2 = contraMaquina ? "J2: máquina " + perfilMaquina.toLowerCase() : "J2: ← → mover · ↑ saltar · ↓ patear · Enter poder";
        String ayuda = "J1: A D mover · W saltar · S patear · Q poder     " + j2 + "     P pausa · Esc terminar";
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fill(new java.awt.geom.Rectangle2D.Double(0, Cancha.ALTO - 16, Cancha.ANCHO, 16));
        textoCentrado(g2, ayuda, Cancha.ANCHO / 2, Cancha.ALTO - 4, 9.5f, Color.WHITE);
    }

    private void dibujarPausa(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fill(new java.awt.geom.Rectangle2D.Double(0, ALTO_MARCADOR, Cancha.ANCHO, Cancha.ALTO - ALTO_MARCADOR));
        textoCentrado(g2, "PAUSA", Cancha.ANCHO / 2, 222, Estilo.titulo(60), Estilo.ACENTO);
        textoCentrado(g2, "P para continuar  ·  Esc para terminar el partido", Cancha.ANCHO / 2, 255, 15, Color.WHITE);
    }

    // ------------------------------------------------------------------ utilidades de dibujo

    private static void barra(Graphics2D g2, double x, double y, double ancho, double alto, double fraccion, Color color) {
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fill(new RoundRectangle2D.Double(x, y, ancho, alto, alto, alto));
        g2.setColor(color);
        g2.fill(new RoundRectangle2D.Double(x, y, ancho * Math.max(0, Math.min(1, fraccion)), alto, alto, alto));
    }

    private static void texto(Graphics2D g2, String texto, double x, double y, double ancho, boolean alinearIzquierda) {
        FontMetrics m = g2.getFontMetrics();
        double tx = alinearIzquierda ? x : x + ancho - m.stringWidth(texto);
        g2.drawString(texto, (float) tx, (float) y);
    }

    /** Texto pequeño centrado con una sombra leve. */
    private static void textoCentrado(Graphics2D g2, String texto, double cx, double y, float tamanio, Color color) {
        g2.setFont(Estilo.letra(Font.BOLD, tamanio));
        FontMetrics m = g2.getFontMetrics();
        float tx = (float) (cx - m.stringWidth(texto) / 2.0);
        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(texto, tx + 1, (float) y + 1);
        g2.setColor(color);
        g2.drawString(texto, tx, (float) y);
    }

    /** Texto grande con la letra del juego y contorno oscuro, como en los botones. */
    private static void textoCentrado(Graphics2D g2, String texto, double cx, double y, Font letra, Color color) {
        Estilo.textoContorno(g2, texto, letra, cx, y, color, Math.max(2.5f, letra.getSize2D() * 0.14f));
    }

    private static void estrella(Graphics2D g2, double cx, double cy, double r) {
        Path2D estrella = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double radio = i % 2 == 0 ? r : r / 2.2;
            double a = -Math.PI / 2 + i * Math.PI / 5;
            if (i == 0) estrella.moveTo(cx + Math.cos(a) * radio, cy + Math.sin(a) * radio);
            else estrella.lineTo(cx + Math.cos(a) * radio, cy + Math.sin(a) * radio);
        }
        estrella.closePath();
        g2.fill(estrella);
    }

    /** Público de la tribuna: posiciones y colores fijos para que no parpadee. */
    private static double[][] generarPublico() {
        Random azar = new Random(2026);
        Color[] camisetas = {new Color(220, 60, 60), new Color(60, 120, 220), new Color(250, 210, 60),
            new Color(60, 180, 90), Color.WHITE, new Color(240, 140, 40)};
        java.util.List<double[]> personas = new java.util.ArrayList<>();
        for (double fila = 80; fila < 225; fila += 15) {
            for (double x = 4; x < Cancha.ANCHO; x += 11) {
                Color c = camisetas[azar.nextInt(camisetas.length)];
                personas.add(new double[] {x + azar.nextDouble() * 3, fila + azar.nextDouble() * 2, c.getRed(), c.getGreen(), c.getBlue()});
            }
        }
        return personas.toArray(new double[0][]);
    }
}
