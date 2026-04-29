package Vistas;

import Modelos.Usuario;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

// Vista para crear nuevos usuarios directamente desde la aplicación.
// Valida cada campo individualmente: rechaza caracteres inválidos al teclear
// (TextFormatter) y muestra error inline al perder foco si el formato falla.
// El botón "Registrar usuario" se habilita solo cuando todos los campos son válidos.
public class RegistroUsuarioView extends BorderPane {

    // Patrones de validación
    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");
    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[\\p{L} ]{2,}$");
    private static final Pattern PATRON_DIGITOS = Pattern.compile("^\\d*$");
    private static final Pattern PATRON_DECIMAL = Pattern.compile("^\\d*(\\.\\d*)?$");
    private static final int MIN_CONTRASENA = 4;
    private static final int MAX_EDAD = 120;

    // Campos del formulario
    private TextField campoId;
    private TextField campoNombre;
    private PasswordField campoContrasena;
    private TextField campoCorreo;
    private TextField campoTelefono;
    private TextField campoEdad;
    private TextField campoCc;
    private TextField campoSaldoActual;

    // Etiquetas de error inline (una por campo)
    private Label errorId;
    private Label errorNombre;
    private Label errorContrasena;
    private Label errorCorreo;
    private Label errorTelefono;
    private Label errorEdad;
    private Label errorCc;
    private Label errorSaldo;

    private Button botonRegistrar;
    private Label etiquetaEstado;
    private VBox tarjetaFormulario;

    public RegistroUsuarioView() {
        construirVista();
    }

    private void construirVista() {
        this.getStyleClass().add("app-root");

        StackPane fondoCentral = new StackPane();
        fondoCentral.getStyleClass().add("background-pane");
        fondoCentral.setPadding(new Insets(30));

        tarjetaFormulario = new VBox(22);
        tarjetaFormulario.getStyleClass().add("form-card");
        tarjetaFormulario.setMaxWidth(900);
        tarjetaFormulario.setPadding(new Insets(35));

        Label titulo = new Label("Registro de usuario");
        titulo.getStyleClass().add("title-label");

        Label subtitulo = new Label("Crea usuarios desde la aplicación desktop y guárdalos directamente en SQLite.");
        subtitulo.getStyleClass().add("subtitle-label");
        subtitulo.setWrapText(true);

        GridPane formulario = crearFormulario();
        HBox filaBotones = crearFilaBotones();

        etiquetaEstado = new Label("Completa el formulario y presiona Registrar usuario.");
        etiquetaEstado.getStyleClass().add("status-label");
        etiquetaEstado.setWrapText(true);

        tarjetaFormulario.getChildren().addAll(titulo, subtitulo, formulario, filaBotones, etiquetaEstado);
        fondoCentral.getChildren().add(tarjetaFormulario);

        ScrollPane scroll = new ScrollPane(fondoCentral);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        this.setCenter(scroll);

        configurarValidaciones();
        aplicarAnimacionEntrada(tarjetaFormulario);
    }

    // Construye el GridPane y crea los TextFormatter que rechazan caracteres no válidos al teclear
    private GridPane crearFormulario() {
        GridPane formulario = new GridPane();
        formulario.setHgap(18);
        formulario.setVgap(16);
        formulario.getStyleClass().add("form-grid");

        campoId          = crearCampoTexto("Ej: 1");
        campoNombre      = crearCampoTexto("Nombre completo");
        campoContrasena  = new PasswordField();
        campoContrasena.setPromptText("Contraseña (mínimo " + MIN_CONTRASENA + " caracteres)");
        campoContrasena.getStyleClass().add("form-input");
        campoCorreo      = crearCampoTexto("correo@ejemplo.com");
        campoTelefono    = crearCampoTexto("3001234567");
        campoEdad        = crearCampoTexto("Ej: 25");
        campoCc          = crearCampoTexto("Documento");
        campoSaldoActual = crearCampoTexto("Ej: 150000");

        // Rechazo en vivo: solo dígitos en ID, teléfono, edad, cédula
        campoId.setTextFormatter(crearFiltroEnteros());
        campoTelefono.setTextFormatter(crearFiltroEnteros());
        campoEdad.setTextFormatter(crearFiltroEnteros());
        campoCc.setTextFormatter(crearFiltroEnteros());
        // Saldo: dígitos + un punto decimal
        campoSaldoActual.setTextFormatter(crearFiltroDecimales());

        // Etiquetas de error inline (ocultas hasta que falle una validación)
        errorId         = crearEtiquetaError();
        errorNombre     = crearEtiquetaError();
        errorContrasena = crearEtiquetaError();
        errorCorreo     = crearEtiquetaError();
        errorTelefono   = crearEtiquetaError();
        errorEdad       = crearEtiquetaError();
        errorCc         = crearEtiquetaError();
        errorSaldo      = crearEtiquetaError();

        formulario.add(crearGrupoCampo("ID", campoId, errorId), 0, 0);
        formulario.add(crearGrupoCampo("Nombre", campoNombre, errorNombre), 1, 0);

        formulario.add(crearGrupoCampo("Contraseña", campoContrasena, errorContrasena), 0, 1);
        formulario.add(crearGrupoCampo("Correo", campoCorreo, errorCorreo), 1, 1);

        formulario.add(crearGrupoCampo("Teléfono", campoTelefono, errorTelefono), 0, 2);
        formulario.add(crearGrupoCampo("Edad", campoEdad, errorEdad), 1, 2);

        formulario.add(crearGrupoCampo("Cédula", campoCc, errorCc), 0, 3);
        formulario.add(crearGrupoCampo("Saldo actual", campoSaldoActual, errorSaldo), 1, 3);

        return formulario;
    }

