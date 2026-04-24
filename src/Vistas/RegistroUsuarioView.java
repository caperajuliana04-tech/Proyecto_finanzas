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

// Vista para crear nuevos usuarios directamente desde la aplicación.
// Valida todos los campos, verifica que el ID no esté duplicado y guarda en SQLite.
public class RegistroUsuarioView extends BorderPane {

    // Campos del formulario: uno por cada atributo del usuario
    private TextField campoId;          // ID numérico del usuario (clave primaria en la DB)
    private TextField campoNombre;      // Nombre completo
    private PasswordField campoContrasena; // Contraseña (oculta los caracteres al escribir)
    private TextField campoCorreo;      // Correo electrónico (usado para iniciar sesión)
    private TextField campoTelefono;    // Número de teléfono
    private TextField campoEdad;        // Edad (debe ser número positivo)
    private TextField campoCc;          // Cédula o documento de identidad
    private TextField campoSaldoActual; // Saldo inicial con el que arranca el usuario

    private Label etiquetaEstado;       // Muestra mensajes de éxito o error al usuario
    private VBox tarjetaFormulario;     // Tarjeta visual que contiene todo el formulario

    // Constructor sin parámetros: esta vista no necesita un usuario previo (es para crearlos)
    public RegistroUsuarioView() {
        construirVista();
    }

    // Arma el layout completo: fondo → tarjeta → ScrollPane
    private void construirVista() {
        this.getStyleClass().add("app-root");

        // StackPane actúa como fondo con padding, centra la tarjeta
        StackPane fondoCentral = new StackPane();
        fondoCentral.getStyleClass().add("background-pane");
        fondoCentral.setPadding(new Insets(30));

        tarjetaFormulario = new VBox(22);
        tarjetaFormulario.getStyleClass().add("form-card");
        tarjetaFormulario.setMaxWidth(900); // Limita el ancho máximo de la tarjeta
        tarjetaFormulario.setPadding(new Insets(35));

        Label titulo = new Label("Registro de usuario");
        titulo.getStyleClass().add("title-label");

        Label subtitulo = new Label("Crea usuarios desde la aplicación desktop y guárdalos directamente en SQLite.");
        subtitulo.getStyleClass().add("subtitle-label");
        subtitulo.setWrapText(true); // Permite que el texto haga salto de línea si no cabe

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

        // ScrollPane permite scroll en pantallas pequeñas; setFitToHeight evita espacios vacíos
        ScrollPane scroll = new ScrollPane(fondoCentral);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true); // El usuario puede arrastrar el contenido con el mouse
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        this.setCenter(scroll);

