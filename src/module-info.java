module proyecto.finanzas {
    requires javafx.controls;
    requires javafx.graphics;
    requires org.xerial.sqlitejdbc;

    opens App to javafx.graphics;
}
