package Modelos;
public class Usuario extends Persona {
    private double saldoActual;

    public Usuario (int id, String nombre, String contrasena, String correo, String numeroTelefono, int edad, String cc, double saldoActual) {
        super(id, nombre, contrasena, correo, numeroTelefono, edad, cc);
        this.saldoActual = saldoActual;
    }

    public double getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(double saldoActual) {
        this.saldoActual = saldoActual;
    }

    
    public void registrarUsuario() {
        System.out.println("Usuario registrado correctamente.");
    }

    public void leerPerfil() {
        System.out.println("ID: " + getId());
        System.out.println("Nombre: " + getNombre());
        System.out.println("Correo: " + getCorreo());
        System.out.println("Telefono: " + getNumeroTelefono());
        System.out.println("Edad: " + getEdad());
        System.out.println("CC: " + getCc());
        System.out.println("Saldo actual: " + saldoActual);
    }


    }


    

