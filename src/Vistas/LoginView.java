package Vistas;

import App.MainApp;
import Modelos.Usuario;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

// Pantalla de inicio de sesión.
// El usuario ingresa su correo y contraseña para acceder al dashboard.
// Extiende BorderPane para aprovechar el área central del layout.
public class LoginView extends BorderPane {

    private final Stage stage;         // Referencia a la ventana principal para poder cambiar de escena
    private TextField campoCorreo;     // Campo de texto para el correo electrónico
    private PasswordField campoContrasena; // Campo de contraseña (oculta caracteres)
    private Label etiquetaEstado;      // Muestra mensajes de error o éxito al usuario

    // Constructor: recibe el stage para poder navegar hacia el dashboard al autenticar
    public LoginView(Stage stage) {
        this.stage = stage;
        construirVista();
    }

    // Construye y organiza todos los elementos visuales de la pantalla
    private void construirVista() {
        this.getStyleClass().add("app-root"); // Aplica el fondo degradado definido en app.css

        // Contenedor central que centra la tarjeta del formulario
        StackPane fondo = new StackPane();
        fondo.getStyleClass().add("background-pane");
        fondo.setPadding(new Insets(40));

        // Tarjeta blanca que contiene el formulario de login
        VBox tarjeta = new VBox(20);
        tarjeta.getStyleClass().add("form-card");
        tarjeta.setMaxWidth(440);
        tarjeta.setPadding(new Insets(40));
        tarjeta.setAlignment(Pos.CENTER);

        Label titulo = new Label("💰 Finanzas");
        titulo.getStyleClass().add("title-label");
        titulo.setAlignment(Pos.CENTER);

        Label subtitulo = new Label("Inicia sesión para gestionar tus finanzas personales.");
        subtitulo.getStyleClass().add("subtitle-label");
        subtitulo.setWrapText(true);
        subtitulo.setAlignment(Pos.CENTER);

        // Campos de entrada
        campoCorreo = new TextField();
        campoCorreo.setPromptText("correo@ejemplo.com");
        campoCorreo.getStyleClass().add("form-input");

        campoContrasena = new PasswordField();
        campoContrasena.setPromptText("Contraseña");
        campoContrasena.getStyleClass().add("form-input");

        // Agrupa cada campo con su etiqueta descriptiva
        VBox grupoCampos = new VBox(12,
            crearGrupo("Correo electrónico", campoCorreo),
            crearGrupo("Contraseña", campoContrasena)
        );

        // Botón principal: llama a iniciarSesion() al hacer clic
        Button botonIngresar = new Button("Iniciar sesión");
        botonIngresar.getStyleClass().add("primary-button");
        botonIngresar.setMaxWidth(Double.MAX_VALUE);
        botonIngresar.setOnAction(e -> iniciarSesion());

        // También permite iniciar sesión presionando Enter desde el campo de contraseña
        campoContrasena.setOnAction(e -> iniciarSesion());

        // Botón secundario: abre la ventana de registro de nuevo usuario
        Button botonRegistro = new Button("Crear cuenta nueva");
        botonRegistro.getStyleClass().add("secondary-button");
        botonRegistro.setMaxWidth(Double.MAX_VALUE);
        botonRegistro.setOnAction(e -> abrirRegistro());

        // Etiqueta que muestra mensajes de error o confirmación
        etiquetaEstado = new Label("");
        etiquetaEstado.getStyleClass().add("status-label");
        etiquetaEstado.setWrapText(true);
        etiquetaEstado.setMaxWidth(Double.MAX_VALUE);

        tarjeta.getChildren().addAll(titulo, subtitulo, grupoCampos, botonIngresar, botonRegistro, etiquetaEstado);
        fondo.getChildren().add(tarjeta);
        this.setCenter(fondo);

        // Animación de entrada: la tarjeta aparece con fade + deslizamiento desde abajo
        FadeTransition fade = new FadeTransition(Duration.millis(600), tarjeta);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(600), tarjeta);
        slide.setFromY(20);
        slide.setToY(0);

        fade.play();
        slide.play();
    }

    // Crea un grupo visual: etiqueta encima del campo de entrada
    private VBox crearGrupo(String etiquetaTexto, javafx.scene.control.Control campo) {
        Label etiqueta = new Label(etiquetaTexto);
        etiqueta.getStyleClass().add("field-label");
        return new VBox(6, etiqueta, campo);
    }

    // Valida las credenciales ingresadas y navega al dashboard si son correctas
    private void iniciarSesion() {
        String correo = campoCorreo.getText().trim();
        String contrasena = campoContrasena.getText().trim();

        // Valida que ambos campos estén llenos
        if (correo.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Completa todos los campos.");
            return;
        }

        // Busca el usuario en la base de datos por correo
        Usuario usuario = Usuario.buscarPorCorreo(correo);

        if (usuario == null) {
            mostrarError("No existe una cuenta con ese correo.");
            return;
        }

        // Verifica que la contraseña coincida
        if (!usuario.getContrasena().equals(contrasena)) {
            mostrarError("Contraseña incorrecta.");
            return;
        }

        // Credenciales correctas: abre el dashboard pasando el objeto usuario autenticado
        MainApp.abrirDashboard(stage, usuario);
    }

    // Abre una nueva ventana (Stage independiente) con el formulario de registro
    private void abrirRegistro() {
        Stage ventana = new Stage();
        RegistroUsuarioView registro = new RegistroUsuarioView();
        Scene escena = new Scene(registro, 900, 680);
        String rutaCss = getClass().getResource("/styles/app.css").toExternalForm();
        escena.getStylesheets().add(rutaCss);
        ventana.setTitle("Registro de nuevo usuario");
        ventana.setScene(escena);
        ventana.show();
    }

    // Muestra un mensaje de error con estilo rojo bajo los botones
    private void mostrarError(String texto) {
        etiquetaEstado.setText(texto);
        etiquetaEstado.getStyleClass().removeAll("status-success", "status-error");
        etiquetaEstado.getStyleClass().add("status-error");
    }
}
