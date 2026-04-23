package Test;

import Modelos.Admin;

public class MainTestAdmin {
    public static void main(String[] args) {

        // 1) CREAR un admin nuevo en memoria
        Admin admin1 = new Admin(
            101,
            "Carlos Rojas",
            "clave123",
            "carlos@correo.com",
            "3001234567",
            30,
            "1234567890"
        );

        // 2) REGISTRAR el admin en la base de datos
        System.out.println("----- REGISTRAR ADMIN -----");
        admin1.registrarAdmin();

        // 3) LEER el admin desde la base de datos usando el id
        System.out.println("\n----- LEER ADMIN -----");
        Admin adminLeido = Admin.leerPerfil(101);

        if (adminLeido != null) {
            System.out.println("ID: " + adminLeido.getId());
            System.out.println("Nombre: " + adminLeido.getNombre());
            System.out.println("Correo: " + adminLeido.getCorreo());
            System.out.println("Telefono: " + adminLeido.getNumeroTelefono());
            System.out.println("Edad: " + adminLeido.getEdad());
            System.out.println("CC: " + adminLeido.getCc());
        } else {
            System.out.println("No se encontró el admin.");
        }

        // 4) ACTUALIZAR el admin
        System.out.println("\n----- ACTUALIZAR ADMIN -----");
        Admin adminActualizado = new Admin(
            101,
            "Carlos Rojas Actualizado",
            "nuevaClave456",
            "carlos_actualizado@correo.com",
            "3119998888",
            31,
            "1234567890"
        );

        adminActualizado.actualizarAdmin();

        // 5) LEER otra vez para comprobar que sí cambió
        System.out.println("\n----- LEER ADMIN ACTUALIZADO -----");
        Admin adminDespuesDeActualizar = Admin.leerPerfil(101);

        if (adminDespuesDeActualizar != null) {
            System.out.println("ID: " + adminDespuesDeActualizar.getId());
            System.out.println("Nombre: " + adminDespuesDeActualizar.getNombre());
            System.out.println("Correo: " + adminDespuesDeActualizar.getCorreo());
            System.out.println("Telefono: " + adminDespuesDeActualizar.getNumeroTelefono());
            System.out.println("Edad: " + adminDespuesDeActualizar.getEdad());
            System.out.println("CC: " + adminDespuesDeActualizar.getCc());
        } else {
            System.out.println("No se encontró el admin.");
        }

        // 6) ELIMINAR el admin
        System.out.println("\n----- ELIMINAR ADMIN -----");
        boolean eliminado = Admin.eliminarAdmin(101);

        if (eliminado) {
            System.out.println("El admin fue eliminado correctamente.");
        } else {
            System.out.println("No se pudo eliminar el admin.");
        }

        // 7) LEER después de eliminar para comprobar que ya no existe
        System.out.println("\n----- VERIFICAR ELIMINACION -----");
        Admin adminEliminado = Admin.leerPerfil(101);

        if (adminEliminado == null) {
            System.out.println("Correcto: el admin ya no existe en la base de datos.");
        } else {
            System.out.println("Error: el admin todavía existe.");
        }
    }
}