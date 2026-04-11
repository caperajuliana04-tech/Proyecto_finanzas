import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainTest {
    public static void main(String[] args) {

        // =========================
        // 1. PRUEBAS DE OBJETOS
        // =========================
        Usuario usuario1 = new Usuario(
                1,
                "Carlos Rojas",
                "1234",
                "carlos@gmail.com",
                "3001234567",
                21,
                "1023456789",
                500000
        );

        Admin admin1 = new Admin(
                100,
                "Administrador",
                "admin123",
                "admin@gmail.com",
                "3000000000",
                30,
                "999999999"
        );

        System.out.println("=== PRUEBA 1: OBJETOS EN MEMORIA ===");
        usuario1.registrarUsuario();
        usuario1.leerPerfil();

        System.out.println("\nAdmin creado correctamente:");
        System.out.println("Nombre admin: " + admin1.getNombre());
        System.out.println("Correo admin: " + admin1.getCorreo());

        // =========================
        // 2. CONEXION SQLITE
        // =========================
        String url = "jdbc:sqlite:finanzas.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("\nConexion SQLite establecida correctamente.");

            // =========================
            // 3. CREAR TABLAS
            // =========================
            String sqlCrearUsuarios =
                    "CREATE TABLE IF NOT EXISTS usuarios (" +
                    "id INTEGER PRIMARY KEY, " +
                    "nombre TEXT NOT NULL, " +
                    "contrasena TEXT NOT NULL, " +
                    "correo TEXT NOT NULL, " +
                    "numero_telefono TEXT, " +
                    "edad INTEGER, " +
                    "cc TEXT NOT NULL, " +
                    "saldo_actual REAL" +
                    ");";

            String sqlCrearAdmins =
                    "CREATE TABLE IF NOT EXISTS admins (" +
                    "id INTEGER PRIMARY KEY, " +
                    "nombre TEXT NOT NULL, " +
                    "contrasena TEXT NOT NULL, " +
                    "correo TEXT NOT NULL, " +
                    "numero_telefono TEXT, " +
                    "edad INTEGER, " +
                    "cc TEXT NOT NULL" +
                    ");";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sqlCrearUsuarios);
                stmt.execute(sqlCrearAdmins);
                System.out.println("Tablas creadas o verificadas correctamente.");
            }

            // =========================
            // 4. INSERTAR DATOS DE PRUEBA
            // =========================
            String sqlInsertUsuario =
                    "INSERT OR REPLACE INTO usuarios " +
                    "(id, nombre, contrasena, correo, numero_telefono, edad, cc, saldo_actual) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

            String sqlInsertAdmin =
                    "INSERT OR REPLACE INTO admins " +
                    "(id, nombre, contrasena, correo, numero_telefono, edad, cc) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);";

            try (PreparedStatement psUsuario = conn.prepareStatement(sqlInsertUsuario);
                 PreparedStatement psAdmin = conn.prepareStatement(sqlInsertAdmin)) {

                psUsuario.setInt(1, usuario1.getId());
                psUsuario.setString(2, usuario1.getNombre());
                psUsuario.setString(3, usuario1.getContrasena());
                psUsuario.setString(4, usuario1.getCorreo());
                psUsuario.setString(5, usuario1.getNumeroTelefono());
                psUsuario.setInt(6, usuario1.getEdad());
                psUsuario.setString(7, usuario1.getCc());
                psUsuario.setDouble(8, usuario1.getSaldoActual());
                psUsuario.executeUpdate();

                psAdmin.setInt(1, admin1.getId());
                psAdmin.setString(2, admin1.getNombre());
                psAdmin.setString(3, admin1.getContrasena());
                psAdmin.setString(4, admin1.getCorreo());
                psAdmin.setString(5, admin1.getNumeroTelefono());
                psAdmin.setInt(6, admin1.getEdad());
                psAdmin.setString(7, admin1.getCc());
                psAdmin.executeUpdate();

                System.out.println("Datos insertados correctamente en SQLite.");
            }

            // =========================
            // 5. CONSULTAR USUARIOS
            // =========================
            System.out.println("\n=== PRUEBA 2: CONSULTAR USUARIOS DESDE SQLITE ===");
            String sqlSelectUsuarios = "SELECT * FROM usuarios";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlSelectUsuarios)) {

                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id"));
                    System.out.println("Nombre: " + rs.getString("nombre"));
                    System.out.println("Correo: " + rs.getString("correo"));
                    System.out.println("Telefono: " + rs.getString("numero_telefono"));
                    System.out.println("Edad: " + rs.getInt("edad"));
                    System.out.println("CC: " + rs.getString("cc"));
                    System.out.println("Saldo: " + rs.getDouble("saldo_actual"));
                    System.out.println("-----------------------------");
                }
            }

            // =========================
            // 6. CONSULTAR ADMINS
            // =========================
            System.out.println("\n=== PRUEBA 3: CONSULTAR ADMINS DESDE SQLITE ===");
            String sqlSelectAdmins = "SELECT * FROM admins";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlSelectAdmins)) {

                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id"));
                    System.out.println("Nombre: " + rs.getString("nombre"));
                    System.out.println("Correo: " + rs.getString("correo"));
                    System.out.println("Telefono: " + rs.getString("numero_telefono"));
                    System.out.println("Edad: " + rs.getInt("edad"));
                    System.out.println("CC: " + rs.getString("cc"));
                    System.out.println("-----------------------------");
                }
            }

            // =========================
            // 7. ACTUALIZAR USUARIO EN MEMORIA Y EN SQLITE
            // =========================
            System.out.println("\n=== PRUEBA 4: ACTUALIZAR USUARIO EN MEMORIA Y EN SQLITE ===");
            usuario1.actualizarInfo(
                    "Carlos Actualizado",
                    "nuevo_correo@gmail.com",
                    "3119998888",
                    22
            );
            usuario1.setSaldoActual(800000);

            String sqlUpdateUsuario =
                    "UPDATE usuarios " +
                    "SET nombre = ?, correo = ?, numero_telefono = ?, edad = ?, saldo_actual = ? " +
                    "WHERE id = ?;";

            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateUsuario)) {
                psUpdate.setString(1, usuario1.getNombre());
                psUpdate.setString(2, usuario1.getCorreo());
                psUpdate.setString(3, usuario1.getNumeroTelefono());
                psUpdate.setInt(4, usuario1.getEdad());
                psUpdate.setDouble(5, usuario1.getSaldoActual());
                psUpdate.setInt(6, usuario1.getId());
                psUpdate.executeUpdate();

                System.out.println("Usuario actualizado correctamente en SQLite.");
            }

            // =========================
            // 8. VERIFICAR ACTUALIZACION
            // =========================
            System.out.println("\n=== PRUEBA 5: VERIFICAR ACTUALIZACION EN SQLITE ===");
            String sqlBuscarUsuario = "SELECT * FROM usuarios WHERE id = ?";

            try (PreparedStatement psBuscar = conn.prepareStatement(sqlBuscarUsuario)) {
                psBuscar.setInt(1, usuario1.getId());

                try (ResultSet rs = psBuscar.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("ID: " + rs.getInt("id"));
                        System.out.println("Nombre: " + rs.getString("nombre"));
                        System.out.println("Correo: " + rs.getString("correo"));
                        System.out.println("Telefono: " + rs.getString("numero_telefono"));
                        System.out.println("Edad: " + rs.getInt("edad"));
                        System.out.println("CC: " + rs.getString("cc"));
                        System.out.println("Saldo: " + rs.getDouble("saldo_actual"));
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Ocurrio un error:");
            e.printStackTrace();
        }
    }
}