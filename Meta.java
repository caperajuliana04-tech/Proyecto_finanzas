import java.time.LocalDate;
public class Meta {
    private int idMeta, idUsuario;
    private double montoObjetivo, montoActual;
    private LocalDate fechaRegistro;
    private LocalDate fechaEsperada;
    private String nombre;

  public Meta(int idMeta, int idUsuario, double montoObjetivo, LocalDate fechaRegistro, LocalDate fechaEsperada, String nombre) {
        this.idMeta = idMeta;
        this.idUsuario = idUsuario;
        this.montoObjetivo = montoObjetivo;
        this.fechaRegistro = fechaRegistro;
        this.fechaEsperada = fechaEsperada;
        this.nombre = nombre;
    }

    public int getIdMeta() {
        return idMeta;
    }

    public void setIdMeta(int idMeta) {
        this.idMeta = idMeta;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public double getMontoObjetivo() {
        return montoObjetivo;
    }

    public void setMontoObjetivo(double montoObjetivo) {
        this.montoObjetivo = montoObjetivo;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDate getFechaEsperada() {
        return fechaEsperada;
    }

    public void setFechaEsperada(LocalDate fechaEsperada) {
        this.fechaEsperada = fechaEsperada;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void agregarMeta(int idUsuario, double montoObjetivo, LocalDate fechaRegistro, LocalDate fechaEsperada, String nombre) {

    }
    public void actualizarMeta(int idMeta, double nuevoMontoObjetivo, LocalDate nuevaFechaEsperada, String nuevoNombre) {
        if (this.idMeta == idMeta) {
            this.montoObjetivo = nuevoMontoObjetivo;
            this.fechaEsperada = nuevaFechaEsperada;
            this.nombre = nuevoNombre;
        }
    }
    public void eliminarMeta(int idMeta) {
        if (this.idMeta == idMeta) {
            
        }
    }
    


}
