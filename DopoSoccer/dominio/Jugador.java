package dominio;

/**
 * Jugador en la cancha durante un partido. Sus estadísticas y puntos viven en su cuenta;
 * aquí están la posición, los goles, la vida, la barra de poder, el aura y el efecto que sufre.
 */
public abstract class Jugador extends ElementoMovil {

    public static final double ANCHO = 34;
    public static final double ALTO = 52;
    public static final int DANIO_PATADA = 10;
    /** Velocidad al caminar con speed 1, en unidades por segundo. */
    public static final double VELOCIDAD_BASE = 250;
    /** Altura del salto con jump 1, en unidades. */
    public static final double SALTO_BASE = 140;
    public static final double TIEMPO_PATADA = 0.25;
    /** Distancia máxima a la que la patada alcanza al rival. */
    public static final double ALCANCE_PATADA = 24;

    private final Lado lado;
    private final Personaje personaje;
    private final Cuenta cuenta;
    private final BarraDePoder barra = new BarraDePoder();
    private int goles;
    private double vida;
    private boolean poderActivo;
    private double tiempoPatada;
    private Efecto efecto;

    protected Jugador(Lado lado, Personaje personaje, Cuenta cuenta) {
        super(ANCHO, ALTO);
        this.lado = lado;
        this.personaje = personaje;
        this.cuenta = cuenta;
        vida = cuenta.getEstadisticas().vidaMaxima();
    }

    public void moverIzquierda() {
        if (puedeMoverse()) velocidadX = -velocidadHorizontal();
    }

    public void moverDerecha() {
        if (puedeMoverse()) velocidadX = velocidadHorizontal();
    }

    public void detener() {
        if (puedeMoverse()) velocidadX = 0;
    }

    /** Solo salta si está apoyado (en el suelo o sobre un travesaño). */
    public void saltar() {
        if (puedeSaltar() && velocidadY == 0) velocidadY = -Math.sqrt(2 * GRAVEDAD * alturaDeSalto());
    }

    /** Altura que alcanza un salto, en unidades de cancha: SALTO_BASE x factor de la estadística jump. */
    public double alturaDeSalto() {
        return SALTO_BASE * cuenta.getEstadisticas().factorSalto();
    }

    public void patear() {
        if (puedePatear() && tiempoPatada <= 0) tiempoPatada = TIEMPO_PATADA;
    }

    /** Con la barra llena activa el aura; la barra se vacía y empieza a recargarse de inmediato. */
    public void activarPoder() throws ExcepcionDOPOSoccer {
        if (!poderDisponible()) throw new ExcepcionDOPOSoccer(ExcepcionDOPOSoccer.PODER_NO_DISPONIBLE);
        barra.vaciar();
        poderActivo = true;
    }

    public boolean poderDisponible() {
        return barra.estaLlena() && !poderActivo;
    }

    /** Los jugadores humanos se controlan con el teclado; las máquinas deciden aquí qué hacer. */
    public void decidirAccion(Partido partido) {
    }

    public void actualizar(double segundos) {
        barra.cargar(segundos);
        if (efecto != null) {
            efecto.actualizar(segundos);
            if (efecto.haTerminado()) {
                Efecto terminado = efecto;
                efecto = null;
                terminado.finalizar(this);
            }
        }
        tiempoPatada = Math.max(0, tiempoPatada - segundos);
        if (!puedeMoverse()) velocidadX *= Math.pow(0.05, segundos);
        mover(segundos);
    }

    /**
     * Con aura, ejecuta el tiro de poder del personaje y el aura desaparece. Sin aura, empuja el balón:
     * de cabeza si lo toca arriba, más fuerte hacia el arco rival si está pateando.
     */
    public void tocarBalon(Balon balon, Partido partido) {
        if (poderActivo) {
            poderActivo = false;
            personaje.getTiroDePoder().lanzar(partido, this);
            return;
        }
        boolean deCabeza = balon.centroY() < y + alto * 0.35;
        double sentido = balon.centroX() < centroX() ? -1 : 1;
        double bx = deCabeza ? balon.getX() : (sentido < 0 ? x - balon.getAncho() : x + ancho);
        bx = Math.max(0, Math.min(Cancha.ANCHO - balon.getAncho(), bx));
        balon.ubicar(bx, deCabeza ? y - balon.getAlto() : balon.getY());
        if (estaPateando()) balon.impulsar(-haciaSuArco() * 650, -380);
        else balon.impulsar(sentido * 260 + velocidadX * 0.5, deCabeza ? -420 : -180);
    }

    public boolean estaPateando() {
        return tiempoPatada > 0;
    }

    /** La patada ya golpeó al rival: no vuelve a hacerle daño hasta la siguiente. */
    public void terminarPatada() {
        tiempoPatada = 0;
    }

    public boolean alcanzaA(Jugador rival) {
        boolean mismaAltura = y < rival.y + rival.alto && rival.y < y + alto;
        double distancia = Math.max(rival.x - (x + ancho), x - (rival.x + rival.ancho));
        return mismaAltura && distancia <= ALCANCE_PATADA;
    }

    /** Patada normal: 10 x multiplicadorKick. */
    public double danioPatada() {
        return DANIO_PATADA * multiplicadorPatada();
    }

    public double multiplicadorPatada() {
        return cuenta.getEstadisticas().multiplicadorPatada();
    }

    /** La vida nunca baja de cero; al llegar a cero el jugador queda aturdido. */
    public void recibirDanio(double danio) {
        if (danio <= 0) return;
        vida = Math.max(0, vida - danio);
        if (vida == 0 && !estaAturdido()) aplicarEfecto(new Aturdimiento());
    }

    /** Los efectos no se acumulan: el nuevo reemplaza al anterior. */
    public void aplicarEfecto(Efecto nuevo) {
        if (efecto != null) efecto.finalizar(this);
        efecto = nuevo;
        nuevo.iniciar(this);
    }

    public void restaurarVida() {
        vida = getVidaMaxima();
    }

    public void anotarGol() {
        goles++;
    }

    public void recibirGol() {
        barra.sumarCuarto();
    }

    public void ganarPuntos(int puntos) {
        cuenta.ganarPuntos(puntos);
    }

    /** -1 si su arco está a la izquierda, 1 si está a la derecha. */
    public int haciaSuArco() {
        return lado == Lado.IZQUIERDO ? -1 : 1;
    }

    public boolean estaAturdido() {
        return efecto instanceof Aturdimiento;
    }

    private boolean puedeMoverse() {
        return efecto == null || efecto.permiteMoverse();
    }

    private boolean puedeSaltar() {
        return efecto == null || efecto.permiteSaltar();
    }

    private boolean puedePatear() {
        return efecto == null || efecto.permitePatear();
    }

    private double velocidadHorizontal() {
        double factorEfecto = efecto == null ? 1 : efecto.factorVelocidad();
        return VELOCIDAD_BASE * cuenta.getEstadisticas().factorVelocidad() * factorEfecto;
    }

    public Lado getLado() {
        return lado;
    }

    public int getGoles() {
        return goles;
    }

    public double getVida() {
        return vida;
    }

    public double getVidaMaxima() {
        return cuenta.getEstadisticas().vidaMaxima();
    }

    public boolean tienePoderActivo() {
        return poderActivo;
    }

    public double getCargaBarra() {
        return barra.getCarga();
    }

    public Personaje getPersonaje() {
        return personaje;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public Efecto getEfecto() {
        return efecto;
    }
}
