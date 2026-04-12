package Modelos;
import java.time.LocalDate;
import java.util.*;
public class Gasto {
    private int idGasto,idUsuario;
    private double montoGastado;
    private LocalDate fecha;
    private String concepto;
    private ArrayList<Gasto> gastos = new ArrayList<>();
    
    public Gasto(int idGasto, int idUsuario, double montoGastado, LocalDate fecha, String concepto) {
        this.idGasto = idGasto;
        this.idUsuario = idUsuario;
        this.montoGastado = montoGastado;
        this.fecha = fecha;
        this.concepto = concepto;
    }
    public int getIdGasto() {
        return idGasto;
    }
    public void setIdGasto(int idGasto) {
        this.idGasto = idGasto;
    }
    public int getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
    public double getMontoGastado() {
        return montoGastado;
    }
    public void setMontoGastado(double montoGastado) {
        this.montoGastado = montoGastado;
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
    public double agregarGasto(int monto, String concepto,LocalDate fecha) {
        double saldoActual = leerSaldoActual();
        saldoActual -= monto;
        return saldoActual;
    }
    public double leerSaldoActual() {
        double saldoActual =50000.0;
        return saldoActual;
    }
    public void actualizarGasto(int idGasto, double nuevoMonto, String nuevoConcepto, LocalDate nuevaFecha) {
        if (this.idGasto == idGasto) {
            this.montoGastado = nuevoMonto;
            this.concepto = nuevoConcepto;
            this.fecha = nuevaFecha;
        }
    }
    public Gasto leeGasto(int idGasto) {
        if (this.idGasto==idGasto) {
            return this;
        } else {
            return null;
        }
    }
    public List<Gasto> listarGastosPorUsuario(int idUsuario) {
        for (Gasto gasto : gastos) {
            if (gasto.getIdUsuario() == idUsuario) {
                gastos.add(gasto);
            }
        }
        return gastos;    
     }
     public void eliminarGasto(int idGasto) {
        if (this.idGasto == idGasto) {
            gastos.remove(this);
        }
     }
     public static void main(String[] args) {
         Gasto gasto1 = new Gasto(1, 1, 200.0, LocalDate.now(), "Alimentación");
         gasto1.agregarGasto(200, "Alimentación", LocalDate.now());
     }

}
