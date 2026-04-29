package Vistas;

import Modelos.AnalizadorFinanciero;
import Modelos.Gasto;
import Modelos.Ingreso;
import Modelos.Meta;
import Modelos.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class GraficasView extends BorderPane {

    private final Usuario usuario;

    private static final NumberFormat FMT = NumberFormat.getInstance(new Locale("es", "CO"));
    private static final DateTimeFormatter FMT_MES_IN  = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter FMT_MES_OUT = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("es", "CO"));

    public GraficasView(Usuario usuario) {
        this.usuario = usuario;
        construirVista();
    }

    private void construirVista() {
        VBox contenido = new VBox(28);
        contenido.getStyleClass().add("content-area");
        contenido.setPadding(new Insets(28));

        Label titulo = new Label("📊 Gráficas financieras");
        titulo.getStyleClass().add("content-title");

        List<Ingreso> ingresos = Ingreso.listarIngresosPorUsuario(usuario.getId());
        List<Gasto>   gastos   = Gasto.listarGastosPorUsuario(usuario.getId());
        List<Meta>    metas    = Meta.listarMetasPorUsuario(usuario.getId());

        double totalI = AnalizadorFinanciero.calcularTotalIngresos(ingresos);
        double totalG = AnalizadorFinanciero.calcularTotalGastos(gastos);
        Map<String, Double> porCategoria = AnalizadorFinanciero.agruparGastosPorCategoria(gastos);

        contenido.getChildren().addAll(
            titulo,
            seccionGrafica1(porCategoria, totalG),
            seccionGrafica2(totalI, totalG),
            seccionGrafica3(gastos),
            seccionGrafica4(metas),
            seccionGrafica5(porCategoria, totalG)
        );

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        this.setCenter(scroll);
    }

    // ──────────────────────────────────────────────────────────
    //  1. BARRAS — Gasto total por categoría
    // ──────────────────────────────────────────────────────────
    private VBox seccionGrafica1(Map<String, Double> porCategoria, double totalG) {
        CategoryAxis ejeX = new CategoryAxis();
        ejeX.setLabel("Categoría de gasto");
        ejeX.setTickLabelRotation(-30);
        ejeX.setTickLabelFill(Color.web("#1e293b"));

        NumberAxis ejeY = ejeMoneda("Monto gastado (pesos COP)");

        BarChart<String, Number> grafica = new BarChart<>(ejeX, ejeY);
        grafica.setLegendVisible(false);
        grafica.setPrefHeight(340);
        grafica.setAnimated(false);
        grafica.setBarGap(3);
        grafica.setCategoryGap(22);
        grafica.setHorizontalGridLinesVisible(true);
        grafica.setVerticalGridLinesVisible(false);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        porCategoria.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(e -> serie.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        if (porCategoria.isEmpty()) serie.getData().add(new XYChart.Data<>("Sin datos", 0));
        grafica.getData().add(serie);

        // Tabla resumen
        VBox resumen = resumenBox();
        resumen.getChildren().addAll(
            etiquetaResumen("Detalle por categoría"),
            filaEncabezado("Categoría", "Monto (COP)", "% del total", "Proporción")
        );
        for (Map.Entry<String, Double> e : porCategoria.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()).toList()) {
            resumen.getChildren().add(filaCategoria(
                e.getKey(), e.getValue().longValue(),
                totalG > 0 ? e.getValue() / totalG * 100 : 0));
        }
        if (totalG > 0) {
            String mayor = porCategoria.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
            String menor = porCategoria.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
            resumen.getChildren().add(new Separator());
            resumen.getChildren().addAll(
                fila("Total gastado",            "$" + FMT.format((long) totalG) + " COP"),
                fila("Categoría con mayor gasto", mayor),
                fila("Categoría con menor gasto", menor)
            );
        }

        return tarjeta("1. Gastos por categoría",
            "Eje X: categorías  |  Eje Y: monto en pesos COP  |  Cada barra muestra el total gastado en esa categoría",
            grafica, resumen);
    }

    // ──────────────────────────────────────────────────────────
    //  2. BARRAS — Ingresos vs Gastos
    // ──────────────────────────────────────────────────────────
    private VBox seccionGrafica2(double totalI, double totalG) {
        CategoryAxis ejeX = new CategoryAxis();
        ejeX.setLabel("Tipo de movimiento");
        ejeX.setTickLabelFill(Color.web("#1e293b"));

        NumberAxis ejeY = ejeMoneda("Monto total acumulado (pesos COP)");

        BarChart<String, Number> grafica = new BarChart<>(ejeX, ejeY);
        grafica.setLegendVisible(false);
        grafica.setPrefHeight(300);
        grafica.setAnimated(false);
        grafica.setCategoryGap(80);
        grafica.setHorizontalGridLinesVisible(true);
        grafica.setVerticalGridLinesVisible(false);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.getData().add(new XYChart.Data<>("Ingresos", totalI));
        serie.getData().add(new XYChart.Data<>("Gastos",   totalG));
        grafica.getData().add(serie);

        double saldo = totalI - totalG;
        double pct   = totalI > 0 ? totalG / totalI * 100 : 0;
        String estado = AnalizadorFinanciero.determinarEstadoFinanciero(totalI, totalG);
        String color  = estado.equals("SUPERAVIT") ? "#16a34a" : estado.equals("DEFICIT") ? "#dc2626" : "#d97706";

        VBox resumen = resumenBox();
        Label lblEstado = new Label("Estado: " + estado);
        lblEstado.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + color + ";");
        resumen.getChildren().addAll(lblEstado, new Separator(),
            fila("Total ingresos",  "$" + FMT.format((long) totalI) + " COP"),
            fila("Total gastos",    "$" + FMT.format((long) totalG) + " COP"),
            fila("Saldo neto",      "$" + FMT.format((long) saldo) + " COP  " + (saldo >= 0 ? "✔" : "✘")),
            fila("% del ingreso gastado", String.format("%.1f%%", pct))
        );

        return tarjeta("2. Ingresos vs Gastos",
            "Eje X: tipo de movimiento  |  Eje Y: monto en pesos COP  |  Barra más alta = mayor monto",
            grafica, resumen);
    }

    // ──────────────────────────────────────────────────────────
    //  3. LÍNEA — Evolución mensual de gastos
    // ──────────────────────────────────────────────────────────
    private VBox seccionGrafica3(List<Gasto> gastos) {
        CategoryAxis ejeX = new CategoryAxis();
        ejeX.setLabel("Mes");
        ejeX.setTickLabelRotation(-30);
        ejeX.setTickLabelFill(Color.web("#1e293b"));

        NumberAxis ejeY = ejeMoneda("Gasto mensual (pesos COP)");

        LineChart<String, Number> grafica = new LineChart<>(ejeX, ejeY);
        grafica.setLegendVisible(false);
        grafica.setPrefHeight(320);
        grafica.setAnimated(false);
        grafica.setCreateSymbols(true);
        grafica.setHorizontalGridLinesVisible(true);
        grafica.setVerticalGridLinesVisible(true);

        TreeMap<String, Double> porMes = new TreeMap<>();
        for (Gasto g : gastos) {
            porMes.merge(YearMonth.from(g.getFecha()).format(FMT_MES_IN), g.getMonto(), Double::sum);
        }

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Map.Entry<String, Double> e : porMes.entrySet()) {
            String etiqueta = YearMonth.parse(e.getKey(), FMT_MES_IN).format(FMT_MES_OUT);
            serie.getData().add(new XYChart.Data<>(etiqueta, e.getValue()));
        }
        if (porMes.isEmpty()) serie.getData().add(new XYChart.Data<>("Sin datos", 0));
        grafica.getData().add(serie);

        VBox resumen = resumenBox();
        if (!porMes.isEmpty()) {
            Map.Entry<String, Double> mayor = porMes.entrySet().stream().max(Map.Entry.comparingByValue()).get();
            Map.Entry<String, Double> menor = porMes.entrySet().stream().min(Map.Entry.comparingByValue()).get();
            double promedio = porMes.values().stream().mapToDouble(d -> d).average().orElse(0);

            resumen.getChildren().add(etiquetaResumen("Gasto por mes:"));
            for (Map.Entry<String, Double> e : porMes.entrySet()) {
                String etq = YearMonth.parse(e.getKey(), FMT_MES_IN).format(FMT_MES_OUT);
                String marca = e.getKey().equals(mayor.getKey()) ? "  ▲ mayor"
                            : e.getKey().equals(menor.getKey()) ? "  ▼ menor" : "";
                Label f = new Label("  • " + etq + "   $" + FMT.format(e.getValue().longValue()) + " COP" + marca);
                f.setStyle("-fx-font-size: 13px;");
                resumen.getChildren().add(f);
            }
            resumen.getChildren().add(new Separator());
            resumen.getChildren().addAll(
                fila("Mes con mayor gasto",
                    YearMonth.parse(mayor.getKey(), FMT_MES_IN).format(FMT_MES_OUT) +
                    "  ($" + FMT.format(mayor.getValue().longValue()) + " COP)"),
                fila("Mes con menor gasto",
                    YearMonth.parse(menor.getKey(), FMT_MES_IN).format(FMT_MES_OUT) +
                    "  ($" + FMT.format(menor.getValue().longValue()) + " COP)"),
                fila("Promedio mensual", "$" + FMT.format((long) promedio) + " COP"),
                fila("Periodo analizado",
                    YearMonth.parse(porMes.firstKey(), FMT_MES_IN).format(FMT_MES_OUT) + " — " +
                    YearMonth.parse(porMes.lastKey(),  FMT_MES_IN).format(FMT_MES_OUT) +
                    " (" + porMes.size() + " meses)")
            );
        }

        return tarjeta("3. Evolución mensual de gastos",
            "Eje X: mes  |  Eje Y: gasto total de ese mes en pesos COP  |  La línea sube cuando el gasto aumentó",
            grafica, resumen);
    }

    // ──────────────────────────────────────────────────────────
    //  4. PROGRESO — Avance de metas de ahorro
    // ──────────────────────────────────────────────────────────
    private VBox seccionGrafica4(List<Meta> metas) {
        VBox contenido = new VBox(14);
        if (metas.isEmpty()) {
            Label sin = new Label("No tienes metas registradas. Ve a la sección Metas para crear una.");
            sin.getStyleClass().add("subtitle-label");
            sin.setWrapText(true);
            contenido.getChildren().add(sin);
        } else {
            for (Meta m : metas) contenido.getChildren().add(crearFilaMeta(m));
        }
        return tarjeta("4. Progreso de metas de ahorro",
            "Escala: 0% (sin ahorro) → 100% (meta cumplida)  |  Azul = en curso  |  Amarillo = +80%  |  Verde = cumplida",
            contenido, null);
    }

    private VBox crearFilaMeta(Meta meta) {
        double progreso = meta.calcularProgreso();
        double fraccion = Math.min(progreso / 100.0, 1.0);
        double faltante = Math.max(meta.getMontoObjetivo() - meta.getMontoActual(), 0);

        String colorBarra, textoEstado;
        if (progreso >= 100) {
            colorBarra   = "-fx-accent: #16a34a;";
            textoEstado  = "✔ Meta cumplida";
        } else if (progreso >= 80) {
            colorBarra   = "-fx-accent: #d97706;";
            textoEstado  = String.format("⚡ %.1f%% — ¡Muy cerca!", progreso);
        } else {
            colorBarra   = "-fx-accent: #2563eb;";
            textoEstado  = String.format("%.1f%% completado", progreso);
        }

        Label nombre = new Label(meta.getNombre());
        nombre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ProgressBar barra = new ProgressBar(fraccion);
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.setPrefHeight(22);
        barra.setStyle(colorBarra);

        // Marcas de escala bajo la barra
        Label m0   = new Label("0%");   m0.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        Label m50  = new Label("50%");  m50.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        Label m100 = new Label("100%"); m100.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        Region esp1 = new Region(); HBox.setHgrow(esp1, Priority.ALWAYS);
        Region esp2 = new Region(); HBox.setHgrow(esp2, Priority.ALWAYS);
        HBox escala = new HBox(m0, esp1, m50, esp2, m100);

        Label estado = new Label(textoEstado);
        estado.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label montos = new Label(
            "Ahorrado: $" + FMT.format((long) meta.getMontoActual()) + " COP" +
            "   /   Objetivo: $" + FMT.format((long) meta.getMontoObjetivo()) + " COP" +
            "   |   Falta: $" + FMT.format((long) faltante) + " COP");
        montos.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        montos.setWrapText(true);

        Label fecha = new Label("Fecha límite: " + meta.getFechaEsperada());
        fecha.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        VBox card = new VBox(5, nombre, barra, escala, estado, montos, fecha);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; " +
                      "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 14;");
        return card;
    }

    // ──────────────────────────────────────────────────────────
    //  5. CIRCULAR — Distribución de gastos por categoría
    // ──────────────────────────────────────────────────────────
    private VBox seccionGrafica5(Map<String, Double> porCategoria, double totalG) {
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        porCategoria.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(e -> {
                double pct = totalG > 0 ? e.getValue() / totalG * 100 : 0;
                datos.add(new PieChart.Data(String.format("%s  %.1f%%", e.getKey(), pct), e.getValue()));
            });
        if (porCategoria.isEmpty()) datos.add(new PieChart.Data("Sin datos  0%", 1));

        PieChart grafica = new PieChart(datos);
        grafica.setLegendVisible(true);
        grafica.setPrefHeight(380);
        grafica.setLabelsVisible(true);
        grafica.setAnimated(false);
        grafica.setStartAngle(90);

        VBox resumen = resumenBox();
        resumen.getChildren().addAll(
            etiquetaResumen("Distribución proporcional"),
            filaEncabezado("Categoría", "Monto (COP)", "% del total", "Proporción")
        );

        boolean hayConcentracion = false;
        for (Map.Entry<String, Double> e : porCategoria.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()).toList()) {
            double pct = totalG > 0 ? e.getValue() / totalG * 100 : 0;
            if (pct > 40) hayConcentracion = true;
            resumen.getChildren().add(filaCategoria(e.getKey(), e.getValue().longValue(), pct));
        }
        resumen.getChildren().add(new Separator());
        String catMayor = porCategoria.entrySet().stream()
            .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("-");
        double pctMayor = porCategoria.isEmpty() ? 0 : porCategoria.get(catMayor) / totalG * 100;
        resumen.getChildren().addAll(
            fila("Total analizado",      "$" + FMT.format((long) totalG) + " COP"),
            fila("Categoría dominante",  catMayor + String.format("  (%.1f%%)", pctMayor)),
            fila("Nº de categorías",     String.valueOf(porCategoria.size()))
        );
        resumen.getChildren().add(new Separator());

        Label interp;
        if (hayConcentracion) {
            interp = new Label("⚠ Una categoría supera el 40% del total — concentración alta. " +
                "Considera redistribuir el gasto entre más categorías.");
            interp.setStyle("-fx-text-fill: #b45309; -fx-background-color: #fef3c7; " +
                "-fx-padding: 8; -fx-background-radius: 5; -fx-font-size: 13px;");
        } else {
            interp = new Label("✔ Ninguna categoría supera el 40% — distribución equilibrada.");
            interp.setStyle("-fx-text-fill: #15803d; -fx-background-color: #dcfce7; " +
                "-fx-padding: 8; -fx-background-radius: 5; -fx-font-size: 13px;");
        }
        interp.setWrapText(true);
        resumen.getChildren().add(interp);

        return tarjeta("5. Distribución de gastos (%)",
            "Cada porción = una categoría  |  Tamaño de la porción = % del total  |  El círculo completo = 100%",
            grafica, resumen);
    }

    // ──────────────────────────────────────────────────────────
    //  HELPERS
    // ──────────────────────────────────────────────────────────

    /** Eje Y con formato legible: $1.5M, $500K, $0 */
    private NumberAxis ejeMoneda(String titulo) {
        NumberAxis eje = new NumberAxis();
        eje.setLabel(titulo);
        eje.setForceZeroInRange(true);
        eje.setAutoRanging(true);
        eje.setTickLabelFill(Color.web("#1e293b"));
        eje.setTickLabelFormatter(new StringConverter<>() {
            @Override public String toString(Number n) {
                long v = n.longValue();
                if (v >= 1_000_000) return "$" + String.format("%.1fM", v / 1_000_000.0);
                if (v >= 1_000)     return "$" + (v / 1_000) + "K";
                return v == 0 ? "$0" : "$" + v;
            }
            @Override public Number fromString(String s) { return 0; }
        });
        return eje;
    }



    private VBox tarjeta(String titulo, String subtitulo,
                         javafx.scene.Node grafica, javafx.scene.Node resumen) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label lblSub = new Label(subtitulo);
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        lblSub.setWrapText(true);

        VBox card = new VBox(10, lblTitulo, new Separator(), lblSub, grafica);
        if (resumen != null) {
            Label lblR = new Label("📋 Resumen de datos");
            lblR.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #334155;");
            card.getChildren().addAll(lblR, resumen);
        }
        card.getStyleClass().add("content-card");
        card.setPadding(new Insets(18));
        return card;
    }

    private VBox resumenBox() {
        VBox v = new VBox(6);
        v.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; " +
                   "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 14;");
        return v;
    }

    private HBox filaEncabezado(String c1, String c2, String c3, String c4) {
        HBox h = new HBox(0,
            colLabel(c1, 150, true), colLabel(c2, 140, true),
            colLabel(c3, 100, true), colLabel(c4, 180, true));
        h.setStyle("-fx-border-color: transparent transparent #cbd5e1 transparent; -fx-padding: 2 0 6 0;");
        return h;
    }

    private HBox filaCategoria(String nombre, long monto, double pct) {
        double anchoBarra = Math.min(pct / 100.0 * 160, 160);
        Region barra = new Region();
        barra.setPrefWidth(anchoBarra);
        barra.setPrefHeight(14);
        barra.setStyle("-fx-background-color: " +
            (pct > 40 ? "#ef4444" : pct > 25 ? "#f59e0b" : "#3b82f6") +
            "; -fx-background-radius: 3;");

        Label lPct = new Label(String.format("%.1f%%", pct));
        lPct.setMinWidth(50);
        if (pct > 40) lPct.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-font-size: 13px;");
        else          lPct.setStyle("-fx-font-size: 13px;");

        Label lAlerta = new Label(pct > 40 ? " ⚠" : "");
        lAlerta.setStyle("-fx-text-fill: #dc2626;");

        HBox barraConAlerta = new HBox(4, barra, lAlerta);
        barraConAlerta.setAlignment(Pos.CENTER_LEFT);
        barraConAlerta.setMinWidth(180);

        HBox h = new HBox(0,
            colLabel(nombre,              150, false),
            colLabel("$" + FMT.format(monto), 140, false),
            lPct, barraConAlerta);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(3, 0, 3, 0));
        return h;
    }

    private Label colLabel(String texto, double ancho, boolean esHeader) {
        Label l = new Label(texto);
        l.setMinWidth(ancho);
        l.setPrefWidth(ancho);
        l.setStyle(esHeader
            ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #475569;"
            : "-fx-font-size: 13px; -fx-text-fill: #1e293b;");
        return l;
    }

    private HBox fila(String clave, String valor) {
        Label k = new Label(clave + ": ");
        k.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        Label v = new Label(valor);
        v.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        v.setWrapText(true);
        HBox h = new HBox(4, k, v);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private Label etiquetaResumen(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        return l;
    }
}
