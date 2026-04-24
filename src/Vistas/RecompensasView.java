package Vistas;

import Modelos.Recompensa;
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

// Vista para registrar y visualizar las recompensas o logros del usuario.
// Funciona igual que AlertasView pero sobre la tabla "recompensa".
public class RecompensasView extends BorderPane {

    private final Usuario usuario;    // Usuario autenticado
    private TextField campoTipo;      // Tipo de recompensa (ej: "Medalla", "Insignia")
    private TextField campoMensaje;   // Descripción del logro
    private DatePicker campoFecha;    // Fecha de desbloqueo de la recompensa
    private Label etiquetaEstado;     // Mensajes de éxito o error
    private TableView<Recompensa> tabla; // Tabla con todas las recompensas del usuario

    public RecompensasView(Usuario usuario) {
        this.usuario = usuario;
        construirVista();
    }

    // Arma el layout: título, formulario y tabla dentro de un ScrollPane
    private void construirVista() {
        VBox contenido = new VBox(20);
        contenido.getStyleClass().add("content-area");
        contenido.setPadding(new Insets(28));

        Label titulo = new Label("🏆 Recompensas");
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

    // Construye el formulario para agregar una recompensa manualmente
    private VBox crearFormulario() {
        VBox formulario = new VBox(14);
        formulario.getStyleClass().add("content-card");

        Label labelForm = new Label("Agregar recompensa");
        labelForm.getStyleClass().add("field-label");
        labelForm.setStyle("-fx-font-size: 15px;");

        campoTipo = new TextField();
        campoTipo.setPromptText("Tipo (ej: Medalla, Insignia)");
        campoTipo.getStyleClass().add("form-input");

        campoMensaje = new TextField();
        campoMensaje.setPromptText("Mensaje (ej: ¡Lograste tu primera meta!)");
        campoMensaje.getStyleClass().add("form-input");

        campoFecha = new DatePicker(LocalDate.now());
        campoFecha.getStyleClass().add("form-input");

        // GridPane: tipo y fecha en fila 1; mensaje en fila 2 (colspan=2)
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.add(crearGrupo("Tipo", campoTipo), 0, 0);
        grid.add(crearGrupo("Fecha desbloqueo", campoFecha), 1, 0);
        grid.add(crearGrupo("Mensaje", campoMensaje), 0, 1, 2, 1);

        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col);

        etiquetaEstado = new Label("");
        etiquetaEstado.getStyleClass().add("status-label");
        etiquetaEstado.setMaxWidth(Double.MAX_VALUE);

        Button botonCrear = new Button("Agregar recompensa");
        botonCrear.getStyleClass().add("primary-button");
        botonCrear.setOnAction(e -> crearRecompensa());

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

    // Tabla con columnas: tipo, mensaje, fecha, estado y acción eliminar
    private TableView<Recompensa> crearTabla() {
        TableView<Recompensa> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Recompensa, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));

        TableColumn<Recompensa, String> colMensaje = new TableColumn<>("Mensaje");
        colMensaje.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMensaje()));

        TableColumn<Recompensa, String> colFecha = new TableColumn<>("Fecha desbloqueo");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaDesbloqueo().toString()));

        // "Desbloqueada" o "Bloqueada" según el campo booleano de la recompensa
        TableColumn<Recompensa, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isDesbloqueada() ? "Desbloqueada" : "Bloqueada"));

        // Botón eliminar: borra la recompensa de la DB y recarga la tabla
        TableColumn<Recompensa, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            {
                btn.getStyleClass().add("danger-button");
                btn.setOnAction(e -> {
                    Recompensa r = getTableView().getItems().get(getIndex());
                    Recompensa.eliminarRecompensa(r.getIdRecompensa());
                    cargarDatos();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tv.getColumns().addAll(colTipo, colMensaje, colFecha, colEstado, colAccion);
        tv.setMinHeight(300);
        return tv;
    }

    // Recarga la lista de recompensas desde la base de datos
    private void cargarDatos() {
        List<Recompensa> lista = Recompensa.listarRecompensasPorUsuario(usuario.getId());
        ObservableList<Recompensa> datos = FXCollections.observableArrayList(lista);
        tabla.setItems(datos);
    }

    // Valida el formulario y crea la recompensa si todos los campos están llenos
    private void crearRecompensa() {
        String tipo    = campoTipo.getText().trim();
        String mensaje = campoMensaje.getText().trim();
        LocalDate fecha = campoFecha.getValue();

        if (tipo.isEmpty() || mensaje.isEmpty() || fecha == null) {
            mostrarMensaje("Completa todos los campos.", false);
            return;
        }

        // desbloqueada=false: la recompensa recién creada empieza como bloqueada
        Recompensa recompensa = new Recompensa(0, tipo, mensaje, fecha, false, usuario.getId());
        recompensa.crearRecompensa();
        mostrarMensaje("Recompensa agregada correctamente.", true);
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
