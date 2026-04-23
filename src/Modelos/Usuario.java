package Modelos;

import db.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Usuario extends Persona {
    private double saldoActual;

    public Usuario(int id, String nombre, String contrasena, String correo, String numeroTelefono, int edad, String cc, double saldoActual) {
        super(id, nombre, contrasena, correo, numeroTelefono, edad, cc);
        this.saldoActual = saldoActual;
    }

    public double getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(double saldoActual) {
        this.saldoActual = saldoActual;
    }

    public void registrarUsuario() {
        String sql = "INSERT INTO usuario (id, nombre, contrasena, correo, numero_telefono, edad, cc, saldo_actual) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement consultaPreparada = conexion.prepareStatement(sql)) {

            consultaPreparada.setInt(1, this.getId());
            consultaPreparada.setString(2, this.getNombre());
            consultaPreparada.setString(3, this.getContrasena());
            consultaPreparada.setString(4, this.getCorreo());
            consultaPreparada.setString(5, this.getNumeroTelefono());
            consultaPreparada.setInt(6, this.getEdad());
            consultaPreparada.setString(7, this.getCc());
            consultaPreparada.setDouble(8, this.getSaldoActual());

            consultaPreparada.executeUpdate();
            System.out.println("Usuario registrado correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
        }
    }

    public static Usuario leerPerfil(int idBuscado) {
        String sql = "SELECT * FROM usuario WHERE id = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement consultaPreparada = conexion.prepareStatement(sql)) {

            consultaPreparada.setInt(1, idBuscado);
            ResultSet resultado = consultaPreparada.executeQuery();

            if (resultado.next()) {
                return new Usuario(
                    resultado.getInt("id"),
                    resultado.getString("nombre"),
                    resultado.getString("contrasena"),
                    resultado.getString("correo"),
                    resultado.getString("numero_telefono"),
                    resultado.getInt("edad"),
                    resultado.getString("cc"),
                    resultado.getDouble("saldo_actual")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al leer perfil: " + e.getMessage());
        }

        return null;
    }

    public void actualizarUsuario() {
        String sql = "UPDATE usuario SET nombre = ?, contrasena = ?, correo = ?, numero_telefono = ?, edad = ?, cc = ?, saldo_actual = ? WHERE id = ?";

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
            consultaPreparada.setDouble(7, this.getSaldoActual());
            consultaPreparada.setInt(8, this.getId());

            int filasAfectadas = consultaPreparada.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Usuario actualizado correctamente.");
            } else {
                System.out.println("No se encontró un usuario con ese ID.");
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
        }
    }

    public static boolean eliminarUsuario(int idBuscado) {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (Connection conexion = ConexionDB.conectar();
             PreparedStatement consultaPreparada = conexion.prepareStatement(sql)) {

            if (conexion == null) {
                System.out.println("No se pudo conectar a la base de datos.");
                return false;
            }

            consultaPreparada.setInt(1, idBuscado);

            int filasAfectadas = consultaPreparada.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Usuario eliminado correctamente.");
                return true;
            } else {
                System.out.println("No se encontró un usuario con ese ID.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
}   