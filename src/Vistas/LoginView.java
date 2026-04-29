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

import java.util.regex.Pattern;

// Pantalla de inicio de sesión.
// El usuario ingresa su correo y contraseña para acceder al dashboard.
// Extiende BorderPane para aprovechar el área central del layout.
public class LoginView extends BorderPane {

    // Regex de correo: usuario@dominio.tld (al menos un punto en el dominio)
    private static final Pattern PATRON_CORREO =
        Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");
    // Longitud mínima de contraseña aceptada
    private static final int LONGITUD_MIN_CONTRASENA = 4;

    private final Stage stage;         // Referencia a la ventana principal para poder cambiar de escena
    private TextField campoCorreo;     // Campo de texto para el correo electrónico
    private PasswordField campoContrasena; // Campo de contraseña (oculta caracteres)
    private Label etiquetaEstado;      // Muestra mensajes de error o éxito al usuario
    private Label errorCorreo;         // Mensaje de error inline debajo del correo
    private Label errorContrasena;     // Mensaje de error inline debajo de la contraseña
    private Button botonIngresar;      // Botón principal: se habilita solo si todo es válido

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
        campoContrasena.setPromptText("Contraseña (mínimo " + LONGITUD_MIN_CONTRASENA + " caracteres)");
        campoContrasena.getStyleClass().add("form-input");
        // Bloqueo inicial: la contraseña se habilita solo cuando el correo sea válido
        campoContrasena.setDisable(true);

        // Etiquetas de error inline por campo (se muestran/ocultan según validación)
        errorCorreo = crearEtiquetaError();
        errorContrasena = crearEtiquetaError();

        // Agrupa cada campo con su etiqueta descriptiva y su mensaje de error
        VBox grupoCampos = new VBox(12,
            crearGrupo("Correo electrónico", campoCorreo, errorCorreo),
            crearGrupo("Contraseña", campoContrasena, errorContrasena)
        );

        // Botón principal: llama a iniciarSesion() al hacer clic
        botonIngresar = new Button("Iniciar sesión");
        botonIngresar.getStyleClass().add("primary-button");
        botonIngresar.setMaxWidth(Double.MAX_VALUE);
        botonIngresar.setOnAction(e -> iniciarSesion());
        // Bloqueo inicial: el botón se habilita solo cuando ambos campos sean válidos
        botonIngresar.setDisable(true);

        // También permite iniciar sesión presionando Enter desde el campo de contraseña
        campoContrasena.setOnAction(e -> iniciarSesion());

        // Conecta los listeners de validación campo a campo
        configurarValidaciones();

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

    // Crea un grupo visual: etiqueta encima del campo de entrada y, debajo, su error inline
    private VBox crearGrupo(String etiquetaTexto, javafx.scene.control.Control campo, Label errorInline) {
        Label etiqueta = new Label(etiquetaTexto);
        etiqueta.getStyleClass().add("field-label");
        return new VBox(6, etiqueta, campo, errorInline);
    }

    // Etiqueta de error que vive bajo cada campo. Empieza oculta (managed=false la saca del layout).
    private Label crearEtiquetaError() {
        Label etiqueta = new Label("");
        etiqueta.getStyleClass().add("field-error");
        etiqueta.setVisible(false);
        etiqueta.setManaged(false);
        return etiqueta;
    }

    // Engancha listeners para validar correo y contraseña a medida que el usuario escribe
    // y al perder foco. Mantiene el bloqueo encadenado: contraseña → solo si correo válido,
    // botón ingresar → solo si ambos válidos.
    private void configurarValidaciones() {
        campoCorreo.textProperty().addListener((obs, anterior, nuevo) -> {
            // Si el correo deja de ser válido, bloquea contraseña y limpia su error
            if (!correoEsValido()) {
                campoContrasena.setDisable(true);
                campoContrasena.clear();
                ocultarError(campoContrasena, errorContrasena);
            } else {
                campoContrasena.setDisable(false);
            }
            // Mientras escribe, limpia el error visual hasta que pierda foco
            ocultarError(campoCorreo, errorCorreo);
            actualizarBotonIngresar();
        });

        // Al salir del campo de correo, valida formato y muestra error si aplica
        campoCorreo.focusedProperty().addListener((obs, antes, ahora) -> {
            if (!ahora) validarCorreo();
        });

        campoContrasena.textProperty().addListener((obs, anterior, nuevo) -> {
            ocultarError(campoContrasena, errorContrasena);
            actualizarBotonIngresar();
        });

        campoContrasena.focusedProperty().addListener((obs, antes, ahora) -> {
            if (!ahora) validarContrasena();
        });
    }

