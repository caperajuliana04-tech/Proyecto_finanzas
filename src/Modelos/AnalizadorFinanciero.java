package Modelos;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Clase de análisis financiero: calcula totales, detecta condiciones de riesgo
// y genera alertas o recompensas automáticas según el estado financiero del usuario.
// Todos los métodos son estáticos (sin estado propio), igual que las clases modelo.
public class AnalizadorFinanciero {

    // Suma todos los montos de la lista de ingresos
    public static double calcularTotalIngresos(List<Ingreso> ingresos) {
        double total = 0;
        for (Ingreso ingreso : ingresos) {
            total += ingreso.getMonto();
        }
        return total;
    }

    // Suma todos los montos de la lista de gastos
    public static double calcularTotalGastos(List<Gasto> gastos) {
        double total = 0;
        for (Gasto gasto : gastos) {
            total += gasto.getMonto();
        }
        return total;
    }

    // Calcula el saldo disponible: ingresos menos gastos
    public static double calcularSaldo(double totalIngresos, double totalGastos) {
        return totalIngresos - totalGastos;
    }

    // Calcula qué porcentaje del ingreso total se ha gastado.
    // Retorna 0 si no hay ingresos para evitar división por cero.
    public static double calcularPorcentajeGasto(double totalGastos, double totalIngresos) {
        return totalIngresos == 0 ? 0 : (totalGastos / totalIngresos) * 100;
    }

    // Agrupa los gastos sumando el monto de cada categoría en un mapa.
    // Ejemplo: { "Alimentación": 300000, "Transporte": 80000 }
    public static Map<String, Double> agruparGastosPorCategoria(List<Gasto> gastos) {
        Map<String, Double> porCategoria = new HashMap<>();
        for (Gasto gasto : gastos) {
            String categoria = gasto.getCategoria();
            double acumulado = porCategoria.getOrDefault(categoria, 0.0);
            porCategoria.put(categoria, acumulado + gasto.getMonto());
        }
        return porCategoria;
    }

    // Determina el estado financiero del usuario comparando ingresos y gastos.
    // Retorna "SUPERAVIT", "EQUILIBRIO" o "DEFICIT"
    public static String determinarEstadoFinanciero(double totalIngresos, double totalGastos) {
        if (totalGastos < totalIngresos) return "SUPERAVIT";
        if (totalGastos == totalIngresos) return "EQUILIBRIO";
        return "DEFICIT";
    }

    // Genera una alerta de DEFICIT si los gastos superan los ingresos.
    // Retorna null si la condición no se cumple, para que quien llama pueda ignorarla.
    public static Alerta generarAlertaDeficit(double totalIngresos, double totalGastos, int idUsuario) {
        if (totalGastos > totalIngresos) {
            String mensaje = String.format(
                "Tus gastos ($%.0f) superan tus ingresos ($%.0f). Estás en déficit.",
                totalGastos, totalIngresos);
            return new Alerta(0, "Déficit", mensaje, LocalDate.now(), false, idUsuario);
        }
        return null;
    }

    // Genera una alerta de PRECAUCION si los gastos están entre el 80% y el 100% del ingreso.
    // No duplica con la alerta de déficit (solo aplica si aún no hay déficit).
    public static Alerta generarAlertaPrecaucion(double totalIngresos, double totalGastos, int idUsuario) {
        if (totalIngresos == 0) return null;
        double porcentaje = (totalGastos / totalIngresos) * 100;
        if (porcentaje >= 80 && porcentaje < 100) {
            String mensaje = String.format(
                "Has gastado el %.0f%% de tus ingresos este mes. Cuida tu presupuesto.",
                porcentaje);
            return new Alerta(0, "Precaución", mensaje, LocalDate.now(), false, idUsuario);
        }
        return null;
    }

    // Genera una alerta de CONCENTRACION si alguna categoría supera el 40% del total de gastos.
    // Retorna la alerta para la categoría con mayor porcentaje que exceda el umbral.
    public static Alerta generarAlertaConcentracion(Map<String, Double> porCategoria,
                                                     double totalGastos, int idUsuario) {
        if (totalGastos == 0) return null;
        String categoriaExcede = null;
        double porcentajeMaximo = 0;
        for (Map.Entry<String, Double> entrada : porCategoria.entrySet()) {
            double porcentaje = (entrada.getValue() / totalGastos) * 100;
            if (porcentaje > 40 && porcentaje > porcentajeMaximo) {
                categoriaExcede = entrada.getKey();
                porcentajeMaximo = porcentaje;
            }
        }
        if (categoriaExcede != null) {
            String mensaje = String.format(
                "La categoría '%s' representa el %.0f%% de tus gastos totales.",
                categoriaExcede, porcentajeMaximo);
            return new Alerta(0, "Concentración", mensaje, LocalDate.now(), false, idUsuario);
        }
        return null;
    }

    // Genera una recompensa de SUPERAVIT si los ingresos superan los gastos.
    // Retorna null si no hay superávit.
    public static Recompensa evaluarRecompensaSuperavit(double totalIngresos, double totalGastos,
                                                         int idUsuario) {
        if (totalIngresos > 0 && totalGastos < totalIngresos) {
            double ahorro = totalIngresos - totalGastos;
            String mensaje = String.format(
                "¡Excelente! Tienes un superávit de $%.0f este mes. ¡Sigue ahorrando!", ahorro);
            return new Recompensa(0, "Superávit", mensaje, LocalDate.now(), true, idUsuario);
        }
        return null;
    }

    // Genera una recompensa si alguna meta supera el 80% pero no ha llegado al 100%.
    // Retorna la recompensa para la primera meta que cumpla la condición.
    public static Recompensa evaluarRecompensaMetaAvanzada(List<Meta> metas, int idUsuario) {
        for (Meta meta : metas) {
            double progreso = meta.calcularProgreso();
            if (progreso >= 80 && progreso < 100) {
                String mensaje = String.format(
                    "¡Vas muy bien con tu meta '%s'! Llevas el %.0f%%. ¡Ya casi lo logras!",
                    meta.getNombre(), progreso);
                return new Recompensa(0, "Meta avanzada", mensaje, LocalDate.now(), true, idUsuario);
            }
        }
        return null;
    }

    // Genera una recompensa si alguna meta alcanzó el 100% de su objetivo.
    // Retorna la recompensa para la primera meta completada.
    public static Recompensa evaluarRecompensaMetaCumplida(List<Meta> metas, int idUsuario) {
        for (Meta meta : metas) {
            if (meta.calcularProgreso() >= 100) {
                String mensaje = String.format(
                    "¡Felicitaciones! Completaste tu meta '%s'. ¡Excelente trabajo!",
                    meta.getNombre());
                return new Recompensa(0, "Meta cumplida", mensaje, LocalDate.now(), true, idUsuario);
            }
        }
        return null;
    }

    // Genera una alerta motivacional si alguna meta supera el 80% de progreso.
    // Es un mensaje positivo que anima al usuario a terminar la meta.
    public static Alerta generarAlertaMetaProgreso(List<Meta> metas, int idUsuario) {
        for (Meta meta : metas) {
            double progreso = meta.calcularProgreso();
            if (progreso >= 80 && progreso < 100) {
                String mensaje = String.format(
                    "Tu meta '%s' está al %.0f%%. ¡Estás muy cerca de cumplirla!",
                    meta.getNombre(), progreso);
                return new Alerta(0, "Meta en progreso", mensaje, LocalDate.now(), false, idUsuario);
            }
        }
        return null;
    }
}
