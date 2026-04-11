public class Admin extends Persona {

    public Admin (int id, String nombre, String contrasena, String correo, String numeroTelefono, int edad, String cc) {
        super(id, nombre, contrasena, correo, numeroTelefono, edad, cc);
    }

    public Usuario buscarUsuario(int id) {
        return null;
    }

    public boolean eliminarUsuario(int id) {
        return false;
    }

    public void listarUsuarios() {

    }
}

