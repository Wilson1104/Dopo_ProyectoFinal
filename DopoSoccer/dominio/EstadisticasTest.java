package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EstadisticasTest {

    private Estadisticas estadisticas;

    @BeforeEach
    public void setUp() {
        estadisticas = new Estadisticas();
    }

    @Test
    public void deberiaEmpezarConTodasLasEstadisticasEnUno() {
        for (TipoEstadistica tipo : TipoEstadistica.values()) assertEquals(1, estadisticas.getNivel(tipo));
        assertEquals(100, estadisticas.vidaMaxima(), 1e-9);
        assertEquals(1.0, estadisticas.multiplicadorPatada(), 1e-9);
    }

    @Test
    public void deberiaCalcularLaVidaMaximaDelAnexo() throws ExcepcionDOPOSoccer {
        mejorarHasta(TipoEstadistica.SUPERVIVENCIA, 3);
        assertEquals(120, estadisticas.vidaMaxima(), 1e-9);
        mejorarHasta(TipoEstadistica.SUPERVIVENCIA, 10);
        assertEquals(190, estadisticas.vidaMaxima(), 1e-9);
    }

    @Test
    public void deberiaCalcularElMultiplicadorDePatadaDelAnexo() throws ExcepcionDOPOSoccer {
        mejorarHasta(TipoEstadistica.PATADA, 5);
        assertEquals(1.4, estadisticas.multiplicadorPatada(), 1e-9);
    }

    @Test
    public void deberiaDuplicarElPrecioCadaVezQueSeCompra() throws ExcepcionDOPOSoccer {
        assertEquals(500, estadisticas.precioMejora(TipoEstadistica.VELOCIDAD));
        estadisticas.mejorar(TipoEstadistica.VELOCIDAD);
        assertEquals(1000, estadisticas.precioMejora(TipoEstadistica.VELOCIDAD));
        estadisticas.mejorar(TipoEstadistica.VELOCIDAD);
        assertEquals(2000, estadisticas.precioMejora(TipoEstadistica.VELOCIDAD));
        assertEquals(500, estadisticas.precioMejora(TipoEstadistica.SALTO));
    }

    @Test
    public void noDeberiaPasarDelNivelMaximo() throws ExcepcionDOPOSoccer {
        mejorarHasta(TipoEstadistica.SALTO, 10);
        ExcepcionDOPOSoccer e = assertThrows(ExcepcionDOPOSoccer.class, () -> estadisticas.mejorar(TipoEstadistica.SALTO));
        assertEquals(ExcepcionDOPOSoccer.ESTADISTICA_MAXIMA, e.getMessage());
        assertThrows(ExcepcionDOPOSoccer.class, () -> estadisticas.precioMejora(TipoEstadistica.SALTO));
    }

    private void mejorarHasta(TipoEstadistica tipo, int nivel) throws ExcepcionDOPOSoccer {
        while (estadisticas.getNivel(tipo) < nivel) estadisticas.mejorar(tipo);
    }
}