    private HBox crearFilaBotones() {
        botonRegistrar = new Button("Registrar usuario");
        botonRegistrar.getStyleClass().add("primary-button");
        botonRegistrar.setOnAction(e -> registrarUsuarioDesdeFormulario());
        botonRegistrar.setDisable(true); // Bloqueado hasta que todos los campos sean válidos

        Button botonLimpiar = new Button("Limpiar");
        botonLimpiar.getStyleClass().add("secondary-button");
        botonLimpiar.setOnAction(e -> limpiarCampos());

        aplicarEfectoHover(botonRegistrar);
        aplicarEfectoHover(botonLimpiar);

        HBox filaBotones = new HBox(12, botonRegistrar, botonLimpiar);
        filaBotones.setAlignment(Pos.CENTER_RIGHT);

        return filaBotones;
    }

    // Engancha listeners para validar al perder foco y para evaluar el botón al cambiar texto
    private void configurarValidaciones() {
        engancharCampo(campoId, errorId, this::validarId);
        engancharCampo(campoNombre, errorNombre, this::validarNombre);
        engancharCampo(campoContrasena, errorContrasena, this::validarContrasena);
        engancharCampo(campoCorreo, errorCorreo, this::validarCorreo);
        engancharCampo(campoTelefono, errorTelefono, this::validarTelefono);
        engancharCampo(campoEdad, errorEdad, this::validarEdad);
        engancharCampo(campoCc, errorCc, this::validarCc);
        engancharCampo(campoSaldoActual, errorSaldo, this::validarSaldo);
    }

    // Conecta un campo a su validador: limpia error mientras escribe + valida al perder foco
    private void engancharCampo(javafx.scene.control.TextInputControl campo, Label etiquetaError,
                                java.util.function.Supplier<Boolean> validador) {
        campo.textProperty().addListener((obs, anterior, nuevo) -> {
            ocultarError(campo, etiquetaError);
            actualizarBoton();
        });
        campo.focusedProperty().addListener((obs, antes, ahora) -> {
            if (!ahora) validador.get();
        });
    }

    // ------------------ Validadores por campo ------------------

    private boolean validarId() {
        String texto = campoId.getText().trim();
        if (texto.isEmpty()) return error(campoId, errorId, "El ID es obligatorio.");
        try {
            int valor = Integer.parseInt(texto);
            if (valor < 1) return error(campoId, errorId, "El ID debe ser mayor o igual a 1.");
        } catch (NumberFormatException e) {
            return error(campoId, errorId, "ID inválido.");
        }
        ocultarError(campoId, errorId);
        return true;
    }

    private boolean validarNombre() {
        String texto = campoNombre.getText().trim();
        if (texto.isEmpty()) return error(campoNombre, errorNombre, "El nombre es obligatorio.");
        if (!PATRON_NOMBRE.matcher(texto).matches())
            return error(campoNombre, errorNombre, "Solo letras y espacios (mínimo 2 caracteres).");
        ocultarError(campoNombre, errorNombre);
        return true;
    }

    private boolean validarContrasena() {
        String texto = campoContrasena.getText();
        if (texto.isEmpty()) return error(campoContrasena, errorContrasena, "La contraseña es obligatoria.");
        if (texto.length() < MIN_CONTRASENA)
            return error(campoContrasena, errorContrasena, "Mínimo " + MIN_CONTRASENA + " caracteres.");
        ocultarError(campoContrasena, errorContrasena);
        return true;
    }

    private boolean validarCorreo() {
        String texto = campoCorreo.getText().trim();
        if (texto.isEmpty()) return error(campoCorreo, errorCorreo, "El correo es obligatorio.");
        if (!PATRON_CORREO.matcher(texto).matches())
            return error(campoCorreo, errorCorreo, "Formato inválido (ej: nombre@dominio.com).");
        ocultarError(campoCorreo, errorCorreo);
        return true;
    }