    // Devuelve true si el correo cumple el formato esperado
    private boolean correoEsValido() {
        return PATRON_CORREO.matcher(campoCorreo.getText().trim()).matches();
    }

    // Devuelve true si la contraseña tiene al menos LONGITUD_MIN_CONTRASENA caracteres
    private boolean contrasenaEsValida() {
        return campoContrasena.getText().trim().length() >= LONGITUD_MIN_CONTRASENA;
    }

    // Valida el correo al perder foco; muestra error inline si no es válido
    private boolean validarCorreo() {
        String texto = campoCorreo.getText().trim();
        if (texto.isEmpty()) {
            mostrarError(campoCorreo, errorCorreo, "El correo es obligatorio.");
            return false;
        }
        if (!correoEsValido()) {
            mostrarError(campoCorreo, errorCorreo, "Formato de correo inválido (ej: nombre@dominio.com).");
            return false;
        }
        ocultarError(campoCorreo, errorCorreo);
        return true;
    }

    // Valida la contraseña al perder foco; muestra error inline si no es válida
    private boolean validarContrasena() {
        String texto = campoContrasena.getText().trim();
        if (texto.isEmpty()) {
            mostrarError(campoContrasena, errorContrasena, "La contraseña es obligatoria.");
            return false;
        }
        if (texto.length() < LONGITUD_MIN_CONTRASENA) {
            mostrarError(campoContrasena, errorContrasena,
                "Mínimo " + LONGITUD_MIN_CONTRASENA + " caracteres.");
            return false;
        }
        ocultarError(campoContrasena, errorContrasena);
        return true;
    }

    // Habilita el botón "Iniciar sesión" solo cuando ambos campos sean válidos
    private void actualizarBotonIngresar() {
        botonIngresar.setDisable(!(correoEsValido() && contrasenaEsValida()));
    }

    // Pinta el campo en estado de error y muestra el mensaje inline
    private void mostrarError(javafx.scene.control.Control campo, Label etiquetaError, String mensaje) {
        if (!campo.getStyleClass().contains("form-input-error")) {
            campo.getStyleClass().add("form-input-error");
        }
        etiquetaError.setText(mensaje);
        etiquetaError.setVisible(true);
        etiquetaError.setManaged(true);
    }

    // Quita el estado de error del campo y oculta el mensaje inline
    private void ocultarError(javafx.scene.control.Control campo, Label etiquetaError) {
        campo.getStyleClass().remove("form-input-error");
        etiquetaError.setVisible(false);
        etiquetaError.setManaged(false);
    }

    // Valida las credenciales ingresadas y navega al dashboard si son correctas
    private void iniciarSesion() {
        // Refuerza la validación campo a campo antes de tocar la base de datos
        boolean correoOk = validarCorreo();
        boolean contrasenaOk = validarContrasena();
        if (!correoOk || !contrasenaOk) {
            actualizarBotonIngresar();
            return;
        }

        String correo = campoCorreo.getText().trim();
        String contrasena = campoContrasena.getText().trim();

        // Busca el usuario en la base de datos por correo
        Usuario usuario = Usuario.buscarPorCorreo(correo);

        if (usuario == null) {
            mostrarErrorGeneral("No existe una cuenta con ese correo.");
            return;
        }

        // Verifica que la contraseña coincida
        if (!usuario.getContrasena().equals(contrasena)) {
            mostrarErrorGeneral("Contraseña incorrecta.");
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

    // Muestra un mensaje de error general (debajo de los botones) con estilo rojo
    private void mostrarErrorGeneral(String texto) {
        etiquetaEstado.setText(texto);
        etiquetaEstado.getStyleClass().removeAll("status-success", "status-error");
        etiquetaEstado.getStyleClass().add("status-error");
    }
}
