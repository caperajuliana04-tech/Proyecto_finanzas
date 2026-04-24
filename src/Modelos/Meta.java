package Modelos;

import db.ConexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Representa una meta de ahorro que un usuario desea alcanzar.
// Gestiona la tabla "meta" con operaciones de inserción, listado y eliminación.
public class Meta {

    private int idMeta;              // ID autogenerado por la base de datos
    private int idUsuario;           // ID del usuario dueño de la meta
    private double montoObjetivo;    // Monto total que se quiere ahorrar
    private double montoActual;      // Monto ahorrado hasta el momento
    private LocalDate fechaRegistro; // Fecha en que se creó la meta
    private LocalDate fechaEsperada; // Fecha límite para alcanzar la meta
    private String nombre;           // Nombre descriptivo de la meta (ej: "Vacaciones")

    // Constructor completo
    public Meta(int idMeta, int idUsuario, double montoObjetivo, double montoActual,
                LocalDate fechaRegistro, LocalDate fechaEsperada, String nombre) {
        this.idMeta = idMeta;
        this.idUsuario = idUsuario;
        this.montoObjetivo = montoObjetivo;
        this.montoActual = montoActual;
        this.fechaRegistro = fechaRegistro;
        this.fechaEsperada = fechaEsperada;
        this.nombre = nombre;
    }

    // --- Getters (solo lectura) ---
    public int getIdMeta() { return idMeta; }
    public int getIdUsuario() { return idUsuario; }
    public double getMontoObjetivo() { return montoObjetivo; }
    public double getMontoActual() { return montoActual; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public LocalDate getFechaEsperada() { return fechaEsperada; }
    public String getNombre() { return nombre; }

    // Calcula el porcentaje de avance de la meta.
    // Evita división por cero si el objetivo es 0.
    public double calcularProgreso() {
        return montoObjetivo == 0 ? 0 : (montoActual / montoObjetivo) * 100;
    }

    // Inserta esta meta en la base de datos
    public void agregarMeta() {
        String sql = "INSERT INTO meta (nombre, monto_objetivo, monto_actual, fecha_registro, fecha_esperada, id_usuario) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setDouble(2, this.montoObjetivo);
            ps.setDouble(3, this.montoActual);
            ps.setString(4, this.fechaRegistro.toString()); // "YYYY-MM-DD"
            ps.setString(5, this.fechaEsperada.toString());
            ps.setInt(6, this.idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al agregar meta: " + e.getMessage());
        }
    }

    // Retorna todas las metas de un usuario ordenadas de la más reciente a la más antigua
    public static List<Meta> listarMetasPorUsuario(int idUsuario) {
        List<Meta> lista = new ArrayList<>();
        String sql = "SELECT * FROM meta WHERE id_usuario = ? ORDER BY fecha_registro DESC";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            // Construye un objeto Meta por cada fila
            while (rs.next()) {
                lista.add(new Meta(
                    rs.getInt("id_meta"),
                    rs.getInt("id_usuario"),
                    rs.getDouble("monto_objetivo"),
                    rs.getDouble("monto_actual"),
                    LocalDate.parse(rs.getString("fecha_registro")),
                    LocalDate.parse(rs.getString("fecha_esperada")),
                    rs.getString("nombre")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar metas: " + e.getMessage());
        }
        return lista;
    }

    // Elimina una meta por su ID. Retorna true si se eliminó correctamente
    public static boolean eliminarMeta(int idMeta) {
        String sql = "DELETE FROM meta WHERE id_meta = ?";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMeta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar meta: " + e.getMessage());
            return false;
        }
    }
}
