package Modelos;

import db.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Admin extends Persona {

    public Admin(int id, String nombre, String contrasena, String correo, String numeroTelefono, int edad, String cc) {
        super(id, nombre, contrasena, correo, numeroTelefono, edad, cc);
    }

    public void registrarAdmin() {
        String sql = "INSERT INTO admin (id, nombre, contrasena, correo, numero_telefono, edad, cc) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement consultaPreparada = conexion.prepareStatement(sql)) {

            consultaPreparada.setInt(1, this.getId());
            consultaPreparada.setString(2, this.getNombre());
            consultaPreparada.setString(3, this.getContrasena());
            consultaPreparada.setString(4, this.getCorreo());
            consultaPreparada.setString(5, this.getNumeroTelefono());
            consultaPreparada.setInt(6, this.getEdad());
            consultaPreparada.setString(7, this.getCc());

            consultaPreparada.executeUpdate();
            System.out.println("Admin registrado correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al registrar admin: " + e.getMessage());
        }
    }

    public static Admin leerPerfil(int idBuscado) {
        String sql = "SELECT * FROM admin WHERE id = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement consultaPreparada = conexion.prepareStatement(sql)) {

            consultaPreparada.setInt(1, idBuscado);
            ResultSet resultado = consultaPreparada.executeQuery();

            if (resultado.next()) {
                return new Admin(
                    resultado.getInt("id"),
                    resultado.getString("nombre"),
                    resultado.getString("contrasena"),
                    resultado.getString("correo"),
                    resultado.getString("numero_telefono"),
                    resultado.getInt("edad"),
                    resultado.getString("cc")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al leer perfil del admin: " + e.getMessage());
        }

        return null;
    }

    public void actualizarAdmin() {
        String sql = "UPDATE admin SET nombre = ?, contrasena = ?, correo = ?, numero_telefono = ?, edad = ?, cc = ? WHERE id = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement consultaPreparada = conexion.prepareStatement(sql)) {

            if (conexion == null) {
                System.out.println("No se pudo conectar a la base de datos.");
                return;
            }

            consultaPreparada.setString(1, this.getNombre());
            consultaPreparada.setString(2, this.getContrasena());
            consultaPreparada.setString(3, this.getCorreo());
            consultaPreparada.setString(4, this.getNumeroTelefono());
            consultaPreparada.setInt(5, this.getEdad());
            consultaPreparada.setString(6, this.getCc());
            consultaPreparada.setInt(7, this.getId());

            int filasAfectadas = consultaPreparada.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Admin actualizado correctamente.");
            } else {
                System.out.println("No se encontró un admin con ese ID.");
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar admin: " + e.getMessage());
        }
    }

    public static boolean eliminarAdmin(int idBuscado) {
        String sql = "DELETE FROM admin WHERE id = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement consultaPreparada = conexion.prepareStatement(sql)) {

            if (conexion == null) {
                System.out.println("No se pudo conectar a la base de datos.");
                return false;
            }

            consultaPreparada.setInt(1, idBuscado);

            int filasAfectadas = consultaPreparada.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Admin eliminado correctamente.");
                return true;
            } else {
                System.out.println("No se encontró un admin con ese ID.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar admin: " + e.getMessage());
            return false;
        }
    }
}

