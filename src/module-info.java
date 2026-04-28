// Descriptor del módulo Java para el proyecto de finanzas personales.
// El sistema de módulos de Java (introducido en Java 9) controla qué paquetes
// son visibles hacia afuera y qué librerías externas puede usar este módulo.
module proyecto.finanzas {

    // "requires" declara las dependencias externas que este módulo necesita.

    // javafx.controls: incluye todos los controles de UI (Button, TextField, TableView, etc.)
    requires javafx.controls;

    // javafx.graphics: incluye el motor de renderizado, Stage, Scene, y los nodos base (Pane, Label…)
    requires javafx.graphics;

    // org.xerial.sqlitejdbc: driver JDBC para SQLite, permite hacer consultas SQL con PreparedStatement
    requires org.xerial.sqlitejdbc;
    requires jdk.httpserver;

    // "opens" permite que JavaFX acceda a las clases de esos paquetes por reflexión en tiempo de ejecución.
    // Sin esto, JavaFX no puede instanciar ni animar los nodos definidos en esos paquetes.

    // Abre el paquete App (donde vive MainApp) a javafx.graphics para que pueda lanzar la aplicación
    opens App to javafx.graphics;

    // Abre el paquete Vistas a javafx.graphics para que pueda manejar las escenas y transiciones
    opens Vistas to javafx.graphics;
}