    private boolean validarTelefono() {
        String texto = campoTelefono.getText().trim();
        if (texto.isEmpty()) return error(campoTelefono, errorTelefono, "El teléfono es obligatorio.");
        ocultarError(campoTelefono, errorTelefono);
        return true;
    }

    private boolean validarEdad() {
        String texto = campoEdad.getText().trim();
        if (texto.isEmpty()) return error(campoEdad, errorEdad, "La edad es obligatoria.");
        try {
            int valor = Integer.parseInt(texto);
            if (valor < 1 || valor > MAX_EDAD)
                return error(campoEdad, errorEdad, "Edad debe estar entre 1 y " + MAX_EDAD + ".");
        } catch (NumberFormatException e) {
            return error(campoEdad, errorEdad, "Edad inválida.");
        }
        ocultarError(campoEdad, errorEdad);
        return true;
    }

    private boolean validarCc() {
        String texto = campoCc.getText().trim();
        if (texto.isEmpty()) return error(campoCc, errorCc, "La cédula es obligatoria.");
        ocultarError(campoCc, errorCc);
        return true;
    }

    private boolean validarSaldo() {
        String texto = campoSaldoActual.getText().trim();
        if (texto.isEmpty()) return error(campoSaldoActual, errorSaldo, "El saldo es obligatorio.");
        if (texto.equals(".")) return error(campoSaldoActual, errorSaldo, "Saldo inválido.");
        try {
            double valor = Double.parseDouble(texto);
            if (valor < 0) return error(campoSaldoActual, errorSaldo, "El saldo no puede ser negativo.");
        } catch (NumberFormatException e) {
            return error(campoSaldoActual, errorSaldo, "Saldo inválido.");
        }
        ocultarError(campoSaldoActual, errorSaldo);
        return true;
    }

    // Devuelve true si todos los campos pasan validación silenciosa (sin mostrar errores)
    private boolean todosLosCamposValidos() {
        return idEsValido()
            && PATRON_NOMBRE.matcher(campoNombre.getText().trim()).matches()
            && campoContrasena.getText().length() >= MIN_CONTRASENA
            && PATRON_CORREO.matcher(campoCorreo.getText().trim()).matches()
            && !campoTelefono.getText().trim().isEmpty()
            && edadEsValida()
            && !campoCc.getText().trim().isEmpty()
            && saldoEsValido();
    }

    private boolean idEsValido() {
        try {
            return Integer.parseInt(campoId.getText().trim()) >= 1;
        } catch (NumberFormatException e) { return false; }
    }

    private boolean edadEsValida() {
        try {
            int v = Integer.parseInt(campoEdad.getText().trim());
            return v >= 1 && v <= MAX_EDAD;
        } catch (NumberFormatException e) { return false; }
    }

    private boolean saldoEsValido() {
        String t = campoSaldoActual.getText().trim();
        if (t.isEmpty() || t.equals(".")) return false;
        try { return Double.parseDouble(t) >= 0; }
        catch (NumberFormatException e) { return false; }
    }

    private void actualizarBoton() {
        botonRegistrar.setDisable(!todosLosCamposValidos());
    }

    // ------------------ Persistencia ------------------

    private void registrarUsuarioDesdeFormulario() {
        // Re-valida todo (defensivo): pinta errores en cualquier campo que falle
        boolean ok = validarId() & validarNombre() & validarContrasena() & validarCorreo()
                   & validarTelefono() & validarEdad() & validarCc() & validarSaldo();
        if (!ok) {
            mostrarMensaje("Corrige los campos marcados en rojo.", false);
            actualizarBoton();
            return;
        }

        try {
            int id           = Integer.parseInt(campoId.getText().trim());
            int edad         = Integer.parseInt(campoEdad.getText().trim());
            double saldo     = Double.parseDouble(campoSaldoActual.getText().trim());
            String nombre    = campoNombre.getText().trim();
            String contrasena = campoContrasena.getText();
            String correo    = campoCorreo.getText().trim();
            String telefono  = campoTelefono.getText().trim();
            String cc        = campoCc.getText().trim();

            // Verifica que no exista ya un usuario con ese mismo ID
            if (Usuario.leerPerfil(id) != null) {
                error(campoId, errorId, "Ya existe un usuario con ese ID.");
                mostrarMensaje("Ya existe un usuario con ese ID.", false);
                actualizarBoton();
                return;
            }

            Usuario nuevoUsuario = new Usuario(id, nombre, contrasena, correo, telefono, edad, cc, saldo);
            nuevoUsuario.registrarUsuario();

            if (Usuario.leerPerfil(id) != null) {
                mostrarMensaje("Usuario registrado correctamente en la base de datos.", true);
                limpiarCamposInterno(false);
            } else {
                mostrarMensaje("No se pudo confirmar el registro del usuario.", false);
            }
        } catch (Exception e) {
            mostrarMensaje("Ocurrió un error al procesar el formulario: " + e.getMessage(), false);
        }
    }

