package Modelos;

import db.ConexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Representa una recompensa o logro que un usuario puede desbloquear.
// Gestiona la tabla "recompensa".
public class Recompensa {

    private int idRecompensa;         // ID autogenerado por la base de datos
    private String tipo;              // Tipo de recompensa (ej: "Medalla", "Insignia")
    private String mensaje;           // Descripción del logro (ej: "¡Primera meta cumplida!")
    private LocalDate fechaDesbloqueo;// Fecha en que se desbloqueó o se asignó la recompensa
    private boolean desbloqueada;     // true si el usuario ya la obtuvo
    private int idUsuario;            // ID del usuario al que pertenece

    // Constructor completo
    public Recompensa(int idRecompensa, String tipo, String mensaje, LocalDate fechaDesbloqueo,
                      boolean desbloqueada, int idUsuario) {
        this.idRecompensa = idRecompensa;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fechaDesbloqueo = fechaDesbloqueo;
        this.desbloqueada = desbloqueada;
        this.idUsuario = idUsuario;
    }

    // --- Getters (solo lectura) ---
    public int getIdRecompensa() { return idRecompensa; }
    public String getTipo() { return tipo; }
    public String getMensaje() { return mensaje; }
    public LocalDate getFechaDesbloqueo() { return fechaDesbloqueo; }
    public boolean isDesbloqueada() { return desbloqueada; }
    public int getIdUsuario() { return idUsuario; }

    // Inserta esta recompensa en la base de datos.
    // "desbloqueada" se guarda como entero (1/0) porque SQLite no tiene tipo booleano nativo.
    public void crearRecompensa() {
        String sql = "INSERT INTO recompensa (tipo, mensaje, fecha_desbloqueo, desbloqueada, id_usuario) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.tipo);
            ps.setString(2, this.mensaje);
            ps.setString(3, this.fechaDesbloqueo.toString()); // "YYYY-MM-DD"
            ps.setInt(4, this.desbloqueada ? 1 : 0);          // booleano → entero
            ps.setInt(5, this.idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al crear recompensa: " + e.getMessage());
        }
    }

    // Verifica si ya existe una recompensa del mismo tipo para este usuario.
    // Evita otorgar la misma recompensa más de una vez.
    public static boolean existeRecompensa(int idUsuario, String tipo) {
        String sql = "SELECT COUNT(*) FROM recompensa WHERE id_usuario = ? AND tipo = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, tipo);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Error al verificar recompensa: " + e.getMessage());
            return false;
        }
    }

    // Retorna todas las recompensas de un usuario ordenadas de la más reciente a la más antigua
    public static List<Recompensa> listarRecompensasPorUsuario(int idUsuario) {
        List<Recompensa> lista = new ArrayList<>();
        String sql = "SELECT * FROM recompensa WHERE id_usuario = ? ORDER BY fecha_desbloqueo DESC";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            // Construye un objeto Recompensa por cada fila; convierte "desbloqueada" de int a boolean
            while (rs.next()) {
                lista.add(new Recompensa(
                    rs.getInt("id_recompensa"),
                    rs.getString("tipo"),
                    rs.getString("mensaje"),
                    LocalDate.parse(rs.getString("fecha_desbloqueo")),
                    rs.getInt("desbloqueada") == 1, // 1 → true, 0 → false
                    rs.getInt("id_usuario")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar recompensas: " + e.getMessage());
        }
        return lista;
    }

    // Elimina una recompensa por su ID. Retorna true si se eliminó correctamente
    public static boolean eliminarRecompensa(int idRecompensa) {
        String sql = "DELETE FROM recompensa WHERE id_recompensa = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRecompensa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar recompensa: " + e.getMessage());
            return false;
        }
    }
}
