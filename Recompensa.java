import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Recompensa {
    private int idRecompensa;   
    private String tipo;
    private String mensaje; 
    private LocalDate fechaCreacion;
    private boolean desbloqueada;
    private int idUsuario;  

    public Recompensa(int idRecompensa, String tipo, String mensaje, LocalDate fechaCreacion, boolean desbloqueada, int idUsuario) {
        this.idRecompensa = idRecompensa;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fechaCreacion = fechaCreacion;
        this.desbloqueada = desbloqueada;
        this.idUsuario = idUsuario;
    }

    public static Recompensa crearRecompensa(String tipo, String mensaje, LocalDate fechaCreacion, int idUsuario) {
        Recompensa recompensa1 = new Recompensa(1, tipo, mensaje, fechaCreacion, false, idUsuario);
        return recompensa1;
    }

    public void eliminarRecompensa(int idRecompensa) {
        if (this.idRecompensa == idRecompensa) {
            System.out.println("Recompensa eliminada: " + this.idRecompensa);
        } else {
            System.out.println("No se encontró la recompensa con ID: " + idRecompensa);
        }
    }

    public static Recompensa leerRecompensa(int idRecompensa) {
        //Con el id buscar la recompensa 
        //Mostrar recompensa 
        Recompensa recompensaLeida = new Recompensa(idRecompensa, "Tipo de recompensa", "Mensaje de recompensa", LocalDate.now(), false, 1);
        return recompensaLeida;
    }

    public List<Recompensa> listarRecompensas(int idUsuario) {
        List<Recompensa> recompensas = new ArrayList<>();
        // Buscar en la base de datos las recompensas del usuario y agregar a lista
        return recompensas;
    }
}
