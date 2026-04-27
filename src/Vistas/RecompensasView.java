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

import java.util.List;

// Vista que muestra las recompensas ganadas automáticamente por el sistema (gamificación).
// Las recompensas NO se crean manualmente: el sistema las otorga cuando el usuario
// cumple condiciones positivas como mantener superávit, avanzar en metas o completarlas.
public class RecompensasView extends BorderPane {

    private final Usuario usuario;
    private TableView<Recompensa> tabla;

    public RecompensasView(Usuario usuario) {
        this.usuario = usuario;
        construirVista();
    }

    // Arma el layout: título, descripción del sistema de gamificación y tabla de recompensas
    private void construirVista() {
        VBox contenido = new VBox(20);
        contenido.getStyleClass().add("content-area");
        contenido.setPadding(new Insets(28));

        Label titulo = new Label("🏆 Recompensas");
        titulo.getStyleClass().add("content-title");

        // Explica cómo funciona el sistema de gamificación
        Label info = new Label(
            "Las recompensas se otorgan automáticamente cuando mantienes buenos hábitos financieros: " +
            "tener superávit (gastos < ingresos), avanzar al 80% en una meta o completar una meta al 100%.");
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

    // Tabla con columnas: tipo, mensaje, fecha de obtención, estado y botón eliminar
    private TableView<Recompensa> crearTabla() {
        TableView<Recompensa> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("Aún no tienes recompensas. Registra ingresos, gastos y metas para obtenerlas."));

        TableColumn<Recompensa, String> colTipo = new TableColumn<>("Logro");
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));
        colTipo.setPrefWidth(150);
        colTipo.setMaxWidth(170);

        TableColumn<Recompensa, String> colMensaje = new TableColumn<>("Mensaje");
        colMensaje.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMensaje()));
        colMensaje.setPrefWidth(450);
        colMensaje.setCellFactory(col -> new TableCell<>() {
            private final Label lbl = new Label();
            { lbl.setWrapText(true); lbl.setMaxWidth(Double.MAX_VALUE); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                lbl.setText(empty || item == null ? "" : item);
                setGraphic(empty ? null : lbl);
            }
        });

        TableColumn<Recompensa, String> colFecha = new TableColumn<>("Fecha obtenida");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaDesbloqueo().toString()));
        colFecha.setPrefWidth(120);
        colFecha.setMaxWidth(130);

        TableColumn<Recompensa, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setPrefWidth(100);
        colAccion.setMaxWidth(110);
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

        tv.getColumns().addAll(colTipo, colMensaje, colFecha, colAccion);
        tv.setFixedCellSize(72);
        tv.setMinHeight(300);
        return tv;
    }

    // Recarga las recompensas del usuario desde la base de datos
    private void cargarDatos() {
        List<Recompensa> lista = Recompensa.listarRecompensasPorUsuario(usuario.getId());
        ObservableList<Recompensa> datos = FXCollections.observableArrayList(lista);
        tabla.setItems(datos);
    }
}
