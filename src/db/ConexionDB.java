package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Clase utilitaria que centraliza la conexión a la base de datos SQLite.
// Todos los modelos la usan para obtener una conexión antes de ejecutar consultas.
public class ConexionDB {

    // Ruta del archivo SQLite. Al ser relativa, busca finanzas.db en el directorio
    // desde donde se ejecuta la aplicación (raíz del proyecto al correr con Maven).
    private static final String URL = "jdbc:sqlite:finanzas.db";

    // Abre y retorna una conexión a la base de datos.
    // Retorna null si la conexión falla (el error se imprime en consola).
    public static Connection conectar() {
        try {
            Connection conn = DriverManager.getConnection(URL);
            System.out.println("Conexión exitosa a la base de datos.");
            return conn;
        } catch (SQLException e) {
            System.out.println("Error al conectar: " + e.getMessage());
            return null;
        }
    }
}
