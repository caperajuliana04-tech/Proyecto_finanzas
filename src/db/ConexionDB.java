package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL = "jdbc:sqlite:finanzas.db";

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