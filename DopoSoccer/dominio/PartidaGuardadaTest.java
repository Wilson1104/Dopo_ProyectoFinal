package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Pruebas de guardar y cargar la partida. Usan una carpeta temporal, así que no tocan la partida real. */
class PartidaGuardadaTest {

    @TempDir
    Path carpeta;

    @Test
    void guardarYAbrirConservaNombresYPuntos() throws Exception {
        File archivo = carpeta.resolve("partida.dat").toFile();
        DOPOSoccer original = new DOPOSoccer();
        original.nuevoJuego(1, "Ana", "Luis");
        original.getRanura(1).getCuenta(1).ganarPuntos(730);
        original.guardar(archivo);

        DOPOSoccer recuperado = new DOPOSoccer();
        recuperado.abrir(archivo);
        Cuenta ana = recuperado.getRanura(1).getCuenta(1);
        assertEquals("Ana", ana.getNombre());
        assertEquals(730, ana.getPuntos());
        assertEquals("Luis", recuperado.getRanura(1).getCuenta(2).getNombre());
    }

    @Test
    void abrirSinArchivoAvisaQueNoHayPartida() {
        File archivo = carpeta.resolve("no-existe.dat").toFile();
        DOPOSoccer soccer = new DOPOSoccer();
        ExcepcionDOPOSoccer error = assertThrows(ExcepcionDOPOSoccer.class, () -> soccer.abrir(archivo));
        assertEquals(ExcepcionDOPOSoccer.NO_HAY_PARTIDA, error.getMessage());
    }

    @Test
    void sinCuentasNoSeCreaNingunArchivo() throws Exception {
        File archivo = carpeta.resolve("vacia.dat").toFile();
        new DOPOSoccer().guardar(archivo);
        assertFalse(archivo.exists());
    }

    @Test
    void partidoAMedioJugarSeRecuperaEnPausaYSigueDesdeAhi() throws Exception {
        File archivo = carpeta.resolve("en-curso.dat").toFile();
        DOPOSoccer original = new DOPOSoccer();
        original.nuevoJuego(1, "Ana", "Luis");
        original.nuevoPartido(Resolucion.BAJA, Personaje.CAMERUN, Personaje.NIGERIA, null, false, 3);
        original.moverDerecha(1);
        for (int i = 0; i < 40; i++) original.actualizar(0.05);
        original.guardar(archivo);

        DOPOSoccer recuperado = new DOPOSoccer();
        recuperado.abrir(archivo);
        Partido partido = recuperado.getPartido();
        assertNotNull(partido);
        assertTrue(partido.isPausado());
        assertEquals(original.getPartido().getTiempoTranscurrido(), partido.getTiempoTranscurrido());
        assertEquals(original.getPartido().getJugador(1).getX(), partido.getJugador(1).getX());

        recuperado.reanudar();
        double antes = partido.getTiempoTranscurrido();
        recuperado.actualizar(0.05);
        assertTrue(partido.getTiempoTranscurrido() > antes);
    }

    @Test
    void golDelPartidoLlegaALaCuentaAlCargar() throws Exception {
        File archivo = carpeta.resolve("goles.dat").toFile();
        DOPOSoccer original = new DOPOSoccer();
        original.nuevoJuego(1, "Ana", "Luis");
        original.nuevoPartido(Resolucion.BAJA, Personaje.CAMERUN, Personaje.NIGERIA, null, false, 3);
        Partido partido = original.getPartido();
        partido.registrarGol(partido.getJugador(1));
        original.guardar(archivo);

        DOPOSoccer recuperado = new DOPOSoccer();
        recuperado.abrir(archivo);
        Jugador ana = recuperado.getPartido().getJugador(1);
        assertEquals(1, ana.getGoles());
        assertSame(recuperado.getRanura(1).getCuenta(1), ana.getCuenta());
    }

    @Test
    void partidoTerminadoNoSeGuardaPeroSusPuntosSi() throws Exception {
        File archivo = carpeta.resolve("terminado.dat").toFile();
        DOPOSoccer original = new DOPOSoccer();
        original.nuevoJuego(1, "Ana", "Luis");
        original.nuevoPartido(Resolucion.BAJA, Personaje.CAMERUN, Personaje.NIGERIA, null, false, 3);
        original.terminar();
        original.guardar(archivo);

        DOPOSoccer recuperado = new DOPOSoccer();
        recuperado.abrir(archivo);
        assertNull(recuperado.getPartido());
        assertEquals(25, recuperado.getRanura(1).getCuenta(1).getPuntos());
    }
}
