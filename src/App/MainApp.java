package App;

import Modelos.Usuario;
import Vistas.DashboardView;
import Vistas.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Punto de entrada de la aplicación JavaFX.
// Controla la navegación entre la pantalla de login y el dashboard principal.
public class MainApp extends Application {

    // start() es llamado automáticamente por JavaFX al lanzar la app.
    // stage es la ventana principal del sistema operativo.
    @Override
    public void start(Stage escenario) {
        mostrarLogin(escenario);
    }

    // Carga la pantalla de inicio de sesión en el stage dado.
    // Es estático para poder ser llamado desde cualquier vista (ej: al cerrar sesión).
    public static void mostrarLogin(Stage stage) {
        LoginView login = new LoginView(stage);
        Scene escena = new Scene(login, 1100, 760);
        // Carga el archivo CSS desde la carpeta de recursos empaquetada
        String rutaCss = MainApp.class.getResource("/styles/app.css").toExternalForm();
        escena.getStylesheets().add(rutaCss);
        stage.setTitle("Finanzas — Iniciar sesión");
        stage.setMinWidth(900);
        stage.setMinHeight(640);
        stage.setScene(escena);
        stage.show();
    }

    // Carga el dashboard principal para el usuario autenticado.
    // Se llama desde LoginView una vez verificadas las credenciales.
    public static void abrirDashboard(Stage stage, Usuario usuario) {
        DashboardView dashboard = new DashboardView(stage, usuario);
        Scene escena = new Scene(dashboard, 1200, 760);
        String rutaCss = MainApp.class.getResource("/styles/app.css").toExternalForm();
        escena.getStylesheets().add(rutaCss);
        // El título muestra el nombre del usuario autenticado
        stage.setTitle("Finanzas — " + usuario.getNombre());
        stage.setMinWidth(1000);
        stage.setMinHeight(640);
        stage.setScene(escena);
    }

    // Método main: lanza la aplicación JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}
