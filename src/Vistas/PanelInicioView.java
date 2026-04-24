package Vistas;

import Modelos.Alerta;
import Modelos.Gasto;
import Modelos.Ingreso;
import Modelos.Meta;
import Modelos.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

// Panel de inicio (home) que muestra un resumen de la situación financiera del usuario.
// Incluye tarjetas de estadísticas y las últimas alertas.
public class PanelInicioView extends BorderPane {

    private final Usuario usuario; // Usuario autenticado cuya información se muestra

    public PanelInicioView(Usuario usuario) {
        this.usuario = usuario;
        construirVista();
    }

    // Arma el layout completo del panel de inicio
    private void construirVista() {
        VBox contenido = new VBox(20);
        contenido.getStyleClass().add("content-area");
        contenido.setPadding(new Insets(28));

        Label titulo = new Label("Panel principal");
        titulo.getStyleClass().add("content-title");

        HBox tarjetas = crearTarjetas(); // Fila de tarjetas de resumen

        Label labelAlertas = new Label("Alertas recientes");
        labelAlertas.getStyleClass().add("section-label");

        VBox listaAlertas = crearListaAlertas(); // Lista de las últimas alertas

        contenido.getChildren().addAll(titulo, tarjetas, labelAlertas, listaAlertas);

        // ScrollPane permite desplazarse si el contenido excede la altura de la ventana
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        this.setCenter(scroll);
    }

    // Crea las 4 tarjetas de resumen: saldo, ingresos, gastos y metas activas.
    // Consulta la base de datos para obtener los valores actualizados.
    private HBox crearTarjetas() {
        // Recarga el saldo directamente desde la DB para mostrar el valor más actualizado
        Usuario usuarioActual = Usuario.leerPerfil(usuario.getId());
        double saldo = usuarioActual != null ? usuarioActual.getSaldoActual() : usuario.getSaldoActual();

        // Suma todos los montos de ingresos y gastos del usuario
        List<Ingreso> ingresos = Ingreso.listarIngresosPorUsuario(usuario.getId());
        double totalIngresos = ingresos.stream().mapToDouble(Ingreso::getMonto).sum();

        List<Gasto> gastos = Gasto.listarGastosPorUsuario(usuario.getId());
        double totalGastos = gastos.stream().mapToDouble(Gasto::getMonto).sum();

        List<Meta> metas = Meta.listarMetasPorUsuario(usuario.getId());

        // Crea cada tarjeta con icono, valor y descripción
        VBox cardSaldo    = crearTarjeta("💰", String.format("$%.2f", saldo), "Saldo actual");
        VBox cardIngresos = crearTarjeta("📈", String.format("$%.2f", totalIngresos), "Total ingresos");
        VBox cardGastos   = crearTarjeta("📉", String.format("$%.2f", totalGastos), "Total gastos");
        VBox cardMetas    = crearTarjeta("🎯", String.valueOf(metas.size()), "Metas activas");

        HBox tarjetas = new HBox(16, cardSaldo, cardIngresos, cardGastos, cardMetas);
        // Cada tarjeta crece proporcionalmente para ocupar el ancho disponible
        HBox.setHgrow(cardSaldo, Priority.ALWAYS);
        HBox.setHgrow(cardIngresos, Priority.ALWAYS);
        HBox.setHgrow(cardGastos, Priority.ALWAYS);
        HBox.setHgrow(cardMetas, Priority.ALWAYS);

        return tarjetas;
    }

    // Construye una tarjeta individual con icono, valor numérico y etiqueta descriptiva
    private VBox crearTarjeta(String icono, String valor, String descripcion) {
        Label lblIcono = new Label(icono);
        lblIcono.getStyleClass().add("stat-icon");

        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("stat-value");

        Label lblDesc = new Label(descripcion);
        lblDesc.getStyleClass().add("stat-label");

        VBox tarjeta = new VBox(4, lblIcono, lblValor, lblDesc);
        tarjeta.getStyleClass().add("stat-card");
        tarjeta.setAlignment(Pos.CENTER_LEFT);

        return tarjeta;
    }

    // Construye la lista de alertas recientes (máximo 5).
    // Las alertas leídas tienen un estilo visual diferente a las pendientes.
    private VBox crearListaAlertas() {
        VBox lista = new VBox(8);

        List<Alerta> alertas = Alerta.listarAlertasPorUsuario(usuario.getId());

        if (alertas.isEmpty()) {
            Label sinAlertas = new Label("No tienes alertas.");
            sinAlertas.getStyleClass().add("subtitle-label");
            lista.getChildren().add(sinAlertas);
            return lista;
        }

        // Muestra máximo 5 alertas para no saturar el panel de inicio
        int mostrar = Math.min(alertas.size(), 5);
        for (int i = 0; i < mostrar; i++) {
            Alerta alerta = alertas.get(i);
            VBox item = new VBox(3);
            // Estilo diferente según si la alerta fue leída o no
            item.getStyleClass().add(alerta.isLeida() ? "alerta-item-leida" : "alerta-item");

            Label tipo = new Label(alerta.getTipo() + " — " + alerta.getFecha());
            tipo.getStyleClass().add("field-label");

            Label mensaje = new Label(alerta.getMensaje());
            mensaje.getStyleClass().add("subtitle-label");
            mensaje.setWrapText(true);

            item.getChildren().addAll(tipo, mensaje);
            lista.getChildren().add(item);
        }

        return lista;
    }
}
