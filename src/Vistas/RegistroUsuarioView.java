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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class RegistroUsuarioView extends BorderPane {

    private TextField campoId;
    private TextField campoNombre;
    private PasswordField campoContrasena;
    private TextField campoCorreo;
    private TextField campoTelefono;
    private TextField campoEdad;
    private TextField campoCc;
    private TextField campoSaldoActual;

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

        tarjetaFormulario.getChildren().addAll(
            titulo,
            subtitulo,
            formulario,
            filaBotones,
            etiquetaEstado
        );

        fondoCentral.getChildren().add(tarjetaFormulario);

        ScrollPane scroll = new ScrollPane(fondoCentral);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        this.setCenter(scroll);

        aplicarAnimacionEntrada(tarjetaFormulario);
    }

    private GridPane crearFormulario() {
        GridPane formulario = new GridPane();
        formulario.setHgap(18);
        formulario.setVgap(16);
        formulario.getStyleClass().add("form-grid");

        campoId = crearCampoTexto("Ej: 1");
        campoNombre = crearCampoTexto("Nombre completo");
        campoContrasena = new PasswordField();
        campoContrasena.setPromptText("Contraseña");
        campoContrasena.getStyleClass().add("form-input");

        campoCorreo = crearCampoTexto("correo@ejemplo.com");
        campoTelefono = crearCampoTexto("3001234567");
        campoEdad = crearCampoTexto("Ej: 25");
        campoCc = crearCampoTexto("Documento");
        campoSaldoActual = crearCampoTexto("Ej: 150000");

        formulario.add(crearGrupoCampo("ID", campoId), 0, 0);
        formulario.add(crearGrupoCampo("Nombre", campoNombre), 1, 0);

        formulario.add(crearGrupoCampo("Contraseña", campoContrasena), 0, 1);
        formulario.add(crearGrupoCampo("Correo", campoCorreo), 1, 1);

        formulario.add(crearGrupoCampo("Teléfono", campoTelefono), 0, 2);
        formulario.add(crearGrupoCampo("Edad", campoEdad), 1, 2);

        formulario.add(crearGrupoCampo("Cédula", campoCc), 0, 3);
        formulario.add(crearGrupoCampo("Saldo actual", campoSaldoActual), 1, 3);

        return formulario;
    }

    private HBox crearFilaBotones() {
        Button botonRegistrar = new Button("Registrar usuario");
        botonRegistrar.getStyleClass().add("primary-button");
        botonRegistrar.setOnAction(e -> registrarUsuarioDesdeFormulario());

        Button botonLimpiar = new Button("Limpiar");
        botonLimpiar.getStyleClass().add("secondary-button");
        botonLimpiar.setOnAction(e -> limpiarCampos());

        aplicarEfectoHover(botonRegistrar);
        aplicarEfectoHover(botonLimpiar);

        HBox filaBotones = new HBox(12, botonRegistrar, botonLimpiar);
        filaBotones.setAlignment(Pos.CENTER_RIGHT);

        return filaBotones;
    }

    private VBox crearGrupoCampo(String textoLabel, Control campo) {
        Label etiqueta = new Label(textoLabel);
        etiqueta.getStyleClass().add("field-label");

        VBox grupo = new VBox(8, etiqueta, campo);
        VBox.setVgrow(campo, Priority.NEVER);

        return grupo;
    }

    private TextField crearCampoTexto(String placeholder) {
        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.getStyleClass().add("form-input");
        return campo;
    }

    private void registrarUsuarioDesdeFormulario() {
        String textoId = campoId.getText().trim();
        String nombre = campoNombre.getText().trim();
        String contrasena = campoContrasena.getText().trim();
        String correo = campoCorreo.getText().trim();
        String telefono = campoTelefono.getText().trim();
        String textoEdad = campoEdad.getText().trim();
        String cc = campoCc.getText().trim();
        String textoSaldo = campoSaldoActual.getText().trim();

        if (textoId.isEmpty() || nombre.isEmpty() || contrasena.isEmpty() || correo.isEmpty()
                || telefono.isEmpty() || textoEdad.isEmpty() || cc.isEmpty() || textoSaldo.isEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios.", false);
            return;
        }

        try {
            int id = Integer.parseInt(textoId);
            int edad = Integer.parseInt(textoEdad);
            double saldoActual = Double.parseDouble(textoSaldo);

            if (edad <= 0) {
                mostrarMensaje("La edad debe ser mayor que cero.", false);
                return;
            }

            if (saldoActual < 0) {
                mostrarMensaje("El saldo actual no puede ser negativo.", false);
                return;
            }

            Usuario usuarioExistente = Usuario.leerPerfil(id);
            if (usuarioExistente != null) {
                mostrarMensaje("Ya existe un usuario con ese ID.", false);
                return;
            }

            Usuario nuevoUsuario = new Usuario(
                id,
                nombre,
                contrasena,
                correo,
                telefono,
                edad,
                cc,
                saldoActual
            );

            nuevoUsuario.registrarUsuario();

            Usuario usuarioGuardado = Usuario.leerPerfil(id);

            if (usuarioGuardado != null) {
                mostrarMensaje("Usuario registrado correctamente en la base de datos.", true);
                limpiarCamposInterno(false);
            } else {
                mostrarMensaje("No se pudo confirmar el registro del usuario.", false);
            }

        } catch (NumberFormatException e) {
            mostrarMensaje("ID, edad y saldo actual deben ser números válidos.", false);
        } catch (Exception e) {
            mostrarMensaje("Ocurrió un error al procesar el formulario: " + e.getMessage(), false);
        }
    }

    private void limpiarCampos() {
        limpiarCamposInterno(true);
        mostrarMensaje("Formulario limpiado.", true);
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
        campoId.requestFocus();

        if (devolverMensaje) {
            mostrarMensaje("Formulario limpiado.", true);
        }
    }

    private void mostrarMensaje(String texto, boolean exito) {
        etiquetaEstado.setText(texto);
        etiquetaEstado.getStyleClass().removeAll("status-success", "status-error");

        if (exito) {
            etiquetaEstado.getStyleClass().add("status-success");
        } else {
            etiquetaEstado.getStyleClass().add("status-error");
        }
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