    private void limpiarCampos() {
        limpiarCamposInterno(true);
    }

    private void limpiarCamposInterno(boolean devolverMensaje) {
        campoId.clear();
        campoNombre.clear();
        campoContrasena.clear();
        campoCorreo.clear();
        campoTelefono.clear();
        campoEdad.clear();
        campoCc.clear();
        campoSaldoActual.clear();
        // Limpia los errores visuales también
        ocultarError(campoId, errorId);
        ocultarError(campoNombre, errorNombre);
        ocultarError(campoContrasena, errorContrasena);
        ocultarError(campoCorreo, errorCorreo);
        ocultarError(campoTelefono, errorTelefono);
        ocultarError(campoEdad, errorEdad);
        ocultarError(campoCc, errorCc);
        ocultarError(campoSaldoActual, errorSaldo);
        actualizarBoton();
        campoId.requestFocus();

        if (devolverMensaje) {
            mostrarMensaje("Formulario limpiado.", true);
        }
    }

    // ------------------ Helpers de UI ------------------

    private VBox crearGrupoCampo(String textoLabel, Control campo, Label etiquetaError) {
        Label etiqueta = new Label(textoLabel);
        etiqueta.getStyleClass().add("field-label");
        VBox grupo = new VBox(6, etiqueta, campo, etiquetaError);
        VBox.setVgrow(campo, Priority.NEVER);
        return grupo;
    }

    private TextField crearCampoTexto(String placeholder) {
        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.getStyleClass().add("form-input");
        return campo;
    }

    private Label crearEtiquetaError() {
        Label etiqueta = new Label("");
        etiqueta.getStyleClass().add("field-error");
        etiqueta.setVisible(false);
        etiqueta.setManaged(false);
        return etiqueta;
    }

    // TextFormatter que rechaza cualquier carácter que no sea dígito
    private TextFormatter<String> crearFiltroEnteros() {
        UnaryOperator<TextFormatter.Change> filtro = cambio ->
            PATRON_DIGITOS.matcher(cambio.getControlNewText()).matches() ? cambio : null;
        return new TextFormatter<>(filtro);
    }

    // TextFormatter que permite dígitos opcionalmente seguidos por un punto y más dígitos
    private TextFormatter<String> crearFiltroDecimales() {
        UnaryOperator<TextFormatter.Change> filtro = cambio ->
            PATRON_DECIMAL.matcher(cambio.getControlNewText()).matches() ? cambio : null;
        return new TextFormatter<>(filtro);
    }

    // Marca el campo en rojo y muestra el mensaje inline. Devuelve false para encadenar en validadores.
    private boolean error(Control campo, Label etiquetaError, String mensaje) {
        if (!campo.getStyleClass().contains("form-input-error")) {
            campo.getStyleClass().add("form-input-error");
        }
        etiquetaError.setText(mensaje);
        etiquetaError.setVisible(true);
        etiquetaError.setManaged(true);
        return false;
    }

    private void ocultarError(Control campo, Label etiquetaError) {
        campo.getStyleClass().remove("form-input-error");
        etiquetaError.setVisible(false);
        etiquetaError.setManaged(false);
    }

    private void mostrarMensaje(String texto, boolean exito) {
        etiquetaEstado.setText(texto);
        etiquetaEstado.getStyleClass().removeAll("status-success", "status-error");
        etiquetaEstado.getStyleClass().add(exito ? "status-success" : "status-error");
    }

    private void aplicarAnimacionEntrada(Node nodo) {
        FadeTransition fade = new FadeTransition(Duration.millis(700), nodo);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition desplazamiento = new TranslateTransition(Duration.millis(700), nodo);
        desplazamiento.setFromY(25);
        desplazamiento.setToY(0);

        fade.play();
        desplazamiento.play();
    }

    private void aplicarEfectoHover(Button boton) {
        ScaleTransition crecer = new ScaleTransition(Duration.millis(140), boton);
        crecer.setToX(1.03);
        crecer.setToY(1.03);

        ScaleTransition normal = new ScaleTransition(Duration.millis(140), boton);
        normal.setToX(1.0);
        normal.setToY(1.0);

        boton.setOnMouseEntered(e -> crecer.playFromStart());
        boton.setOnMouseExited(e -> normal.playFromStart());
    }
}
