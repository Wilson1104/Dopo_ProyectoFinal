package dominio;

/**
 * Ilusión: durante 2.5 s el rival no sabe cuál de los 8 balones es el real. No le quita movimientos;
 * la confusión está en que los balones se ven iguales.
 */
public class Confusion extends Efecto {

    public Confusion() {
        super(2.5);
    }

    @Override
    public String getNombre() {
        return "Confusión";
    }
}
