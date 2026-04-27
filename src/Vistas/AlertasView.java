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

import java.util.List;

// Vista que muestra las alertas generadas automáticamente por el sistema.
// Las alertas NO se crean manualmente: el sistema las genera al detectar
// condiciones de riesgo financiero (déficit, precaución, concentración de gasto, meta en progreso).
public class AlertasView extends BorderPane {

    private final Usuario usuario;
    private TableView<Alerta> tabla;

    public AlertasView(Usuario usuario) {
        this.usuario = usuario;
        construirVista();
    }

    // Arma el layout: título, descripción informativa y tabla de alertas
    private void construirVista() {
        VBox contenido = new VBox(20);
        contenido.getStyleClass().add("content-area");
        contenido.setPadding(new Insets(28));

        Label titulo = new Label("🔔 Alertas del sistema");
        titulo.getStyleClass().add("content-title");

        // Explica al usuario que las alertas son automáticas
        Label info = new Label(
            "Las alertas se generan automáticamente cuando el sistema detecta una situación de riesgo financiero: " +
            "déficit, gasto superior al 80% del ingreso, concentración en una categoría o meta cercana a su objetivo.");
        info.getStyleClass().add("subtitle-label");
        info.setWrapText(true);

        tabla = crearTabla();
        cargarDatos();

        contenido.getChildren().addAll(titulo, info, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        this.setCenter(scroll);
    }

    // Tabla con columnas: tipo, mensaje, fecha, estado y botones de acción
    private TableView<Alerta> crearTabla() {
        TableView<Alerta> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("No hay alertas. El sistema generará alertas automáticamente al registrar gastos e ingresos."));

        TableColumn<Alerta, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));
        colTipo.setPrefWidth(140);
        colTipo.setMaxWidth(160);

        TableColumn<Alerta, String> colMensaje = new TableColumn<>("Mensaje");
        colMensaje.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMensaje()));
        colMensaje.setPrefWidth(400);
        colMensaje.setCellFactory(col -> new TableCell<>() {
            private final Label lbl = new Label();
            { lbl.setWrapText(true); lbl.setMaxWidth(Double.MAX_VALUE); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                lbl.setText(empty || item == null ? "" : item);
                setGraphic(empty ? null : lbl);
            }
        });

        TableColumn<Alerta, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colFecha.setPrefWidth(100);
        colFecha.setMaxWidth(110);

        TableColumn<Alerta, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isLeida() ? "Leída" : "Pendiente"));
        colEstado.setPrefWidth(100);
        colEstado.setMaxWidth(110);

        TableColumn<Alerta, Void> colAccion = new TableColumn<>("Acciones");
        colAccion.setPrefWidth(170);
        colAccion.setMaxWidth(180);
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnLeida    = new Button("Marcar leída");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox botones = new HBox(6, btnLeida, btnEliminar);
            {
                btnLeida.getStyleClass().add("small-button");
                btnEliminar.getStyleClass().add("danger-button");

                btnLeida.setOnAction(e -> {
                    Alerta alerta = getTableView().getItems().get(getIndex());
                    alerta.marcarComoLeida();
                    cargarDatos();
                });
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
        tv.setFixedCellSize(72);
        tv.setMinHeight(300);
        return tv;
    }

    // Recarga las alertas del usuario desde la base de datos
    private void cargarDatos() {
        List<Alerta> lista = Alerta.listarAlertasPorUsuario(usuario.getId());
        ObservableList<Alerta> datos = FXCollections.observableArrayList(lista);
        tabla.setItems(datos);
    }
}
