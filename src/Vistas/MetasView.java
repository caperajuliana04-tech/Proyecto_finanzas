package Vistas;

import Modelos.Alerta;
import Modelos.AnalizadorFinanciero;
import Modelos.Meta;
import Modelos.Recompensa;
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

// Vista para registrar y visualizar metas de ahorro.
// Muestra el progreso de cada meta como porcentaje.
public class MetasView extends BorderPane {

    private final Usuario usuario;        // Usuario autenticado
    private TextField campoNombre;        // Nombre de la meta
    private TextField campoMontoObjetivo; // Cuánto se quiere ahorrar en total
    private TextField campoMontoActual;   // Cuánto se lleva ahorrado hasta ahora
    private DatePicker campoFechaEsperada; // Fecha límite para alcanzar la meta
    private Label etiquetaEstado;         // Mensajes de éxito o error
    private TableView<Meta> tabla;        // Tabla con todas las metas del usuario

    public MetasView(Usuario usuario) {
        this.usuario = usuario;
        construirVista();
    }

    // Arma el layout: título, formulario y tabla dentro de un ScrollPane
    private void construirVista() {
        VBox contenido = new VBox(20);
        contenido.getStyleClass().add("content-area");
        contenido.setPadding(new Insets(28));

        Label titulo = new Label("🎯 Metas de ahorro");
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

    // Construye el formulario para agregar una nueva meta de ahorro
    private VBox crearFormulario() {
        VBox formulario = new VBox(14);
        formulario.getStyleClass().add("content-card");

        Label labelForm = new Label("Agregar nueva meta");
        labelForm.getStyleClass().add("field-label");
        labelForm.setStyle("-fx-font-size: 15px;");

        campoNombre = new TextField();
        campoNombre.setPromptText("Nombre (ej: Vacaciones)");
        campoNombre.getStyleClass().add("form-input");

        campoMontoObjetivo = new TextField();
        campoMontoObjetivo.setPromptText("Monto objetivo (ej: 2000000)");
        campoMontoObjetivo.getStyleClass().add("form-input");

        campoMontoActual = new TextField();
        campoMontoActual.setPromptText("Monto actual ahorrado (ej: 0)");
        campoMontoActual.getStyleClass().add("form-input");

        // La fecha esperada se inicializa 3 meses en el futuro como valor sugerido
        campoFechaEsperada = new DatePicker(LocalDate.now().plusMonths(3));
        campoFechaEsperada.getStyleClass().add("form-input");

        // GridPane: 2 columnas × 2 filas para los 4 campos
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.add(crearGrupo("Nombre de la meta", campoNombre), 0, 0);
        grid.add(crearGrupo("Monto objetivo ($)", campoMontoObjetivo), 1, 0);
        grid.add(crearGrupo("Monto actual ($)", campoMontoActual), 0, 1);
        grid.add(crearGrupo("Fecha esperada", campoFechaEsperada), 1, 1);

        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col);

        etiquetaEstado = new Label("");
        etiquetaEstado.getStyleClass().add("status-label");
        etiquetaEstado.setMaxWidth(Double.MAX_VALUE);

        Button botonAgregar = new Button("Agregar meta");
        botonAgregar.getStyleClass().add("primary-button");
        botonAgregar.setOnAction(e -> agregarMeta());

        Button botonLimpiar = new Button("Limpiar");
        botonLimpiar.getStyleClass().add("secondary-button");
        botonLimpiar.setOnAction(e -> limpiar());

        HBox botones = new HBox(10, botonAgregar, botonLimpiar);
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

    // Tabla con columnas: nombre, objetivo, actual, progreso (%), fecha esperada y eliminar
    private TableView<Meta> crearTabla() {
        TableView<Meta> tv = new TableView<>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Meta, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));

