package Test;

import Modelos.Usuario;

public class MainTestRegistrarUsuario {

    public static void main(String[] args) {
        // Usa un ID que no exista ya en la base
        int idPrueba = 1002;

        // 1. Crear el objeto Usuario en memoria
        Usuario usuario = new Usuario(
            idPrueba,
            "Juliana Triana",
            "clave123",
            "juliana@correo.com",
            "3001234567",
            25,
            "1234567891",
            150000.0
        );

        // 2. Intentar registrarlo en la base de datos
        System.out.println("=== INICIANDO PRUEBA DE registrarUsuario() ===");
        usuario.registrarUsuario();

        // 3. Leer el usuario desde la base para comprobar que sí quedó guardado
        Usuario usuarioLeido = Usuario.leerPerfil(idPrueba);

        // 4. Validar resultado
        if (usuarioLeido != null) {
            System.out.println("=== PRUEBA EXITOSA ===");
            System.out.println("ID: " + usuarioLeido.getId());
            System.out.println("Nombre: " + usuarioLeido.getNombre());
            System.out.println("Correo: " + usuarioLeido.getCorreo());
            System.out.println("Telefono: " + usuarioLeido.getNumeroTelefono());
            System.out.println("Edad: " + usuarioLeido.getEdad());
            System.out.println("CC: " + usuarioLeido.getCc());
            System.out.println("Saldo: " + usuarioLeido.getSaldoActual());
        } else {
            System.out.println("=== PRUEBA FALLIDA ===");
            System.out.println("No se pudo leer el usuario después de registrarlo.");
        }
    }


    


    


}
