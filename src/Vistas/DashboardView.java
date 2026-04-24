package Vistas;

import App.MainApp;
import Modelos.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Vista principal de la aplicación, visible después de iniciar sesión.
// Usa BorderPane: sidebar fija a la izquierda y contenido dinámico en el centro.
// La navegación funciona reemplazando el nodo central con la vista seleccionada.
public class DashboardView extends BorderPane {

    private final Stage stage;    // Ventana principal, necesaria para regresar al login
    private final Usuario usuario; // Usuario autenticado, se pasa a cada vista interna
    private Button botonActivo;   // Botón del sidebar que está seleccionado actualmente

    // Constructor: recibe el stage y el usuario para construir el layout completo
    public DashboardView(Stage stage, Usuario usuario) {
        this.stage = stage;
        this.usuario = usuario;
        construirVista();
    }

    // Arma el layout: sidebar a la izquierda y panel de inicio como vista por defecto
    private void construirVista() {
        VBox sidebar = crearSidebar();
        this.setLeft(sidebar);
        // Al abrir el dashboard, se muestra el panel de inicio por defecto
        cargarVista(new PanelInicioView(usuario), null);
    }

    // Crea la barra lateral con los botones de navegación y el botón de cerrar sesión
    private VBox crearSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(24, 16, 24, 16));

        // Encabezado: nombre de la app y saludo al usuario
        Label appName = new Label("💰 Finanzas");
        appName.getStyleClass().add("sidebar-title");

        // Toma solo el primer nombre del usuario para el saludo
        Label nombreUsuario = new Label("Hola, " + usuario.getNombre().split(" ")[0]);
        nombreUsuario.getStyleClass().add("sidebar-subtitle");

        // Botones de navegación, uno por cada sección de la app
        Button btnInicio      = crearBotonNav("🏠  Inicio");
        Button btnIngresos    = crearBotonNav("💵  Ingresos");
        Button btnGastos      = crearBotonNav("💸  Gastos");
        Button btnMetas       = crearBotonNav("🎯  Metas");
        Button btnAlertas     = crearBotonNav("🔔  Alertas");
        Button btnRecompensas = crearBotonNav("🏆  Recompensas");
        Button btnPerfil      = crearBotonNav("👤  Mi perfil");

        // Al hacer clic en cada botón se carga la vista correspondiente en el centro
        btnInicio.setOnAction(e      -> cargarVista(new PanelInicioView(usuario), btnInicio));
        btnIngresos.setOnAction(e    -> cargarVista(new IngresosView(usuario), btnIngresos));
        btnGastos.setOnAction(e      -> cargarVista(new GastosView(usuario), btnGastos));
        btnMetas.setOnAction(e       -> cargarVista(new MetasView(usuario), btnMetas));
        btnAlertas.setOnAction(e     -> cargarVista(new AlertasView(usuario), btnAlertas));
        btnRecompensas.setOnAction(e -> cargarVista(new RecompensasView(usuario), btnRecompensas));
        btnPerfil.setOnAction(e      -> cargarVista(new PerfilView(usuario), btnPerfil));

        // Espaciador flexible: empuja el botón de cerrar sesión hacia el fondo del sidebar
        VBox espaciador = new VBox();
        VBox.setVgrow(espaciador, Priority.ALWAYS);

        // Botón de cerrar sesión: regresa a la pantalla de login
        Button btnSalir = new Button("Cerrar sesión");
        btnSalir.getStyleClass().add("danger-button");
        btnSalir.setMaxWidth(Double.MAX_VALUE);
        btnSalir.setOnAction(e -> MainApp.mostrarLogin(stage));

        sidebar.getChildren().addAll(
            appName, nombreUsuario,
            btnInicio, btnIngresos, btnGastos, btnMetas,
            btnAlertas, btnRecompensas, btnPerfil,
            espaciador, btnSalir
        );

        // El botón de inicio queda seleccionado al abrir el dashboard
        activarBoton(btnInicio);
        return sidebar;
    }

    // Crea un botón de navegación con el estilo y comportamiento estándar del sidebar
    private Button crearBotonNav(String texto) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("nav-button");
        boton.setMaxWidth(Double.MAX_VALUE);     // Ocupa todo el ancho del sidebar
        boton.setAlignment(Pos.CENTER_LEFT);    // Texto alineado a la izquierda
        return boton;
    }

    // Reemplaza el nodo central del BorderPane con la nueva vista y actualiza el botón activo
    private void cargarVista(javafx.scene.Node vista, Button boton) {
        this.setCenter(vista);
        if (boton != null) activarBoton(boton);
    }

    // Resalta visualmente el botón seleccionado y quita el resaltado del anterior
    private void activarBoton(Button boton) {
        if (botonActivo != null) {
            botonActivo.getStyleClass().remove("nav-button-selected");
        }
        boton.getStyleClass().add("nav-button-selected");
        botonActivo = boton;
    }
}
