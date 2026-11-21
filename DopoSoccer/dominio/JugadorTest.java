package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JugadorTest {

    private Jugador jugador;

    @BeforeEach
    public void setUp() throws ExcepcionDOPOSoccer {
        jugador = new JugadorHumano(Lado.IZQUIERDO, Personaje.crear(Personaje.CAMERUN), new Cuenta("Ana"));
    }

    @Test
    public void deberiaEmpezarConVidaMaximaYBarraVacia() {
        assertEquals(100, jugador.getVida(), 1e-9);
        assertEquals(0, jugador.getCargaBarra(), 1e-9);
        assertEquals(0, jugador.getGoles());
        assertFalse(jugador.tienePoderActivo());
    }

    @Test
    public void noDeberiaActivarElPoderConLaBarraIncompleta() {
        jugador.actualizar(9);
        ExcepcionDOPOSoccer e = assertThrows(ExcepcionDOPOSoccer.class, () -> jugador.activarPoder());
        assertEquals(ExcepcionDOPOSoccer.PODER_NO_DISPONIBLE, e.getMessage());
        assertFalse(jugador.tienePoderActivo());
    }

    @Test
    public void deberiaActivarElPoderYVaciarLaBarraAlLlenarse() throws ExcepcionDOPOSoccer {
        llenarBarra();
        jugador.activarPoder();
        assertTrue(jugador.tienePoderActivo());
        assertEquals(0, jugador.getCargaBarra(), 1e-9);
    }

    @Test
    public void noDeberiaActivarOtroPoderMientrasTengaElAura() throws ExcepcionDOPOSoccer {
        llenarBarra();
        jugador.activarPoder();
        llenarBarra();
        assertThrows(ExcepcionDOPOSoccer.class, () -> jugador.activarPoder());
    }

    @Test
    public void deberiaSumarUnCuartoDeBarraAlRecibirGol() {
        jugador.recibirGol();
        assertEquals(0.25, jugador.getCargaBarra(), 1e-9);
    }

    @Test
    public void deberiaQuedarAturdidoAlLlegarACeroYRecuperarLaVida() {
        jugador.recibirDanio(150);
        assertEquals(0, jugador.getVida(), 1e-9);
        assertTrue(jugador.estaAturdido());
        double x = jugador.getX();
        jugador.moverDerecha();
        assertEquals(0, jugador.getVelocidadX(), 1e-9);
        for (int i = 0; i < 31; i++) jugador.actualizar(0.1);
        assertFalse(jugador.estaAturdido());
        assertEquals(100, jugador.getVida(), 1e-9);
        assertEquals(x, jugador.getX(), 1e-9);
    }

    @Test
    public void deberiaHacerElDanioDePatadaSegunSuKick() throws ExcepcionDOPOSoccer {
        for (int i = 0; i < 4; i++) {
            jugador.getCuenta().ganarPuntos(jugador.getCuenta().getEstadisticas().precioMejora(TipoEstadistica.PATADA));
            jugador.getCuenta().comprarMejora(TipoEstadistica.PATADA);
        }
        assertEquals(14, jugador.danioPatada(), 1e-9);
    }

    @Test
    public void electrocutadoDeberiaFrenarloYNoDejarloSaltar() {
        jugador.aplicarEfecto(new Electrocutado());
        jugador.moverDerecha();
        assertEquals(Jugador.VELOCIDAD_BASE * 0.5, jugador.getVelocidadX(), 1e-9);
        jugador.saltar();
        assertEquals(0, jugador.getVelocidadY(), 1e-9);
    }

    @Test
    public void unEfectoNuevoDeberiaReemplazarAlAnterior() {
        jugador.aplicarEfecto(new Electrocutado());
        jugador.aplicarEfecto(new Confusion());
        assertEquals("Confusión", jugador.getEfecto().getNombre());
        jugador.moverDerecha();
        assertEquals(Jugador.VELOCIDAD_BASE, jugador.getVelocidadX(), 1e-9);
    }

    @Test
    public void elEfectoDeberiaTerminarCuandoPasaSuDuracion() {
        jugador.aplicarEfecto(new Empuje());
        jugador.actualizar(1.0);
        jugador.actualizar(0.6);
        assertNull(jugador.getEfecto());
    }

    @Test
    public void deberiaSaltarSoloSiEstaApoyado() {
        jugador.ubicar(100, Cancha.SUELO - Jugador.ALTO);
        jugador.saltar();
        assertTrue(jugador.getVelocidadY() < 0);
        double velocidadDelSalto = jugador.getVelocidadY();
        jugador.actualizar(0.05);
        jugador.saltar();
        assertTrue(jugador.getVelocidadY() > velocidadDelSalto);
    }

    @Test
    public void deberiaAlcanzarAlRivalSoloSiEstaCerca() throws ExcepcionDOPOSoccer {
        Jugador rival = new JugadorHumano(Lado.DERECHO, Personaje.crear(Personaje.NIGERIA), new Cuenta("Luis"));
        jugador.ubicar(100, 300);
        rival.ubicar(100 + Jugador.ANCHO + 10, 300);
        assertTrue(jugador.alcanzaA(rival));
        rival.ubicar(100 + Jugador.ANCHO + 40, 300);
        assertFalse(jugador.alcanzaA(rival));
    }

    private void llenarBarra() {
        for (int i = 0; i < 201; i++) jugador.actualizar(0.05);
    }
}
