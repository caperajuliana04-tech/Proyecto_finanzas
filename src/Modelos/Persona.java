package Modelos;

// Clase base (padre) que representa a cualquier persona del sistema.
// Tanto Usuario como Admin heredan de esta clase y comparten estos atributos.
public class Persona {

    private int id;              // Identificador numérico único de la persona
    private String nombre;       // Nombre completo
    private String contrasena;   // Contraseña en texto plano (uso académico)
    private String correo;       // Correo electrónico, usado para iniciar sesión
    private String numeroTelefono;
    private int edad;
    private String cc;           // Número de cédula / documento de identidad

    // Constructor: inicializa todos los campos de la persona
    public Persona(int id, String nombre, String contrasena, String correo,
                String numeroTelefono, int edad, String cc) {
        this.id = id;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.correo = correo;
        this.numeroTelefono = numeroTelefono;
        this.edad = edad;
        this.cc = cc;
    }

    // --- Getters y setters ---
    // Permiten leer y modificar cada campo desde otras clases

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getNumeroTelefono() { return numeroTelefono; }
    public void setNumeroTelefono(String numeroTelefono) { this.numeroTelefono = numeroTelefono; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getCc() { return cc; }
    public void setCc(String cc) { this.cc = cc; }
}
