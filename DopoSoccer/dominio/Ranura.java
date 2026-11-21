package dominio;

/** Espacio de "nuevo juego": guarda las cuentas de los dos jugadores que juegan juntos. */
public class Ranura {

    private final int numero;
    private Cuenta[] cuentas;

    public Ranura(int numero) {
        this.numero = numero;
    }

    public boolean estaVacia() {
        return cuentas == null;
    }

    public void crearCuentas(String nombre1, String nombre2) throws ExcepcionDOPOSoccer {
        if (nombre1 == null || nombre2 == null) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.NOMBRE_INVALIDO);
        String n1 = nombre1.trim();
        String n2 = nombre2.trim();
        if (n1.isEmpty() || n2.isEmpty() || n1.equalsIgnoreCase(n2)) {
            throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.NOMBRE_INVALIDO);
        }
        cuentas = new Cuenta[] {new Cuenta(n1), new Cuenta(n2)};
    }

    /** @param numero 1 o 2 */
    public Cuenta getCuenta(int numero) throws ExcepcionDOPOSoccer {
        if (estaVacia() || numero < 1 || numero > 2) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.RANURA_INVALIDA);
        return cuentas[numero - 1];
    }

    public int getNumero() {
        return numero;
    }
}
