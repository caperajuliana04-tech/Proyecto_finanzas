package Modelos;

import db.ConexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Representa un ingreso de dinero registrado por un usuario.
// Gestiona la tabla "ingreso" y sincroniza el saldo del usuario al insertar o eliminar.
public class Ingreso {

    private int idIngreso;   // ID autogenerado por la base de datos
    private int idUsuario;   // ID del usuario dueño de este ingreso
    private double monto;    // Cantidad de dinero ingresada
    private LocalDate fecha; // Fecha en que se registró el ingreso
    private String concepto; // Descripción del ingreso (ej: "Salario", "Freelance")

    // Constructor completo
    public Ingreso(int idIngreso, int idUsuario, double monto, LocalDate fecha, String concepto) {
        this.idIngreso = idIngreso;
        this.idUsuario = idUsuario;
        this.monto = monto;
        this.fecha = fecha;
        this.concepto = concepto;
    }

    // --- Getters (solo lectura, no se necesitan setters en las vistas actuales) ---
    public int getIdIngreso() { return idIngreso; }
    public int getIdUsuario() { return idUsuario; }
    public double getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public String getConcepto() { return concepto; }

    // Inserta el ingreso en la tabla "ingreso" y suma el monto al saldo del usuario.
    // Usa la misma conexión para ambas operaciones, evitando abrir la DB dos veces.
    public void registrarIngreso() {
        String sqlInsert = "INSERT INTO ingreso (monto, fecha, concepto, id_usuario) VALUES (?, ?, ?, ?)";
        String sqlSaldo  = "UPDATE usuario SET saldo_actual = saldo_actual + ? WHERE id = ?";
        try (Connection con = ConexionDB.conectar()) {
            // 1) Insertar el registro del ingreso
            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setDouble(1, this.monto);
                ps.setString(2, this.fecha.toString()); // LocalDate se guarda como "YYYY-MM-DD"
                ps.setString(3, this.concepto);
                ps.setInt(4, this.idUsuario);
                ps.executeUpdate();
            }
            // 2) Sumar el monto al saldo del usuario
            try (PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
                ps.setDouble(1, this.monto);
                ps.setInt(2, this.idUsuario);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error al registrar ingreso: " + e.getMessage());
        }
    }

    // Retorna todos los ingresos de un usuario ordenados del más reciente al más antiguo
    public static List<Ingreso> listarIngresosPorUsuario(int idUsuario) {
        List<Ingreso> lista = new ArrayList<>();
        String sql = "SELECT * FROM ingreso WHERE id_usuario = ? ORDER BY fecha DESC";
        try (Connection con = ConexionDB.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            // Recorre cada fila del resultado y construye un objeto Ingreso por fila
            while (rs.next()) {
                lista.add(new Ingreso(
                    rs.getInt("id_ingreso"),
                    rs.getInt("id_usuario"),
                    rs.getDouble("monto"),
                    LocalDate.parse(rs.getString("fecha")), // convierte String → LocalDate
                    rs.getString("concepto")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar ingresos: " + e.getMessage());
        }
        return lista;
    }

    // Elimina un ingreso y descuenta su monto del saldo del usuario.
    // Primero consulta el monto y el idUsuario para poder revertir el saldo correctamente.
    public static boolean eliminarIngreso(int idIngreso) {
        String sqlMonto  = "SELECT monto, id_usuario FROM ingreso WHERE id_ingreso = ?";
        String sqlDelete = "DELETE FROM ingreso WHERE id_ingreso = ?";
        String sqlSaldo  = "UPDATE usuario SET saldo_actual = saldo_actual - ? WHERE id = ?";
        try (Connection con = ConexionDB.conectar()) {
            double monto;
            int idUsuario;
            // 1) Obtener el monto y dueño del ingreso antes de borrarlo
            try (PreparedStatement ps = con.prepareStatement(sqlMonto)) {
                ps.setInt(1, idIngreso);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return false; // No existe el ingreso
                monto = rs.getDouble("monto");
                idUsuario = rs.getInt("id_usuario");
            }
            // 2) Borrar el ingreso
            try (PreparedStatement ps = con.prepareStatement(sqlDelete)) {
                ps.setInt(1, idIngreso);
                ps.executeUpdate();
            }
            // 3) Restar el monto al saldo del usuario
            try (PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
                ps.setDouble(1, monto);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Error al eliminar ingreso: " + e.getMessage());
            return false;
        }
    }
}
