package App;

import Servidor.WebServer;
import javafx.application.Application;

// Punto de entrada que NO extiende Application.
// Este truco permite que el IDE (botón Play / F5) arranque la app sin
// configurar --module-path: cuando la clase con main() no extiende Application,
// el runtime de JavaFX se carga desde el classpath sin restricciones de módulo.
public class Launcher {
    public static void main(String[] args) {
        WebServer.iniciar();
        Application.launch(MainApp.class, args);
    }
}
