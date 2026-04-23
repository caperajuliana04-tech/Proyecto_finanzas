package Test;

import Modelos.Usuario;

public class MainTestActualizarUsuario {

    public static void main(String[] args) {
        int idUsuario = 1001; // pon aquí un ID que ya exista en la base

        System.out.println("=== INICIANDO PRUEBA DE ACTUALIZAR USUARIO ===");

        // 1. Leer el usuario actual desde la base
        Usuario usuario = Usuario.leerPerfil(idUsuario);

        if (usuario == null) {
            System.out.println("No se encontró un usuario con el ID: " + idUsuario);
            return;
        }

        System.out.println("\n--- DATOS ANTES DE ACTUALIZAR ---");
        System.out.println("ID: " + usuario.getId());
        System.out.println("Nombre: " + usuario.getNombre());
        System.out.println("Correo: " + usuario.getCorreo());
        System.out.println("Telefono: " + usuario.getNumeroTelefono());
        System.out.println("Edad: " + usuario.getEdad());
        System.out.println("CC: " + usuario.getCc());
        System.out.println("Saldo: " + usuario.getSaldoActual());

        // 2. Cambiar datos en memoria
        usuario.setNombre("Cristian Actualizado");
        usuario.setContrasena("claveNueva123");
        usuario.setCorreo("cristian.actualizado@correo.com");
        usuario.setNumeroTelefono("3119998888");
        usuario.setEdad(30);
        usuario.setCc("987654321");
        usuario.setSaldoActual(250000.0);

        // 3. Guardar cambios en la base
        usuario.actualizarUsuario();

        // 4. Volver a leer desde la base para confirmar
        Usuario usuarioActualizado = Usuario.leerPerfil(idUsuario);

        

        if (usuarioActualizado != null) {
            System.out.println("\n--- DATOS DESPUES DE ACTUALIZAR ---");
            System.out.println("ID: " + usuarioActualizado.getId());
            System.out.println("Nombre: " + usuarioActualizado.getNombre());
            System.out.println("Correo: " + usuarioActualizado.getCorreo());
            System.out.println("Telefono: " + usuarioActualizado.getNumeroTelefono());
            System.out.println("Edad: " + usuarioActualizado.getEdad());
            System.out.println("CC: " + usuarioActualizado.getCc());
            System.out.println("Saldo: " + usuarioActualizado.getSaldoActual());
        } else {
            System.out.println("Error: no se pudo leer el usuario después de actualizarlo.");
        }
    }
}
