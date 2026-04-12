package Modelos;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Alerta {
    private int idAlerta;
    private String tipo;
    private String mensaje;
    private LocalDate fecha;
    private boolean leida;
    private int idUsuario;

    public Alerta(int idAlerta, String tipo, String mensaje, LocalDate fecha, boolean leida, int idUsuario) {
        this.idAlerta = idAlerta;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.leida = leida;
        this.idUsuario = idUsuario;
    }

    public int getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(int idAlerta) {
        this.idAlerta = idAlerta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void crearAlerta() {
        System.out.println("Alerta creada correctamente.");
    }

    public void eliminarAlerta(int idAlerta) {
        if (this.idAlerta == idAlerta) {
            System.out.println("Alerta eliminada correctamente.");
        } else {
            System.out.println("No se encontro una alerta con ese id.");
        }
    }

    public Alerta leerAlerta(int idAlerta) {
        if (this.idAlerta == idAlerta) {
            return this;
        }
        return null;
    }

    public List<Alerta> listarAlertasPorUsuario(int idUsuario) {
        List<Alerta> listaAlertas = new ArrayList<>();

        if (this.idUsuario == idUsuario) {
            listaAlertas.add(this);
        }

        return listaAlertas;
    }

    public void marcarComoLeida() {
        this.leida = true;
        System.out.println("La alerta fue marcada como leida.");
    }
}
