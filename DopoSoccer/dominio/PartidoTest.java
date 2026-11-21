package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PartidoTest {

    private Jugador jugador1;
    private Jugador jugador2;

    @BeforeEach
    public void setUp() throws ExcepcionDOPOSoccer {
        jugador1 = new JugadorHumano(Lado.IZQUIERDO, Personaje.crear(Personaje.COREA_DEL_SUR), new Cuenta("Ana"));
        jugador2 = new JugadorHumano(Lado.DERECHO, Personaje.crear(Personaje.CAMERUN), new Cuenta("Luis"));
    }

    @Test
    public void deberiaEmpezarCeroACeroConElBalonEnElCentro() {
        Partido partido = new PartidoPorGoles(Resolucion.MEDIA, jugador1, jugador2, 3);
        assertEquals(0, jugador1.getGoles());
        assertEquals(0, jugador2.getGoles());
        Balon balon = partido.getBalonReal();
        assertEquals(Cancha.ANCHO / 2, balon.centroX(), 1e-9);
        assertEquals(Resolucion.MEDIA, partido.getCancha().getResolucion());
    }

    @Test
    public void registrarGolDeberiaSumarAlAnotadorYDarleBarraAlRival() {
        Partido partido = new PartidoPorGoles(Resolucion.BAJA, jugador1, jugador2, 3);
        partido.registrarGol(jugador1);
        assertEquals(1, jugador1.getGoles());
        assertEquals(0.25, jugador2.getCargaBarra(), 1e-9);
        assertEquals(0, jugador1.getCargaBarra(), 1e-9);
    }

    @Test
    public void porGolesDeberiaTerminarAlLlegarALaMeta() {
        Partido partido = new PartidoPorGoles(Resolucion.BAJA, jugador1, jugador2, 2);
        partido.registrarGol(jugador2);
        partido.actualizar(0.01);
        assertFalse(partido.isTerminado());
        partido.registrarGol(jugador2);
        partido.actualizar(0.01);
        assertTrue(partido.isTerminado());
        assertSame(jugador2, partido.getGanador());
    }

    @Test
    public void alTerminarDeberiaDar100AlGanadorY25AlPerdedor() {
        Partido partido = new PartidoPorGoles(Resolucion.BAJA, jugador1, jugador2, 1);
        partido.registrarGol(jugador1);
        partido.actualizar(0.01);
        assertEquals(100, jugador1.getCuenta().getPuntos());
        assertEquals(25, jugador2.getCuenta().getPuntos());
        partido.terminar();
        assertEquals(100, jugador1.getCuenta().getPuntos());
    }

    @Test
    public void siTerminanEmpatadosCadaUnoDeberiaRecibir25() {
        Partido partido = new PartidoPorGoles(Resolucion.BAJA, jugador1, jugador2, 5);
        partido.terminar();
        assertNull(partido.getGanador());
        assertEquals(25, jugador1.getCuenta().getPuntos());
        assertEquals(25, jugador2.getCuenta().getPuntos());
    }

    @Test
    public void porTiempoConEmpateDeberiaIrAMuerteSubitaYTerminarConElSiguienteGol() {
        PartidoPorTiempo partido = new PartidoPorTiempo(Resolucion.BAJA, jugador1, jugador2, 1);
        avanzar(partido, 61);
        assertFalse(partido.isTerminado());
        assertTrue(partido.isMuerteSubita());
        assertEquals("MUERTE SÚBITA", partido.getDescripcionLimite());
        partido.registrarGol(jugador2);
        partido.actualizar(0.01);
        assertTrue(partido.isTerminado());
        assertSame(jugador2, partido.getGanador());
    }

    @Test
    public void porTiempoSinEmpateDeberiaTerminarAlAcabarseElTiempo() {
        PartidoPorTiempo partido = new PartidoPorTiempo(Resolucion.BAJA, jugador1, jugador2, 1);
        partido.registrarGol(jugador1);
        avanzar(partido, 30);
        assertEquals("0:30", partido.getDescripcionLimite());
        avanzar(partido, 31);
        assertTrue(partido.isTerminado());
        assertFalse(partido.isMuerteSubita());
    }

    @Test
    public void enPausaNoDeberiaAvanzarElTiempo() {
        PartidoPorTiempo partido = new PartidoPorTiempo(Resolucion.BAJA, jugador1, jugador2, 2);
        partido.pausar();
        avanzar(partido, 5);
        assertEquals(0, partido.getTiempoTranscurrido(), 1e-9);
        partido.reanudar();
        avanzar(partido, 5);
        assertEquals(5, partido.getTiempoTranscurrido(), 0.06);
    }

    @Test
    public void laBarraDeberiaSeguirCargandoEnMuerteSubita() {
        PartidoPorTiempo partido = new PartidoPorTiempo(Resolucion.BAJA, jugador1, jugador2, 1);
        avanzar(partido, 61);
        assertTrue(partido.isMuerteSubita());
        assertEquals(1, jugador1.getCargaBarra(), 1e-9);
    }

    @Test
    public void elBalonDeberiaCaerYQuedarSobreElCesped() {
        Partido partido = new PartidoPorGoles(Resolucion.BAJA, jugador1, jugador2, 3);
        avanzar(partido, 8);
        Balon balon = partido.getBalonReal();
        assertEquals(Cancha.SUELO, balon.getY() + balon.getAlto(), 1e-6);
    }

    @Test
    public void elBalonQuePasaLaLineaDelArcoDeberiaSerGol() {
        Partido partido = new PartidoPorGoles(Resolucion.BAJA, jugador1, jugador2, 3);
        Balon balon = partido.getBalonReal();
        Arco derecho = partido.getCancha().getArcoDerecho();
        balon.ubicar(Cancha.ANCHO - 20, Cancha.SUELO - 40);
        assertSame(derecho, partido.getCancha().arcoConGol(balon));
        balon.ubicar(Cancha.ANCHO / 2, Cancha.SUELO - 40);
        assertNull(partido.getCancha().arcoConGol(balon));
        balon.ubicar(Cancha.ANCHO - 20, Cancha.SUELO - 40);
        partido.actualizar(0.01);
        assertEquals(1, jugador1.getGoles());
    }

    private void avanzar(Partido partido, double segundos) {
        for (double t = 0; t < segundos; t += 0.05) partido.actualizar(0.05);
    }
}
