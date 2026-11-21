package dominio;

/** Perfil ofensivo: su prioridad es anotar, siempre va por el balón. */
public class MaquinaOfensiva extends Maquina {

    public MaquinaOfensiva(Lado lado, Personaje personaje) {
        super(lado, personaje);
    }

    @Override
    public void decidirAccion(Partido partido) {
        atacar(partido);
    }
}
