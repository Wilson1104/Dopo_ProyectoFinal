package dominio;

/**
 * Personaje que representa a un país. Los personajes solo se diferencian por su país y su tiro de poder;
 * para agregar uno nuevo basta con crear su TiroDePoder y registrarlo en {@link #crear(String)}.
 */
public class Personaje {

    public static final String COREA_DEL_SUR = "Corea del Sur";
    public static final String CAMERUN = "Camerún";
    public static final String NIGERIA = "Nigeria";
    public static final String ESTADOS_UNIDOS = "Estados Unidos";

    private final String pais;
    private final TiroDePoder tiroDePoder;

    private Personaje(String pais, TiroDePoder tiroDePoder) {
        this.pais = pais;
        this.tiroDePoder = tiroDePoder;
    }

    public static Personaje crear(String pais) throws ExcepcionDOPOSoccer {
        if (COREA_DEL_SUR.equals(pais)) return new Personaje(pais, new AuraAzul());
        if (CAMERUN.equals(pais)) return new Personaje(pais, new Rayo());
        if (NIGERIA.equals(pais)) return new Personaje(pais, new Tornado());
        if (ESTADOS_UNIDOS.equals(pais)) return new Personaje(pais, new Ilusion());
        throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.PAIS_DESCONOCIDO);
    }

    public static String[] paisesDisponibles() {
        return new String[] {COREA_DEL_SUR, CAMERUN, NIGERIA, ESTADOS_UNIDOS};
    }

    public String getPais() {
        return pais;
    }

    public TiroDePoder getTiroDePoder() {
        return tiroDePoder;
    }
}
