package Vistas;

import Modelos.Gasto;
import Modelos.Usuario;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

// Vista para registrar y visualizar los gastos del usuario.
// Contiene un formulario de entrada y una tabla con el historial de gastos.
public class GastosView extends BorderPane {

    private final Usuario usuario;    // Usuario autenticado
    private TextField campoNombre;    // Nombre del gasto (ej: "Mercado")
    private TextField campoMonto;     // Cantidad gastada
    private TextField campoCategoria; // Categoría del gasto (ej: "Alimentación")
    private DatePicker campoFecha;    // Selector de fecha
    private Label etiquetaEstado;     // Mensajes de éxito o error
    private TableView<Gasto> tabla;   // Tabla con el historial de gastos

    public GastosView(Usuario usuario) {
        this.usuario = usuario;
        construirVista();
    }

    // Arma el layout: título, formulario y tabla dentro de un ScrollPane
    private void construirVista() {
        VBox contenido = new VBox(20);
        contenido.getStyleClass().add("content-area");
        contenido.setPadding(new Insets(28));

        Label titulo = new Label("💸 Gastos");
        titulo.getStyleClass().add("content-title");

        VBox formulario = crearFormulario();
        tabla = crearTabla();
        cargarDatos(); // Carga los gastos existentes al abrir la vista

        contenido.getChildren().addAll(titulo, formulario, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        this.setCenter(scroll);
    }

    // Construye el formulario de registro con 4 campos organizados en una cuadrícula
    private VBox crearFormulario() {
        VBox formulario = new VBox(14);
        formulario.getStyleClass().add("content-card");

        Label labelForm = new Label("Registrar nuevo gasto");
        labelForm.getStyleClass().add("field-label");
        labelForm.setStyle("-fx-font-size: 15px;");

        campoNombre = new TextField();
        campoNombre.setPromptText("Nombre (ej: Mercado)");
        campoNombre.getStyleClass().add("form-input");

        campoMonto = new TextField();
        campoMonto.setPromptText("Monto (ej: 80000)");
        campoMonto.getStyleClass().add("form-input");

        campoCategoria = new TextField();
        campoCategoria.setPromptText("Categoría (ej: Alimentación)");
        campoCategoria.getStyleClass().add("form-input");

        campoFecha = new DatePicker(LocalDate.now());
        campoFecha.getStyleClass().add("form-input");

        // GridPane: 2 columnas × 2 filas para distribuir los 4 campos
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.add(crearGrupo("Nombre", campoNombre), 0, 0);
        grid.add(crearGrupo("Monto ($)", campoMonto), 1, 0);
        grid.add(crearGrupo("Categoría", campoCategoria), 0, 1);
        grid.add(crearGrupo("Fecha", campoFecha), 1, 1);

        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col);

        etiquetaEstado = new Label("");
        etiquetaEstado.getStyleClass().add("status-label");
        etiquetaEstado.setMaxWidth(Double.MAX_VALUE);

        Button botonAgregar = new Button("Agregar gasto");
        botonAgregar.getStyleClass().add("primary-button");
        botonAgregar.setOnAction(e -> agregarGasto());

        Button botonLimpiar = new Button("Limpiar");
        botonLimpiar.getStyleClass().add("secondary-button");
        botonLimpiar.setOnAction(e -> limpiar());

        HBox botones = new HBox(10, botonAgregar, botonLimpiar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        formulario.getChildren().addAll(labelForm, grid, botones, etiquetaEstado);
        return formulario;
    }

    // Agrupa una etiqueta de texto encima de su campo de entrada
    private VBox crearGrupo(String texto, javafx.scene.control.Control campo) {
        Label lbl = new Label(texto);
        lbl.getStyleClass().add("field-label");
        return new VBox(6, lbl, campo);
    }

    // Construye la tabla con columnas: nombre, monto, categoría, fecha y acción eliminar
    private TableView<Gasto> crearTabla() {
        TableView<Gasto> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Gasto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));

        TableColumn<Gasto, Number> colMonto = new TableColumn<>("Monto ($)");
        colMonto.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getMonto()));

        TableColumn<Gasto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoria()));

        TableColumn<Gasto, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));

        // Columna con botón eliminar: borra el gasto y suma el monto de vuelta al saldo
        TableColumn<Gasto, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            {
                btn.getStyleClass().add("danger-button");
                btn.setOnAction(e -> {
                    Gasto gasto = getTableView().getItems().get(getIndex());
                    Gasto.eliminarGasto(gasto.getIdGasto());
                    cargarDatos();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tv.getColumns().addAll(colNombre, colMonto, colCategoria, colFecha, colAccion);
        tv.setMinHeight(300);
        return tv;
    }

    // Consulta la base de datos y actualiza los datos de la tabla
    private void cargarDatos() {
        List<Gasto> lista = Gasto.listarGastosPorUsuario(usuario.getId());
        ObservableList<Gasto> datos = FXCollections.observableArrayList(lista);
        tabla.setItems(datos);
    }

    // Valida el formulario y registra el gasto si los datos son correctos
    private void agregarGasto() {
        String nombre = campoNombre.getText().trim();
        String textoMonto = campoMonto.getText().trim();
        String categoria = campoCategoria.getText().trim();
        LocalDate fecha = campoFecha.getValue();

        if (nombre.isEmpty() || textoMonto.isEmpty() || categoria.isEmpty() || fecha == null) {
            mostrarMensaje("Completa todos los campos.", false);
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(textoMonto);
            if (monto <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarMensaje("El monto debe ser un número positivo.", false);
            return;
        }

        // idGasto=0 porque la base de datos asignará el ID automáticamente (AUTOINCREMENT)
        Gasto gasto = new Gasto(0, usuario.getId(), monto, fecha, nombre, categoria);
        gasto.agregarGasto();
        mostrarMensaje("Gasto registrado correctamente.", true);
        limpiar();
        cargarDatos();
    }

    // Limpia todos los campos del formulario
    private void limpiar() {
        campoNombre.clear();
        campoMonto.clear();
        campoCategoria.clear();
        campoFecha.setValue(LocalDate.now());
    }

    // Muestra un mensaje de éxito (verde) o error (rojo)
    private void mostrarMensaje(String texto, boolean exito) {
        etiquetaEstado.setText(texto);
        etiquetaEstado.getStyleClass().removeAll("status-success", "status-error");
        etiquetaEstado.getStyleClass().add(exito ? "status-success" : "status-error");
    }
}
