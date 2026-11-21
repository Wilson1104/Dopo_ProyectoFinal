package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DOPOSoccerTest {

    private DOPOSoccer soccer;

    @BeforeEach
    public void setUp() {
        soccer = new DOPOSoccer();
    }

    @Test
    public void deberiaTenerDosRanurasVacias() throws ExcepcionDOPOSoccer {
        assertTrue(soccer.getRanura(1).estaVacia());
        assertTrue(soccer.getRanura(2).estaVacia());
        assertThrows(ExcepcionDOPOSoccer.class, () -> soccer.getRanura(3));
    }

    @Test
    public void noDeberiaCrearPartidoSinJuego() {
        ExcepcionDOPOSoccer e = assertThrows(ExcepcionDOPOSoccer.class,
            () -> soccer.nuevoPartido(Resolucion.BAJA, Personaje.CAMERUN, Personaje.NIGERIA, null, true, 2));
        assertEquals(ExcepcionDOPOSoccer.SIN_JUEGO, e.getMessage());
    }

    @Test
    public void noDeberiaCrearPartidoConLimiteInvalido() throws ExcepcionDOPOSoccer {
        soccer.nuevoJuego(1, "Ana", "Luis");
        ExcepcionDOPOSoccer e = assertThrows(ExcepcionDOPOSoccer.class,
            () -> soccer.nuevoPartido(Resolucion.BAJA, Personaje.CAMERUN, Personaje.NIGERIA, null, false, 0));
        assertEquals(ExcepcionDOPOSoccer.CONFIGURACION_INVALIDA, e.getMessage());
    }

    @Test
    public void noDeberiaCrearPartidoConPaisOPerfilDesconocido() throws ExcepcionDOPOSoccer {
        soccer.nuevoJuego(1, "Ana", "Luis");
        assertThrows(ExcepcionDOPOSoccer.class,
            () -> soccer.nuevoPartido(Resolucion.BAJA, "Narnia", Personaje.NIGERIA, null, true, 2));
        assertThrows(ExcepcionDOPOSoccer.class,
            () -> soccer.nuevoPartido(Resolucion.BAJA, Personaje.CAMERUN, Personaje.NIGERIA, "Genio", true, 2));
    }

    @Test
    public void deberiaCrearUnPartidoJugadorVsJugadorPorTiempo() throws ExcepcionDOPOSoccer {
        soccer.nuevoJuego(2, "Ana", "Luis");
        soccer.nuevoPartido(Resolucion.ALTA, Personaje.ESTADOS_UNIDOS, Personaje.COREA_DEL_SUR, null, true, 3);
        Partido partido = soccer.getPartido();
        assertTrue(partido instanceof PartidoPorTiempo);
        assertEquals("3:00", partido.getDescripcionLimite());
        assertTrue(partido.getJugador(1) instanceof JugadorHumano);
        assertTrue(partido.getJugador(2) instanceof JugadorHumano);
        assertEquals("Ana", partido.getJugador(1).getCuenta().getNombre());
        assertEquals(Personaje.COREA_DEL_SUR, partido.getJugador(2).getPersonaje().getPais());
    }

    @Test
    public void deberiaCrearUnPartidoContraLaMaquina() throws ExcepcionDOPOSoccer {
        soccer.nuevoJuego(1, "Ana", "Luis");
        for (String perfil : DOPOSoccer.PERFILES_MAQUINA) {
            soccer.nuevoPartido(Resolucion.BAJA, Personaje.CAMERUN, Personaje.NIGERIA, perfil, false, 3);
            assertTrue(soccer.getPartido().getJugador(2) instanceof Maquina);
            for (int i = 0; i < 100; i++) soccer.actualizar(0.02);
        }
    }

    @Test
    public void deberiaMoverAlJugadorDesdeLaFachada() throws ExcepcionDOPOSoccer {
        soccer.nuevoJuego(1, "Ana", "Luis");
        soccer.nuevoPartido(Resolucion.BAJA, Personaje.CAMERUN, Personaje.NIGERIA, null, false, 3);
        for (int i = 0; i < 40; i++) soccer.actualizar(0.05);
        Jugador jugador = soccer.getPartido().getJugador(1);
        double x = jugador.getX();
        soccer.moverDerecha(1);
        soccer.actualizar(0.05);
        assertTrue(jugador.getX() > x);
        soccer.detener(1);
        assertEquals(0, jugador.getVelocidadX(), 1e-9);
    }

    @Test
    public void deberiaAvisarSiElPoderNoEstaDisponible() throws ExcepcionDOPOSoccer {
        soccer.nuevoJuego(1, "Ana", "Luis");
        soccer.nuevoPartido(Resolucion.BAJA, Personaje.CAMERUN, Personaje.NIGERIA, null, false, 3);
        assertThrows(ExcepcionDOPOSoccer.class, () -> soccer.activarPoder(1));
        for (int i = 0; i < 205; i++) soccer.actualizar(0.05);
        soccer.activarPoder(1);
        assertTrue(soccer.getPartido().getJugador(1).tienePoderActivo());
    }

    @Test
    public void noDeberiaMejorarEstadisticasDuranteUnPartido() throws ExcepcionDOPOSoccer {
        soccer.nuevoJuego(1, "Ana", "Luis");
        soccer.getRanura(1).getCuenta(1).ganarPuntos(500);
        soccer.nuevoPartido(Resolucion.BAJA, Personaje.CAMERUN, Personaje.NIGERIA, null, false, 3);
        ExcepcionDOPOSoccer e = assertThrows(ExcepcionDOPOSoccer.class,
            () -> soccer.mejorarEstadistica(1, TipoEstadistica.VELOCIDAD));
        assertEquals(ExcepcionDOPOSoccer.PARTIDO_EN_CURSO, e.getMessage());
        soccer.terminar();
        soccer.mejorarEstadistica(1, TipoEstadistica.VELOCIDAD);
        assertEquals(2, soccer.getRanura(1).getCuenta(1).getEstadisticas().getNivel(TipoEstadistica.VELOCIDAD));
    }

    @Test
    public void deberiaContinuarUnaRanuraConSusPuntos() throws ExcepcionDOPOSoccer {
        assertThrows(ExcepcionDOPOSoccer.class, () -> soccer.seleccionarRanura(2));
        soccer.nuevoJuego(2, "Ana", "Luis");
        soccer.getRanura(2).getCuenta(1).ganarPuntos(100);
        soccer.nuevoJuego(1, "Eva", "Sol");
        soccer.seleccionarRanura(2);
        assertEquals(2, soccer.getRanuraActual());
        assertEquals(100, soccer.getRanura(2).getCuenta(1).getPuntos());
    }

    @Test
    public void lasAccionesSinPartidoDeberianAvisar() {
        ExcepcionDOPOSoccer e = assertThrows(ExcepcionDOPOSoccer.class, () -> soccer.saltar(1));
        assertEquals(ExcepcionDOPOSoccer.SIN_PARTIDO, e.getMessage());
        assertNotNull(e.getMessage());
    }
}
