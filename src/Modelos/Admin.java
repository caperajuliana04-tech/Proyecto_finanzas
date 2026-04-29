package Modelos;

import db.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Representa a un administrador del sistema.
// Hereda todos los atributos de Persona y agrega operaciones CRUD sobre la tabla "admin".
public class Admin extends Persona {

    // Constructor: delega directamente al constructor de Persona
    public Admin(int id, String nombre, String contrasena, String correo,
                String numeroTelefono, int edad, String cc) {
        super(id, nombre, contrasena, correo, numeroTelefono, edad, cc);
    }

    // Inserta este administrador en la base de datos
    public void registrarAdmin() {
        String sql = "INSERT INTO admin (id, nombre, contrasena, correo, numero_telefono, edad, cc) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, this.getId());
            ps.setString(2, this.getNombre());
            ps.setString(3, this.getContrasena());
            ps.setString(4, this.getCorreo());
            ps.setString(5, this.getNumeroTelefono());
            ps.setInt(6, this.getEdad());
            ps.setString(7, this.getCc());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al registrar admin: " + e.getMessage());
        }
    }

    // Busca y retorna un Admin por su ID; retorna null si no existe
    public static Admin leerPerfil(int id) {
        String sql = "SELECT * FROM admin WHERE id = ?";
        try (Connection con = ConexionDB.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Construye el objeto Admin con los datos leídos de la fila
                return new Admin(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("contrasena"),
                    rs.getString("correo"),
                    rs.getString("numero_telefono"),
                    rs.getInt("edad"),
                    rs.getString("cc")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al leer admin: " + e.getMessage());
        }
        return null;
    }

    // Actualiza los datos del administrador en la base de datos usando su ID como clave
    public void actualizarAdmin() {
        String sql = "UPDATE admin SET nombre=?, contrasena=?, correo=?, numero_telefono=?, edad=?, cc=? WHERE id=?";
        try (Connection con = ConexionDB.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.getNombre());
            ps.setString(2, this.getContrasena());
            ps.setString(3, this.getCorreo());
            ps.setString(4, this.getNumeroTelefono());
            ps.setInt(5, this.getEdad());
            ps.setString(6, this.getCc());
            ps.setInt(7, this.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al actualizar admin: " + e.getMessage());
        }
    }

    // Elimina un administrador por su ID. Retorna true si se borró al menos una fila
    public static boolean eliminarAdmin(int id) {
        String sql = "DELETE FROM admin WHERE id = ?";
        try (Connection con = ConexionDB.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar admin: " + e.getMessage());
            return false;
        }
    }
}
