package Vistas;

import Modelos.Alerta;
import Modelos.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

// Vista para crear y gestionar alertas o recordatorios del usuario.
// Permite marcar alertas como leídas o eliminarlas desde la tabla.
public class AlertasView extends BorderPane {

    private final Usuario usuario;  // Usuario autenticado
    private TextField campoTipo;    // Tipo de alerta (ej: "Presupuesto", "Recordatorio")
    private TextField campoMensaje; // Texto del mensaje de la alerta
    private DatePicker campoFecha;  // Fecha asociada a la alerta
    private Label etiquetaEstado;   // Mensajes de éxito o error
    private TableView<Alerta> tabla; // Tabla con todas las alertas del usuario

    public AlertasView(Usuario usuario) {
        this.usuario = usuario;
        construirVista();
    }

    // Arma el layout: título, formulario y tabla dentro de un ScrollPane
    private void construirVista() {
        VBox contenido = new VBox(20);
        contenido.getStyleClass().add("content-area");
        contenido.setPadding(new Insets(28));

        Label titulo = new Label("🔔 Alertas");
        titulo.getStyleClass().add("content-title");

        VBox formulario = crearFormulario();
        tabla = crearTabla();
        cargarDatos();

        contenido.getChildren().addAll(titulo, formulario, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        this.setCenter(scroll);
    }

    // Construye el formulario para crear una nueva alerta
    private VBox crearFormulario() {
        VBox formulario = new VBox(14);
        formulario.getStyleClass().add("content-card");

        Label labelForm = new Label("Crear nueva alerta");
        labelForm.getStyleClass().add("field-label");
        labelForm.setStyle("-fx-font-size: 15px;");

        campoTipo = new TextField();
        campoTipo.setPromptText("Tipo (ej: Presupuesto, Recordatorio)");
        campoTipo.getStyleClass().add("form-input");

        campoMensaje = new TextField();
        campoMensaje.setPromptText("Mensaje de la alerta");
        campoMensaje.getStyleClass().add("form-input");

        campoFecha = new DatePicker(LocalDate.now());
        campoFecha.getStyleClass().add("form-input");

        // GridPane: tipo y fecha en la primera fila, mensaje en la segunda (ocupa 2 columnas)
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.add(crearGrupo("Tipo", campoTipo), 0, 0);
        grid.add(crearGrupo("Fecha", campoFecha), 1, 0);
        grid.add(crearGrupo("Mensaje", campoMensaje), 0, 1, 2, 1); // colspan=2: ocupa ambas columnas

        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col);

        etiquetaEstado = new Label("");
        etiquetaEstado.getStyleClass().add("status-label");
        etiquetaEstado.setMaxWidth(Double.MAX_VALUE);

        Button botonCrear = new Button("Crear alerta");
        botonCrear.getStyleClass().add("primary-button");
        botonCrear.setOnAction(e -> crearAlerta());

        Button botonLimpiar = new Button("Limpiar");
        botonLimpiar.getStyleClass().add("secondary-button");
        botonLimpiar.setOnAction(e -> limpiar());

        HBox botones = new HBox(10, botonCrear, botonLimpiar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        formulario.getChildren().addAll(labelForm, grid, botones, etiquetaEstado);
        return formulario;
    }

    // Agrupa etiqueta encima del campo de entrada
    private VBox crearGrupo(String texto, javafx.scene.control.Control campo) {
        Label lbl = new Label(texto);
        lbl.getStyleClass().add("field-label");
        return new VBox(6, lbl, campo);
    }

    // Tabla con columnas: tipo, mensaje, fecha, estado y dos botones de acción
    private TableView<Alerta> crearTabla() {
        TableView<Alerta> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Alerta, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));

        TableColumn<Alerta, String> colMensaje = new TableColumn<>("Mensaje");
        colMensaje.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMensaje()));

        TableColumn<Alerta, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));

        // Columna Estado: muestra "Leída" o "Pendiente" según el campo booleano
        TableColumn<Alerta, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isLeida() ? "Leída" : "Pendiente"));

        // Columna con dos botones: "Marcar leída" y "Eliminar"
        TableColumn<Alerta, Void> colAccion = new TableColumn<>("Acciones");
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnLeida    = new Button("Marcar leída");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox botones = new HBox(6, btnLeida, btnEliminar);
            {
                btnLeida.getStyleClass().add("small-button");
                btnEliminar.getStyleClass().add("danger-button");

                // Marca la alerta como leída en la DB y recarga la tabla
                btnLeida.setOnAction(e -> {
                    Alerta alerta = getTableView().getItems().get(getIndex());
                    alerta.marcarComoLeida();
                    cargarDatos();
                });
                // Elimina la alerta de la DB y recarga la tabla
                btnEliminar.setOnAction(e -> {
                    Alerta alerta = getTableView().getItems().get(getIndex());
                    Alerta.eliminarAlerta(alerta.getIdAlerta());
                    cargarDatos();
                });
                botones.setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : botones);
            }
        });

        tv.getColumns().addAll(colTipo, colMensaje, colFecha, colEstado, colAccion);
        tv.setMinHeight(300);
        return tv;
    }

    // Recarga la lista de alertas desde la base de datos
    private void cargarDatos() {
        List<Alerta> lista = Alerta.listarAlertasPorUsuario(usuario.getId());
        ObservableList<Alerta> datos = FXCollections.observableArrayList(lista);
        tabla.setItems(datos);
    }

    // Valida el formulario y crea la alerta si todos los campos están llenos
    private void crearAlerta() {
        String tipo    = campoTipo.getText().trim();
        String mensaje = campoMensaje.getText().trim();
        LocalDate fecha = campoFecha.getValue();

        if (tipo.isEmpty() || mensaje.isEmpty() || fecha == null) {
            mostrarMensaje("Completa todos los campos.", false);
            return;
        }

        // leida=false: la alerta recién creada siempre empieza como pendiente
        Alerta alerta = new Alerta(0, tipo, mensaje, fecha, false, usuario.getId());
        alerta.crearAlerta();
        mostrarMensaje("Alerta creada correctamente.", true);
        limpiar();
        cargarDatos();
    }

    // Limpia los campos del formulario
    private void limpiar() {
        campoTipo.clear();
        campoMensaje.clear();
        campoFecha.setValue(LocalDate.now());
    }

    // Muestra mensaje de éxito (verde) o error (rojo)
    private void mostrarMensaje(String texto, boolean exito) {
        etiquetaEstado.setText(texto);
        etiquetaEstado.getStyleClass().removeAll("status-success", "status-error");
        etiquetaEstado.getStyleClass().add(exito ? "status-success" : "status-error");
    }
}
