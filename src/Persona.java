public class Persona {
    private int id;
    private String nombre;
    private String contrasena;
    private String correo;
    private String numeroTelefono;
    private int edad;
    private String cc;


public Persona (int id, String nombre, String contrasena, String correo, String numeroTelefono, int edad, String cc) {
    this.id = id;
    this.nombre = nombre;
    this.contrasena = contrasena;
    this.correo = correo;
    this.numeroTelefono = numeroTelefono;
    this.edad = edad;
    this.cc = cc;
}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public int getEdad() {
        return edad;
    }

    public void actualizarInfo(String nombre, String correo, String numeroTelefono, int edad) {
        this.nombre = nombre;
        this.correo = correo;
        this.numeroTelefono = numeroTelefono;
        this.edad = edad;
    }

}