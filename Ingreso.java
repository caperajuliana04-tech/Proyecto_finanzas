import java.time.LocalDate;
import java.util.*;
public class Ingreso {
    private int idIngreso,idUsuario;
    private double monto;
    private LocalDate fecha;
    private String concepto;
    private ArrayList<Ingreso> ingresos = new ArrayList<>();
    
    public Ingreso(int idIngreso, int idUsuario, double monto, LocalDate fecha, String concepto) {
        this.idIngreso = idIngreso;
        this.idUsuario = idUsuario;
        this.monto = monto;
        this.fecha = fecha;
        this.concepto = concepto;
    }
    public int getIdIngreso() {
        return idIngreso;
    }
    public void setIdIngreso(int idIngreso) {
        this.idIngreso = idIngreso;
    }
    public int getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
    public double getMonto() {
        return monto;
    }
    public void setMonto(double monto) {
        this.monto = monto;
    }
    public LocalDate getFecha() {
        return fecha;
    }
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    public String getConcepto() {
        return concepto;
    }
    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }
    public double registrarIngreso(int monto, String concepto,LocalDate fecha) {
        double saldoActual = leerSaldoActual();
        saldoActual += monto;
        return saldoActual;
    }
    public double leerSaldoActual() {
        double saldoActual =50000.0;
        return saldoActual;
    }
    public Ingreso leeIngreso(int idIngreso) {
        if (this.idIngreso==idIngreso) {
            return this;
        } else {
            return null;
        }
    }   
    public ArrayList<Ingreso> listarIngresosPorUsuario(int idUsuario) {
        for (Ingreso ingreso : ingresos) {
            if (ingreso.getIdUsuario() == idUsuario) {
                ingresos.add(ingreso);
            }
        }
        return ingresos;    
    }
    public void eliminarIngreso(int idIngreso) {
        for (Ingreso ingreso : ingresos) {
            if (ingreso.getIdIngreso() == idIngreso) {
                ingresos.remove(ingreso);   
            }       
        }
    }
    public void actualizarIngreso(int idIngreso, int monto, String concepto, LocalDate fecha) {
        for (Ingreso ingreso : ingresos) {
            if (ingreso.getIdIngreso() == idIngreso) {
                ingreso.setMonto(monto);
                ingreso.setConcepto(concepto);
                ingreso.setFecha(fecha);
            }
        }
    }
}
