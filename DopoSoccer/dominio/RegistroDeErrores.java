package dominio;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Log de errores para los programadores: guarda la fecha y la traza de cada error en un archivo.
 */
public class RegistroDeErrores {

    public static final String ARCHIVO = "errores.log";

    private RegistroDeErrores() {
    }

    public static void registrar(Exception error) {
        try (PrintWriter salida = new PrintWriter(new FileWriter(ARCHIVO, true))) {
            salida.println("[" + LocalDateTime.now() + "] " + error);
            error.printStackTrace(salida);
            salida.println();
        } catch (IOException e) {
            System.err.println("No se pudo escribir en " + ARCHIVO + ": " + e.getMessage());
        }
    }
}