        TableColumn<Meta, Number> colObjetivo = new TableColumn<>("Objetivo ($)");
        colObjetivo.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getMontoObjetivo()));

        TableColumn<Meta, Number> colActual = new TableColumn<>("Actual ($)");
        colActual.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getMontoActual()));

        // Columna de progreso: usa calcularProgreso() del modelo y lo formatea como porcentaje
        TableColumn<Meta, String> colProgreso = new TableColumn<>("Progreso");
        colProgreso.setCellValueFactory(d -> {
            double progreso = d.getValue().calcularProgreso();
            return new SimpleStringProperty(String.format("%.1f%%", progreso));
        });

        TableColumn<Meta, String> colFecha = new TableColumn<>("Fecha esperada");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaEsperada().toString()));

        // Columna con botón eliminar
        TableColumn<Meta, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            {
                btn.getStyleClass().add("danger-button");
                btn.setOnAction(e -> {
                    Meta meta = getTableView().getItems().get(getIndex());
                    Meta.eliminarMeta(meta.getIdMeta());
                    cargarDatos();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tv.getColumns().addAll(colNombre, colObjetivo, colActual, colProgreso, colFecha, colAccion);
        tv.setMinHeight(300);
        return tv;
    }

    // Recarga la lista de metas desde la base de datos
    private void cargarDatos() {
        List<Meta> lista = Meta.listarMetasPorUsuario(usuario.getId());
        ObservableList<Meta> datos = FXCollections.observableArrayList(lista);
        tabla.setItems(datos);
    }

    // Valida el formulario y registra la meta si los datos son válidos
    private void agregarMeta() {
        String nombre = campoNombre.getText().trim();
        String textoObjetivo = campoMontoObjetivo.getText().trim();
        String textoActual = campoMontoActual.getText().trim();
        LocalDate fechaEsperada = campoFechaEsperada.getValue();

        if (nombre.isEmpty() || textoObjetivo.isEmpty() || textoActual.isEmpty() || fechaEsperada == null) {
            mostrarMensaje("Completa todos los campos.", false);
            return;
        }

        double objetivo, actual;
        try {
            objetivo = Double.parseDouble(textoObjetivo);
            actual = Double.parseDouble(textoActual);
            if (objetivo <= 0 || actual < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarMensaje("Los montos deben ser números válidos.", false);
            return;
        }

        // fechaRegistro = hoy; idMeta = 0 porque la DB lo asigna automáticamente
        Meta meta = new Meta(0, usuario.getId(), objetivo, actual, LocalDate.now(), fechaEsperada, nombre);
        meta.agregarMeta();
        mostrarMensaje("Meta registrada correctamente.", true);
        limpiar();
        cargarDatos();

        // Evaluación automática de alertas y recompensas según el progreso de las metas.
        // Se evitan duplicados verificando si ya existe una alerta o recompensa del mismo tipo.
        List<Meta> todasMetas = Meta.listarMetasPorUsuario(usuario.getId());

        Alerta alertaProgreso = AnalizadorFinanciero.generarAlertaMetaProgreso(todasMetas, usuario.getId());
        if (alertaProgreso != null && !Alerta.existeAlertaPendiente(usuario.getId(), alertaProgreso.getTipo())) {
            alertaProgreso.crearAlerta();
        }

        Recompensa recompensaAvanzada = AnalizadorFinanciero.evaluarRecompensaMetaAvanzada(todasMetas, usuario.getId());
        if (recompensaAvanzada != null && !Recompensa.existeRecompensa(usuario.getId(), recompensaAvanzada.getTipo())) {
            recompensaAvanzada.crearRecompensa();
        }

        Recompensa recompensaCumplida = AnalizadorFinanciero.evaluarRecompensaMetaCumplida(todasMetas, usuario.getId());
        if (recompensaCumplida != null && !Recompensa.existeRecompensa(usuario.getId(), recompensaCumplida.getTipo())) {
            recompensaCumplida.crearRecompensa();
        }
    }

    // Limpia los campos del formulario
    private void limpiar() {
        campoNombre.clear();
        campoMontoObjetivo.clear();
        campoMontoActual.clear();
        campoFechaEsperada.setValue(LocalDate.now().plusMonths(3));
    }

    // Muestra mensaje de éxito (verde) o error (rojo)
    private void mostrarMensaje(String texto, boolean exito) {
        etiquetaEstado.setText(texto);
        etiquetaEstado.getStyleClass().removeAll("status-success", "status-error");
        etiquetaEstado.getStyleClass().add(exito ? "status-success" : "status-error");
    }
}