        // Anima la tarjeta al abrir la vista (fade + deslizamiento desde abajo)
        aplicarAnimacionEntrada(tarjetaFormulario);
    }

    // Crea el GridPane de 2 columnas con los 8 campos del formulario
    private GridPane crearFormulario() {
        GridPane formulario = new GridPane();
        formulario.setHgap(18); // Espacio horizontal entre columnas
        formulario.setVgap(16); // Espacio vertical entre filas
        formulario.getStyleClass().add("form-grid");

        // Crea todos los campos usando el helper crearCampoTexto()
        campoId           = crearCampoTexto("Ej: 1");
        campoNombre       = crearCampoTexto("Nombre completo");
        // PasswordField enmascara los caracteres con puntos mientras el usuario escribe
        campoContrasena   = new PasswordField();
        campoContrasena.setPromptText("Contraseña");
        campoContrasena.getStyleClass().add("form-input");
        campoCorreo       = crearCampoTexto("correo@ejemplo.com");
        campoTelefono     = crearCampoTexto("3001234567");
        campoEdad         = crearCampoTexto("Ej: 25");
        campoCc           = crearCampoTexto("Documento");
        campoSaldoActual  = crearCampoTexto("Ej: 150000");

        // Organiza los campos en 4 filas × 2 columnas
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

    // Crea la fila con los botones "Registrar usuario" y "Limpiar", con efecto hover
    private HBox crearFilaBotones() {
        Button botonRegistrar = new Button("Registrar usuario");
        botonRegistrar.getStyleClass().add("primary-button");
        botonRegistrar.setOnAction(e -> registrarUsuarioDesdeFormulario());

        Button botonLimpiar = new Button("Limpiar");
        botonLimpiar.getStyleClass().add("secondary-button");
        botonLimpiar.setOnAction(e -> limpiarCampos());

        // Agrega un efecto de escala suave al pasar el mouse sobre los botones
        aplicarEfectoHover(botonRegistrar);
        aplicarEfectoHover(botonLimpiar);

        HBox filaBotones = new HBox(12, botonRegistrar, botonLimpiar);
        filaBotones.setAlignment(Pos.CENTER_RIGHT); // Los botones van pegados a la derecha

        return filaBotones;
    }

    // Agrupa la etiqueta del campo encima del campo de entrada
    private VBox crearGrupoCampo(String textoLabel, Control campo) {
        Label etiqueta = new Label(textoLabel);
        etiqueta.getStyleClass().add("field-label");

        VBox grupo = new VBox(8, etiqueta, campo);
        VBox.setVgrow(campo, Priority.NEVER); // El campo no crece verticalmente de más

        return grupo;
    }

    // Crea un TextField estilizado con el placeholder indicado
    private TextField crearCampoTexto(String placeholder) {
        TextField campo = new TextField();
        campo.setPromptText(placeholder); // Texto gris que aparece cuando el campo está vacío
        campo.getStyleClass().add("form-input");
        return campo;
    }

    // Valida y registra el usuario en la base de datos SQLite
    private void registrarUsuarioDesdeFormulario() {
        // Lee y limpia los valores de todos los campos
        String textoId   = campoId.getText().trim();
        String nombre    = campoNombre.getText().trim();
        String contrasena = campoContrasena.getText().trim();
        String correo    = campoCorreo.getText().trim();
        String telefono  = campoTelefono.getText().trim();
        String textoEdad = campoEdad.getText().trim();
        String cc        = campoCc.getText().trim();
        String textoSaldo = campoSaldoActual.getText().trim();

        // Valida que ningún campo esté vacío antes de continuar
        if (textoId.isEmpty() || nombre.isEmpty() || contrasena.isEmpty() || correo.isEmpty()
                || telefono.isEmpty() || textoEdad.isEmpty() || cc.isEmpty() || textoSaldo.isEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios.", false);
            return;
        }

        try {
            // Convierte los campos numéricos; lanza NumberFormatException si no son números
            int id           = Integer.parseInt(textoId);
            int edad         = Integer.parseInt(textoEdad);
            double saldoActual = Double.parseDouble(textoSaldo);

            // Validaciones de rango
            if (edad <= 0) {
                mostrarMensaje("La edad debe ser mayor que cero.", false);
                return;
            }
            if (saldoActual < 0) {
                mostrarMensaje("El saldo actual no puede ser negativo.", false);
                return;
            }

            // Verifica que no exista ya un usuario con ese mismo ID en la base de datos
            Usuario usuarioExistente = Usuario.leerPerfil(id);
            if (usuarioExistente != null) {
                mostrarMensaje("Ya existe un usuario con ese ID.", false);
                return;
            }

            // Crea el objeto con todos los datos del formulario y lo guarda en SQLite
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

            // Confirma el registro leyendo el usuario recién guardado desde la DB
            // Si leerPerfil devuelve null, algo falló internamente al guardar
            Usuario usuarioGuardado = Usuario.leerPerfil(id);
            if (usuarioGuardado != null) {
                mostrarMensaje("Usuario registrado correctamente en la base de datos.", true);
                limpiarCamposInterno(false); // Limpia sin sobrescribir el mensaje de éxito
            } else {
                mostrarMensaje("No se pudo confirmar el registro del usuario.", false);
            }

        } catch (NumberFormatException e) {
            // Cualquiera de los tres parseInt/parseDouble puede lanzar esto
            mostrarMensaje("ID, edad y saldo actual deben ser números válidos.", false);
        } catch (Exception e) {
            // Captura errores inesperados (ej: DB bloqueada, error de red)
            mostrarMensaje("Ocurrió un error al procesar el formulario: " + e.getMessage(), false);
        }
    }

    // Limpia todos los campos y muestra un mensaje de confirmación
    private void limpiarCampos() {
        limpiarCamposInterno(true);
    }

    // Limpia todos los campos de texto; si devolverMensaje=true también actualiza la etiqueta
    // Se separa en dos métodos para poder limpiar después de un registro exitoso sin sobreescribir
    // el mensaje de éxito con "Formulario limpiado."
    private void limpiarCamposInterno(boolean devolverMensaje) {
        campoId.clear();
        campoNombre.clear();
        campoContrasena.clear();
        campoCorreo.clear();
        campoTelefono.clear();
        campoEdad.clear();
        campoCc.clear();
        campoSaldoActual.clear();
        campoId.requestFocus(); // Devuelve el foco al primer campo para facilitar el siguiente registro

        if (devolverMensaje) {
            mostrarMensaje("Formulario limpiado.", true);
        }
    }

    // Cambia el texto y el color de la etiqueta de estado según éxito o error
    private void mostrarMensaje(String texto, boolean exito) {
        etiquetaEstado.setText(texto);
        // Elimina la clase de color anterior para que no se acumulen dos colores
        etiquetaEstado.getStyleClass().removeAll("status-success", "status-error");

        if (exito) {
            etiquetaEstado.getStyleClass().add("status-success"); // Verde
        } else {
            etiquetaEstado.getStyleClass().add("status-error");   // Rojo
        }
    }

    // Anima la aparición de un nodo: se desvanece desde transparente y sube desde abajo
    // Ambas animaciones corren en paralelo sobre el mismo nodo durante 700 ms
    private void aplicarAnimacionEntrada(Node nodo) {
        FadeTransition fade = new FadeTransition(Duration.millis(700), nodo);
        fade.setFromValue(0); // Empieza completamente transparente
        fade.setToValue(1);   // Termina completamente visible

        TranslateTransition desplazamiento = new TranslateTransition(Duration.millis(700), nodo);
        desplazamiento.setFromY(25); // Empieza 25px más abajo de su posición final
        desplazamiento.setToY(0);    // Termina en su posición natural

        fade.play();
        desplazamiento.play();
    }

    // Aplica un efecto de escala suave al botón cuando el cursor entra y sale
    // ScaleTransition(1.03) agranda ligeramente el botón para dar feedback visual
    private void aplicarEfectoHover(Button boton) {
        ScaleTransition crecer = new ScaleTransition(Duration.millis(140), boton);
        crecer.setToX(1.03); // 3% más grande en horizontal
        crecer.setToY(1.03); // 3% más grande en vertical

        ScaleTransition normal = new ScaleTransition(Duration.millis(140), boton);
        normal.setToX(1.0); // Regresa al tamaño original
        normal.setToY(1.0);

        boton.setOnMouseEntered(e -> crecer.playFromStart()); // Agranda al entrar
        boton.setOnMouseExited(e -> normal.playFromStart());  // Vuelve a normal al salir
    }
}
