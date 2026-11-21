package dominio;

/** Perfil defensivo: su prioridad es cuidar su arco. */
public class MaquinaDefensiva extends Maquina {

    public MaquinaDefensiva(Lado lado, Personaje personaje) {
        super(lado, personaje);
    }

    @Override
    public void decidirAccion(Partido partido) {
        defender(partido);
    }
}
