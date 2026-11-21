package dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CuentaTest {

    @Test
    public void deberiaAcumularPuntos() {
        Cuenta cuenta = new Cuenta("Ana");
        cuenta.ganarPuntos(100);
        cuenta.ganarPuntos(25);
        cuenta.ganarPuntos(-10);
        assertEquals(125, cuenta.getPuntos());
    }

    @Test
    public void deberiaComprarUnaMejoraYDescontarLosPuntos() throws ExcepcionDOPOSoccer {
        Cuenta cuenta = new Cuenta("Ana");
        cuenta.ganarPuntos(1600);
        cuenta.comprarMejora(TipoEstadistica.PATADA);
        assertEquals(1100, cuenta.getPuntos());
        assertEquals(2, cuenta.getEstadisticas().getNivel(TipoEstadistica.PATADA));
        cuenta.comprarMejora(TipoEstadistica.PATADA);
        assertEquals(100, cuenta.getPuntos());
        assertEquals(3, cuenta.getEstadisticas().getNivel(TipoEstadistica.PATADA));
    }

    @Test
    public void noDeberiaComprarSinPuntosSuficientes() {
        Cuenta cuenta = new Cuenta("Ana");
        cuenta.ganarPuntos(499);
        ExcepcionDOPOSoccer e = assertThrows(ExcepcionDOPOSoccer.class,
            () -> cuenta.comprarMejora(TipoEstadistica.VELOCIDAD));
        assertEquals(ExcepcionDOPOSoccer.PUNTOS_INSUFICIENTES, e.getMessage());
        assertEquals(499, cuenta.getPuntos());
        assertEquals(1, cuenta.getEstadisticas().getNivel(TipoEstadistica.VELOCIDAD));
    }

    @Test
    public void deberiaValidarLosNombresDeUnaRanura() throws ExcepcionDOPOSoccer {
        Ranura ranura = new Ranura(1);
        assertThrows(ExcepcionDOPOSoccer.class, () -> ranura.crearCuentas("", "Luis"));
        assertThrows(ExcepcionDOPOSoccer.class, () -> ranura.crearCuentas("Ana", "ana"));
        ranura.crearCuentas(" Ana ", "Luis");
        assertEquals("Ana", ranura.getCuenta(1).getNombre());
        assertEquals("Luis", ranura.getCuenta(2).getNombre());
        assertThrows(ExcepcionDOPOSoccer.class, () -> ranura.getCuenta(3));
    }
}
