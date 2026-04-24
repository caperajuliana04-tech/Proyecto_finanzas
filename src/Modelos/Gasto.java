package Modelos;

import db.ConexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Representa un gasto registrado por un usuario.
// Gestiona la tabla "gasto" y sincroniza el saldo del usuario al insertar o eliminar.
public class Gasto {

    private int idGasto;       // ID autogenerado por la base de datos
    private int idUsuario;     // ID del usuario dueño de este gasto
    private double monto;      // Cantidad de dinero gastada
    private LocalDate fecha;   // Fecha en que ocurrió el gasto
    private String nombre;     // Nombre del gasto (ej: "Mercado", "Netflix")
    private String categoria;  // Categoría del gasto (ej: "Alimentación", "Entretenimiento")

    // Constructor completo
    public Gasto(int idGasto, int idUsuario, double monto, LocalDate fecha, String nombre, String categoria) {
        this.idGasto = idGasto;
        this.idUsuario = idUsuario;
        this.monto = monto;
        this.fecha = fecha;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    // --- Getters (solo lectura) ---
    public int getIdGasto() { return idGasto; }
    public int getIdUsuario() { return idUsuario; }
    public double getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }

    // Inserta el gasto en la tabla "gasto" y resta el monto del saldo del usuario.
    // Usa la misma conexión para ambas operaciones, evitando abrir la DB dos veces.
    public void agregarGasto() {
        String sqlInsert = "INSERT INTO gasto (nombre, monto, fecha, categoria, id_usuario) VALUES (?, ?, ?, ?, ?)";
        String sqlSaldo  = "UPDATE usuario SET saldo_actual = saldo_actual - ? WHERE id = ?";
        try (Connection con = ConexionDB.conectar()) {
            // 1) Insertar el registro del gasto
            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setString(1, this.nombre);
                ps.setDouble(2, this.monto);
                ps.setString(3, this.fecha.toString()); // LocalDate como "YYYY-MM-DD"
                ps.setString(4, this.categoria);
                ps.setInt(5, this.idUsuario);
                ps.executeUpdate();
            }
            // 2) Restar el monto del saldo del usuario
            try (PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
                ps.setDouble(1, this.monto);
                ps.setInt(2, this.idUsuario);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error al agregar gasto: " + e.getMessage());
        }
    }

    // Retorna todos los gastos de un usuario ordenados del más reciente al más antiguo
    public static List<Gasto> listarGastosPorUsuario(int idUsuario) {
        List<Gasto> lista = new ArrayList<>();
        String sql = "SELECT * FROM gasto WHERE id_usuario = ? ORDER BY fecha DESC";
        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            // Construye un objeto Gasto por cada fila retornada
            while (rs.next()) {
                lista.add(new Gasto(
                    rs.getInt("id_gasto"),
                    rs.getInt("id_usuario"),
                    rs.getDouble("monto"),
                    LocalDate.parse(rs.getString("fecha")), // String "YYYY-MM-DD" → LocalDate
                    rs.getString("nombre"),
                    rs.getString("categoria")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar gastos: " + e.getMessage());
        }
        return lista;
    }

    // Elimina un gasto y suma su monto de vuelta al saldo del usuario (reversa del gasto).
    // Primero consulta el monto y el idUsuario para poder revertir el saldo correctamente.
    public static boolean eliminarGasto(int idGasto) {
        String sqlMonto  = "SELECT monto, id_usuario FROM gasto WHERE id_gasto = ?";
        String sqlDelete = "DELETE FROM gasto WHERE id_gasto = ?";
        String sqlSaldo  = "UPDATE usuario SET saldo_actual = saldo_actual + ? WHERE id = ?";
        try (Connection con = ConexionDB.conectar()) {
            double monto;
            int idUsuario;
            // 1) Obtener el monto y dueño del gasto antes de borrarlo
            try (PreparedStatement ps = con.prepareStatement(sqlMonto)) {
                ps.setInt(1, idGasto);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return false; // No existe el gasto
                monto = rs.getDouble("monto");
                idUsuario = rs.getInt("id_usuario");
            }
            // 2) Borrar el gasto
            try (PreparedStatement ps = con.prepareStatement(sqlDelete)) {
                ps.setInt(1, idGasto);
                ps.executeUpdate();
            }
            // 3) Devolver el monto al saldo del usuario
            try (PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
                ps.setDouble(1, monto);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Error al eliminar gasto: " + e.getMessage());
            return false;
        }
    }
}
