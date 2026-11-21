package dominio;

/** Rayo: durante 3 s el rival no puede saltar y se mueve al 50 % de su velocidad. */
public class Electrocutado extends Efecto {

    public Electrocutado() {
        super(3);
    }

    @Override
    public boolean permiteSaltar() {
        return false;
    }

    @Override
    public double factorVelocidad() {
        return 0.5;
    }
}
