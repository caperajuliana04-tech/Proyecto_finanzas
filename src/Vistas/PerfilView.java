package Vistas;

import Modelos.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

// Vista que muestra y permite editar los datos del perfil del usuario autenticado.
// El saldo es de solo lectura porque lo calcula la app automáticamente con ingresos y gastos.
public class PerfilView extends BorderPane {

    private final Usuario usuario;      // Referencia al usuario autenticado (viene del login)
    private TextField campoNombre;      // Campo editable: nombre completo
    private TextField campoCorreo;      // Campo editable: correo electrónico
    private TextField campoTelefono;    // Campo editable: número de teléfono
    private TextField campoEdad;        // Campo editable: edad
    private TextField campoSaldo;       // Campo de solo lectura: muestra el saldo actual
    private Label etiquetaEstado;       // Muestra mensajes de éxito (verde) o error (rojo)

    // Constructor: recibe el usuario de sesión y arma la vista inmediatamente
    public PerfilView(Usuario usuario) {
        this.usuario = usuario;
        construirVista();
    }

    // Arma el layout completo: título + tarjeta de formulario, envuelta en ScrollPane
    private void construirVista() {
        VBox contenido = new VBox(20);
        contenido.getStyleClass().add("content-area");
        contenido.setPadding(new Insets(28));

        Label titulo = new Label("👤 Mi perfil");
        titulo.getStyleClass().add("content-title");

        VBox tarjeta = crearFormulario();

        contenido.getChildren().addAll(titulo, tarjeta);

        // ScrollPane permite scroll vertical si la ventana es pequeña
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        this.setCenter(scroll);
    }

    // Construye la tarjeta central con todos los campos del perfil y el botón guardar
    private VBox crearFormulario() {
        VBox tarjeta = new VBox(18);
        tarjeta.getStyleClass().add("content-card");
        tarjeta.setMaxWidth(700); // Limita el ancho para que no se vea estirado en pantallas grandes

        Label labelInfo = new Label("Información de tu cuenta");
        labelInfo.getStyleClass().add("field-label");
        labelInfo.setStyle("-fx-font-size: 15px;");

        // Muestra el ID y CC como texto informativo (no son editables porque son identificadores únicos)
        Label labelId = new Label("ID: " + usuario.getId() + "   |   CC: " + usuario.getCc());
        labelId.getStyleClass().add("subtitle-label");

        // Inicializa cada campo de texto con el valor actual del usuario
        campoNombre = new TextField(usuario.getNombre());
        campoNombre.getStyleClass().add("form-input");

        campoCorreo = new TextField(usuario.getCorreo());
        campoCorreo.getStyleClass().add("form-input");

        campoTelefono = new TextField(usuario.getNumeroTelefono());
        campoTelefono.getStyleClass().add("form-input");

        // La edad se convierte de int a String porque TextField trabaja con texto
        campoEdad = new TextField(String.valueOf(usuario.getEdad()));
        campoEdad.getStyleClass().add("form-input");

        // El saldo se muestra pero NO se puede editar: setEditable(false) lo bloquea
        // El fondo gris claro es una señal visual de que es solo lectura
        campoSaldo = new TextField(String.valueOf(usuario.getSaldoActual()));
        campoSaldo.setEditable(false);
        campoSaldo.getStyleClass().add("form-input");
        campoSaldo.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");

        // GridPane de 2 columnas para organizar los campos en pares
        GridPane grid = new GridPane();
        grid.setHgap(18); // Espacio horizontal entre columnas
        grid.setVgap(14); // Espacio vertical entre filas
        grid.add(crearGrupo("Nombre completo", campoNombre), 0, 0);
        grid.add(crearGrupo("Correo electrónico", campoCorreo), 1, 0);
        grid.add(crearGrupo("Teléfono", campoTelefono), 0, 1);
        grid.add(crearGrupo("Edad", campoEdad), 1, 1);
        // El campo saldo ocupa las 2 columnas (colspan=2) en la fila 2
        grid.add(crearGrupo("Saldo actual ($) — solo lectura", campoSaldo), 0, 2, 2, 1);

        // Cada columna crece por igual cuando se amplía la ventana
        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col);

        etiquetaEstado = new Label("");
        etiquetaEstado.getStyleClass().add("status-label");
        etiquetaEstado.setMaxWidth(Double.MAX_VALUE);

        Button botonGuardar = new Button("Guardar cambios");
        botonGuardar.getStyleClass().add("primary-button");
        botonGuardar.setOnAction(e -> guardarCambios());

        // HBox alinea el botón a la derecha de la tarjeta
        HBox botones = new HBox(botonGuardar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        tarjeta.getChildren().addAll(labelInfo, labelId, grid, botones, etiquetaEstado);
        return tarjeta;
    }

    // Agrupa una etiqueta descriptiva encima de su campo de entrada
    private VBox crearGrupo(String texto, javafx.scene.control.Control campo) {
        Label lbl = new Label(texto);
        lbl.getStyleClass().add("field-label");
        return new VBox(6, lbl, campo); // 6px de espacio entre la etiqueta y el campo
    }

    // Valida los campos y guarda los cambios en la base de datos si todo es correcto
    private void guardarCambios() {
        String nombre    = campoNombre.getText().trim();
        String correo    = campoCorreo.getText().trim();
        String telefono  = campoTelefono.getText().trim();
        String textoEdad = campoEdad.getText().trim();

        // Verifica que ningún campo obligatorio esté vacío
        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || textoEdad.isEmpty()) {
            mostrarMensaje("Completa todos los campos.", false);
            return;
        }

        // La edad debe ser un número entero positivo
        int edad;
        try {
            edad = Integer.parseInt(textoEdad);
            if (edad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarMensaje("La edad debe ser un número válido.", false);
            return;
        }

        // Actualiza el objeto usuario en memoria con los nuevos valores
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setNumeroTelefono(telefono);
        usuario.setEdad(edad);

        // Persiste los cambios en la base de datos SQLite
        usuario.actualizarUsuario();

        mostrarMensaje("Perfil actualizado correctamente.", true);
    }

    // Muestra un mensaje de éxito (verde) o error (rojo) usando clases CSS
    private void mostrarMensaje(String texto, boolean exito) {
        etiquetaEstado.setText(texto);
        // Limpia el estilo anterior antes de aplicar el nuevo para evitar acumulación
        etiquetaEstado.getStyleClass().removeAll("status-success", "status-error");
        etiquetaEstado.getStyleClass().add(exito ? "status-success" : "status-error");
    }
}
