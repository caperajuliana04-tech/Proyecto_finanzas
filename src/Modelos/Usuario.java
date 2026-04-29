package Modelos;

import db.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Representa al usuario final de la aplicación.
// Hereda de Persona y agrega saldo, más operaciones CRUD sobre la tabla "usuario".
public class Usuario extends Persona {

    private double saldoActual; // Saldo disponible en cuenta, se actualiza al registrar ingresos/gastos

    // Constructor completo
    public Usuario(int id, String nombre, String contrasena, String correo,
                String numeroTelefono, int edad, String cc, double saldoActual) {
        super(id, nombre, contrasena, correo, numeroTelefono, edad, cc);
        this.saldoActual = saldoActual;
    }

    public double getSaldoActual() { return saldoActual; }
    public void setSaldoActual(double saldoActual) { this.saldoActual = saldoActual; }

    // Inserta este usuario en la base de datos con todos sus campos
    public void registrarUsuario() {
        String sql = "INSERT INTO usuario (id, nombre, contrasena, correo, numero_telefono, edad, cc, saldo_actual) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, this.getId());
            ps.setString(2, this.getNombre());
            ps.setString(3, this.getContrasena());
            ps.setString(4, this.getCorreo());
            ps.setString(5, this.getNumeroTelefono());
            ps.setInt(6, this.getEdad());
            ps.setString(7, this.getCc());
            ps.setDouble(8, this.getSaldoActual());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
        }
    }

    // Busca un usuario por su ID numérico (usado al verificar si ya existe al registrar)
    public static Usuario leerPerfil(int id) {
        return buscar("SELECT * FROM usuario WHERE id = ?", ps -> ps.setInt(1, id));
    }

    // Busca un usuario por correo electrónico (usado en el login)
    public static Usuario buscarPorCorreo(String correo) {
        return buscar("SELECT * FROM usuario WHERE correo = ?", ps -> ps.setString(1, correo));
    }

    // Método privado reutilizable que ejecuta cualquier SELECT de usuario y construye el objeto.
    // Recibe la SQL y un "preparador" funcional que asigna el parámetro de búsqueda.
    private static Usuario buscar(String sql, ConsultaSQL preparador) {
        try (Connection con = ConexionDB.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            preparador.preparar(ps);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("contrasena"),
                    rs.getString("correo"),
                    rs.getString("numero_telefono"),
                    rs.getInt("edad"),
                    rs.getString("cc"),
                    rs.getDouble("saldo_actual")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar usuario: " + e.getMessage());
        }
        return null;
    }

    // Actualiza todos los campos editables del usuario en la base de datos
    public void actualizarUsuario() {
        String sql = "UPDATE usuario SET nombre=?, contrasena=?, correo=?, numero_telefono=?, edad=?, cc=?, saldo_actual=? WHERE id=?";
        try (Connection con = ConexionDB.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.getNombre());
            ps.setString(2, this.getContrasena());
            ps.setString(3, this.getCorreo());
            ps.setString(4, this.getNumeroTelefono());
            ps.setInt(5, this.getEdad());
            ps.setString(6, this.getCc());
            ps.setDouble(7, this.getSaldoActual());
            ps.setInt(8, this.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
        }
    }

    // Elimina un usuario por su ID. Retorna true si se eliminó correctamente
    public static boolean eliminarUsuario(int id) {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection con = ConexionDB.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    // Interfaz funcional interna: permite pasar el parámetro de la consulta como lambda.
    // Ejemplo de uso: ps -> ps.setInt(1, id)
    @FunctionalInterface
    private interface ConsultaSQL {
        void preparar(PreparedStatement ps) throws SQLException;
    }
}
