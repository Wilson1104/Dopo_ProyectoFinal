package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
