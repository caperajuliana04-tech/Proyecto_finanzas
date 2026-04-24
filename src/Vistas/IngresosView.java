package Vistas;

import Modelos.Ingreso;
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

// Vista para registrar y visualizar los ingresos del usuario.
// Contiene un formulario de entrada y una tabla con el historial de ingresos.
public class IngresosView extends BorderPane {

    private final Usuario usuario;   // Usuario autenticado
    private TextField campoMonto;    // Campo para el monto del ingreso
    private TextField campoConcepto; // Campo para la descripción del ingreso
    private DatePicker campofecha;   // Selector de fecha
    private Label etiquetaEstado;    // Muestra mensajes de éxito o error
    private TableView<Ingreso> tabla; // Tabla que lista todos los ingresos

    public IngresosView(Usuario usuario) {
        this.usuario = usuario;
        construirVista();
    }

    // Arma el layout: título, formulario y tabla dentro de un ScrollPane
    private void construirVista() {
        VBox contenido = new VBox(20);
        contenido.getStyleClass().add("content-area");
        contenido.setPadding(new Insets(28));

        Label titulo = new Label("💵 Ingresos");
        titulo.getStyleClass().add("content-title");

        VBox formulario = crearFormulario();
        tabla = crearTabla();
        cargarDatos(); // Carga los ingresos existentes al abrir la vista

        contenido.getChildren().addAll(titulo, formulario, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS); // La tabla ocupa el espacio vertical restante

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        this.setCenter(scroll);
    }

    // Construye el formulario de registro: campos + botones + mensaje de estado
    private VBox crearFormulario() {
        VBox formulario = new VBox(14);
        formulario.getStyleClass().add("content-card");

        Label labelForm = new Label("Registrar nuevo ingreso");
        labelForm.getStyleClass().add("field-label");
        labelForm.setStyle("-fx-font-size: 15px;");

        campoMonto = new TextField();
        campoMonto.setPromptText("Monto (ej: 500000)");
        campoMonto.getStyleClass().add("form-input");

        campoConcepto = new TextField();
        campoConcepto.setPromptText("Concepto (ej: Salario)");
        campoConcepto.getStyleClass().add("form-input");

        campofecha = new DatePicker(LocalDate.now()); // Fecha actual por defecto
        campofecha.getStyleClass().add("form-input");

        // GridPane organiza los 3 campos en una fila con columnas de igual tamaño
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.add(crearGrupo("Monto ($)", campoMonto), 0, 0);
        grid.add(crearGrupo("Concepto", campoConcepto), 1, 0);
        grid.add(crearGrupo("Fecha", campofecha), 2, 0);

        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS); // Cada columna crece por igual
        grid.getColumnConstraints().addAll(col, col, col);

        etiquetaEstado = new Label("");
        etiquetaEstado.getStyleClass().add("status-label");
        etiquetaEstado.setMaxWidth(Double.MAX_VALUE);

        Button botonRegistrar = new Button("Registrar ingreso");
        botonRegistrar.getStyleClass().add("primary-button");
        botonRegistrar.setOnAction(e -> registrarIngreso());

        Button botonLimpiar = new Button("Limpiar");
        botonLimpiar.getStyleClass().add("secondary-button");
        botonLimpiar.setOnAction(e -> limpiar());

        HBox botones = new HBox(10, botonRegistrar, botonLimpiar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        formulario.getChildren().addAll(labelForm, grid, botones, etiquetaEstado);
        return formulario;
    }

    // Agrupa una etiqueta de texto encima de su campo de entrada
    private VBox crearGrupo(String texto, javafx.scene.control.Control campo) {
        Label lbl = new Label(texto);
        lbl.getStyleClass().add("field-label");
        VBox g = new VBox(6, lbl, campo);
        VBox.setVgrow(campo, Priority.NEVER);
        return g;
    }

    // Construye la TableView con columnas para monto, concepto, fecha y acción eliminar
    private TableView<Ingreso> crearTabla() {
        TableView<Ingreso> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Columnas llenan el ancho

        // Columna Monto: muestra el valor numérico del ingreso
        TableColumn<Ingreso, Number> colMonto = new TableColumn<>("Monto ($)");
        colMonto.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getMonto()));

        // Columna Concepto: descripción del ingreso
        TableColumn<Ingreso, String> colConcepto = new TableColumn<>("Concepto");
        colConcepto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getConcepto()));

        // Columna Fecha: muestra la fecha como texto
        TableColumn<Ingreso, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));

        // Columna Acción: botón "Eliminar" que borra el ingreso y recarga la tabla
        TableColumn<Ingreso, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            {
                btn.getStyleClass().add("danger-button");
                btn.setOnAction(e -> {
                    Ingreso ingreso = getTableView().getItems().get(getIndex());
                    Ingreso.eliminarIngreso(ingreso.getIdIngreso());
                    cargarDatos(); // Refresca la tabla tras eliminar
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn); // Solo muestra el botón en filas con datos
            }
        });

        tv.getColumns().addAll(colMonto, colConcepto, colFecha, colAccion);
        tv.setMinHeight(300);
        return tv;
    }

    // Consulta la base de datos y actualiza los datos de la tabla
    private void cargarDatos() {
        List<Ingreso> lista = Ingreso.listarIngresosPorUsuario(usuario.getId());
        ObservableList<Ingreso> datos = FXCollections.observableArrayList(lista);
        tabla.setItems(datos);
    }

    // Valida el formulario y registra el ingreso si los datos son correctos
    private void registrarIngreso() {
        String textoMonto = campoMonto.getText().trim();
        String concepto = campoConcepto.getText().trim();
        LocalDate fecha = campofecha.getValue();

        if (textoMonto.isEmpty() || concepto.isEmpty() || fecha == null) {
            mostrarMensaje("Completa todos los campos.", false);
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(textoMonto);
            if (monto <= 0) throw new NumberFormatException(); // El monto debe ser positivo
        } catch (NumberFormatException e) {
            mostrarMensaje("El monto debe ser un número positivo.", false);
            return;
        }

        // Crea el objeto con idIngreso=0 porque la DB asignará el ID automáticamente
        Ingreso ingreso = new Ingreso(0, usuario.getId(), monto, fecha, concepto);
        ingreso.registrarIngreso();
        mostrarMensaje("Ingreso registrado correctamente.", true);
        limpiar();
        cargarDatos(); // Refresca la tabla para mostrar el nuevo registro
    }

    // Limpia todos los campos del formulario y restaura la fecha al día de hoy
    private void limpiar() {
        campoMonto.clear();
        campoConcepto.clear();
        campofecha.setValue(LocalDate.now());
    }

    // Muestra un mensaje de éxito (verde) o error (rojo) en la etiqueta de estado
    private void mostrarMensaje(String texto, boolean exito) {
        etiquetaEstado.setText(texto);
        etiquetaEstado.getStyleClass().removeAll("status-success", "status-error");
        etiquetaEstado.getStyleClass().add(exito ? "status-success" : "status-error");
    }
}
