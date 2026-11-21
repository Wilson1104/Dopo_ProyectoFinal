package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TiroDePoderTest {

    @Test
    public void cadaPaisDeberiaTenerSuTiroDelAnexo() throws ExcepcionDOPOSoccer {
        assertTiro(Personaje.COREA_DEL_SUR, AuraAzul.class, 40);
        assertTiro(Personaje.CAMERUN, Rayo.class, 60);
        assertTiro(Personaje.NIGERIA, Tornado.class, 40);
        assertTiro(Personaje.ESTADOS_UNIDOS, Ilusion.class, 30);
        assertEquals(4, Personaje.paisesDisponibles().length);
    }

    @Test
    public void noDeberiaCrearUnPaisDesconocido() {
        ExcepcionDOPOSoccer e = assertThrows(ExcepcionDOPOSoccer.class, () -> Personaje.crear("Atlantis"));
        assertEquals(ExcepcionDOPOSoccer.PAIS_DESCONOCIDO, e.getMessage());
    }

    /** Ejemplo del Anexo 2: Camerún con Kick 5 contra un rival con Survival 3. */
    @Test
    public void deberiaReproducirElEjemploDelEnunciado() throws ExcepcionDOPOSoccer {
        Jugador jugador1 = new JugadorHumano(Lado.IZQUIERDO, Personaje.crear(Personaje.CAMERUN), cuentaCon(TipoEstadistica.PATADA, 5));
        Jugador jugador2 = new JugadorHumano(Lado.DERECHO, Personaje.crear(Personaje.NIGERIA), cuentaCon(TipoEstadistica.SUPERVIVENCIA, 3));
        assertEquals(120, jugador2.getVida(), 1e-9);
        assertEquals(1.4, jugador1.multiplicadorPatada(), 1e-9);

        jugador2.recibirDanio(jugador1.danioPatada());
        assertEquals(106, jugador2.getVida(), 1e-9);

        jugador2.restaurarVida();
        jugador1.getPersonaje().getTiroDePoder().golpear(jugador2, jugador1);
        assertEquals(36, jugador2.getVida(), 1e-9);
        assertTrue(jugador2.getEfecto() instanceof Electrocutado);
        assertEquals(3, jugador2.getEfecto().getDuracion(), 1e-9);
    }

    @Test
    public void siElTiroDejaLaVidaEnCeroDeberiaQuedarAturdido() throws ExcepcionDOPOSoccer {
        Jugador lanzador = new JugadorHumano(Lado.IZQUIERDO, Personaje.crear(Personaje.CAMERUN), new Cuenta("Ana"));
        Jugador rival = new JugadorHumano(Lado.DERECHO, Personaje.crear(Personaje.NIGERIA), new Cuenta("Luis"));
        rival.recibirDanio(50);
        lanzador.getPersonaje().getTiroDePoder().golpear(rival, lanzador);
        assertEquals(0, rival.getVida(), 1e-9);
        assertTrue(rival.estaAturdido());
    }

    @Test
    public void elTiroDeberiaGolpearAlRivalYNoAQuienLoLanzo() throws ExcepcionDOPOSoccer {
        Jugador lanzador = new JugadorHumano(Lado.IZQUIERDO, Personaje.crear(Personaje.COREA_DEL_SUR), new Cuenta("Ana"));
        Jugador rival = new JugadorHumano(Lado.DERECHO, Personaje.crear(Personaje.NIGERIA), new Cuenta("Luis"));
        Partido partido = new PartidoPorGoles(Resolucion.BAJA, lanzador, rival, 3);
        lanzador.getPersonaje().getTiroDePoder().lanzar(partido, lanzador);
        Balon balon = partido.getBalonReal();
        assertTrue(balon.tieneTiroCargado());
        assertTrue(balon.getVelocidadX() > 0);
        assertFalse(balon.puedeGolpear(lanzador));
        rival.ubicar(balon.getX(), balon.getY());
        assertTrue(balon.puedeGolpear(rival));
        balon.golpear(rival);
        assertFalse(balon.tieneTiroCargado());
        assertEquals(60, rival.getVida(), 1e-9);
        assertTrue(rival.getEfecto() instanceof Empuje);
    }

    @Test
    public void laIlusionDeberiaCrearSieteBalonesFalsosQueDesaparecen() throws ExcepcionDOPOSoccer {
        Jugador lanzador = new JugadorHumano(Lado.IZQUIERDO, Personaje.crear(Personaje.ESTADOS_UNIDOS), new Cuenta("Ana"));
        Jugador rival = new JugadorHumano(Lado.DERECHO, Personaje.crear(Personaje.NIGERIA), new Cuenta("Luis"));
        Partido partido = new PartidoPorGoles(Resolucion.BAJA, lanzador, rival, 3);
        lanzador.getPersonaje().getTiroDePoder().lanzar(partido, lanzador);
        assertEquals(8, partido.getBalones().size());
        for (int i = 0; i < 60; i++) partido.actualizar(0.05);
        assertEquals(1, partido.getBalones().size());
        assertTrue(partido.getBalones().get(0).esReal());
    }

    private void assertTiro(String pais, Class<?> clase, int danio) throws ExcepcionDOPOSoccer {
        TiroDePoder tiro = Personaje.crear(pais).getTiroDePoder();
        assertEquals(clase, tiro.getClass());
        assertEquals(danio, tiro.getDanioBase());
    }

    private Cuenta cuentaCon(TipoEstadistica tipo, int nivel) throws ExcepcionDOPOSoccer {
        Cuenta cuenta = new Cuenta("Prueba");
        while (cuenta.getEstadisticas().getNivel(tipo) < nivel) {
            cuenta.ganarPuntos(cuenta.getEstadisticas().precioMejora(tipo));
            cuenta.comprarMejora(tipo);
        }
        return cuenta;
    }
}
