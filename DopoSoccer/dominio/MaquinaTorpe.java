package dominio;

/** Perfil torpe: escoge sus movimientos al azar. */
public class MaquinaTorpe extends Maquina {

    private int direccion;

    public MaquinaTorpe(Lado lado, Personaje personaje) {
        super(lado, personaje);
    }

    @Override
    public void decidirAccion(Partido partido) {
        if (azar.nextDouble() < 0.04) direccion = azar.nextInt(3) - 1;
        if (direccion < 0) moverIzquierda();
        else if (direccion > 0) moverDerecha();
        else detener();
        if (azar.nextDouble() < 0.02) saltar();
        if (azar.nextDouble() < 0.03) patear();
        if (poderDisponible() && azar.nextDouble() < 0.01) intentarPoder();
    }
}
