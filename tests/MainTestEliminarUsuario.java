package Test;

import Modelos.Usuario;

public class MainTestEliminarUsuario {

    public static void main(String[] args) {
        int idUsuario = 1001; // cambia este valor por un ID que sí exista

        System.out.println("=== INICIANDO PRUEBA DE ELIMINAR USUARIO ===");

        // 1. Verificar si el usuario existe antes de eliminarlo
        Usuario usuarioAntes = Usuario.leerPerfil(idUsuario);
        

        if (usuarioAntes == null) {
            System.out.println("No se encontró un usuario con el ID: " + idUsuario);
            System.out.println("No se puede probar la eliminación porque ese usuario no existe.");
            return;
        }

        System.out.println("\n--- USUARIO ENCONTRADO ANTES DE ELIMINAR ---");
        System.out.println("ID: " + usuarioAntes.getId());
        System.out.println("Nombre: " + usuarioAntes.getNombre());
        System.out.println("Correo: " + usuarioAntes.getCorreo());
        System.out.println("Telefono: " + usuarioAntes.getNumeroTelefono());
        System.out.println("Edad: " + usuarioAntes.getEdad());
        System.out.println("CC: " + usuarioAntes.getCc());
        System.out.println("Saldo: " + usuarioAntes.getSaldoActual());

        // 2. Ejecutar eliminación
        boolean eliminado = Usuario.eliminarUsuario(idUsuario);

        // 3. Verificar si realmente ya no existe
        Usuario usuarioDespues = Usuario.leerPerfil(idUsuario);

        System.out.println("\n--- RESULTADO DE LA ELIMINACION ---");
        if (eliminado && usuarioDespues == null) {
            System.out.println("Prueba exitosa: el usuario fue eliminado correctamente.");
        } else if (!eliminado) {
            System.out.println("La eliminación devolvió false.");
        } else {
            System.out.println("Algo salió mal: el método indicó éxito, pero el usuario aún aparece en la base.");
        }
    }
}