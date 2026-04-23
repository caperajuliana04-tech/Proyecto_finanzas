package App;

import Vistas.RegistroUsuarioView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage escenario) {
        RegistroUsuarioView vistaRegistroUsuario = new RegistroUsuarioView();

        Scene escena = new Scene(vistaRegistroUsuario, 1100, 760);

        String rutaCss = getClass().getResource("/styles/app.css").toExternalForm();
        escena.getStylesheets().add(rutaCss);

        escenario.setTitle("Proyecto Finanzas - Registro de Usuarios");
        escenario.setMinWidth(980);
        escenario.setMinHeight(680);
        escenario.setScene(escena);
        escenario.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}