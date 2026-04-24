package Modelos;

import db.ConexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Representa una alerta o recordatorio asociado a un usuario.
// Puede estar pendiente (no leída) o marcada como leída.
// Gestiona la tabla "alerta".
public class Alerta {

    private int idAlerta;    // ID autogenerado por la base de datos
    private String tipo;     // Tipo de alerta (ej: "Presupuesto", "Recordatorio")
    private String mensaje;  // Texto descriptivo de la alerta
    private LocalDate fecha; // Fecha de la alerta
    private boolean leida;   // true si el usuario ya la marcó como leída
    private int idUsuario;   // ID del usuario al que pertenece esta alerta

    // Constructor completo
    public Alerta(int idAlerta, String tipo, String mensaje, LocalDate fecha, boolean leida, int idUsuario) {
        this.idAlerta = idAlerta;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.leida = leida;
        this.idUsuario = idUsuario;
    }

    // --- Getters (solo lectura) ---
    public int getIdAlerta() { return idAlerta; }
    public String getTipo() { return tipo; }
    public String getMensaje() { return mensaje; }
    public LocalDate getFecha() { return fecha; }
    public boolean isLeida() { return leida; }
    public int getIdUsuario() { return idUsuario; }

    // Inserta esta alerta en la base de datos.
    // El campo "leida" se guarda como entero: 1 = leída, 0 = pendiente (SQLite no tiene booleano nativo)
    public void crearAlerta() {
        String sql = "INSERT INTO alerta (tipo, mensaje, fecha, leida, id_usuario) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.tipo);
            ps.setString(2, this.mensaje);
            ps.setString(3, this.fecha.toString()); // "YYYY-MM-DD"
            ps.setInt(4, this.leida ? 1 : 0);       // booleano → entero
            ps.setInt(5, this.idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al crear alerta: " + e.getMessage());
        }
    }

    // Marca esta alerta como leída en la base de datos y actualiza el campo local
    public void marcarComoLeida() {
        String sql = "UPDATE alerta SET leida = 1 WHERE id_alerta = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, this.idAlerta);
            // Si se actualizó al menos una fila, refleja el cambio en el objeto también
            if (ps.executeUpdate() > 0) this.leida = true;
        } catch (SQLException e) {
            System.out.println("Error al marcar alerta: " + e.getMessage());
        }
    }

    // Retorna todas las alertas de un usuario ordenadas de la más reciente a la más antigua
    public static List<Alerta> listarAlertasPorUsuario(int idUsuario) {
        List<Alerta> lista = new ArrayList<>();
        String sql = "SELECT * FROM alerta WHERE id_usuario = ? ORDER BY fecha DESC";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            // Construye un objeto Alerta por cada fila; convierte el entero "leida" a booleano
            while (rs.next()) {
                lista.add(new Alerta(
                    rs.getInt("id_alerta"),
                    rs.getString("tipo"),
                    rs.getString("mensaje"),
                    LocalDate.parse(rs.getString("fecha")),
                    rs.getInt("leida") == 1, // 1 → true, 0 → false
                    rs.getInt("id_usuario")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar alertas: " + e.getMessage());
        }
        return lista;
    }

    // Elimina una alerta por su ID. Retorna true si se eliminó correctamente
    public static boolean eliminarAlerta(int idAlerta) {
        String sql = "DELETE FROM alerta WHERE id_alerta = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAlerta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar alerta: " + e.getMessage());
            return false;
        }
    }
}
