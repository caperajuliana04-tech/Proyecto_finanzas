package Servidor;

import Modelos.*;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.*;
import java.util.Locale;

public class WebServer {

    private static final Map<String, Integer> sesiones = Collections.synchronizedMap(new HashMap<>());

    public static void iniciar() {
        Thread hilo = new Thread(() -> {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(7070), 0);
                server.createContext("/api/login",       WebServer::login);
                server.createContext("/api/inicio",      WebServer::inicio);
                server.createContext("/api/ingresos",    WebServer::ingresos);
                server.createContext("/api/gastos",      WebServer::gastos);
                server.createContext("/api/metas",       WebServer::metas);
                server.createContext("/api/alertas",     WebServer::alertas);
                server.createContext("/api/recompensas", WebServer::recompensas);
                server.createContext("/",                WebServer::servirArchivo);
                server.setExecutor(Executors.newCachedThreadPool());
                server.start();
                System.out.println("Servidor web en http://localhost:7070");
            } catch (IOException e) {
                System.out.println("Error iniciando servidor web: " + e.getMessage());
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    // ── Archivos estáticos ──────────────────────────────────────────────────────

    private static void servirArchivo(HttpExchange ex) throws IOException {
        String ruta = ex.getRequestURI().getPath();
        if (ruta.equals("/")) ruta = "/index.html";
        InputStream is = WebServer.class.getResourceAsStream("/web" + ruta);
        if (is == null) { responder(ex, 404, "No encontrado", "text/plain"); return; }
        byte[] bytes = is.readAllBytes();
        String tipo = ruta.endsWith(".js")  ? "application/javascript; charset=utf-8"
                    : ruta.endsWith(".css") ? "text/css; charset=utf-8"
                    : "text/html; charset=utf-8";
        ex.getResponseHeaders().set("Content-Type", tipo);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    // ── Login ───────────────────────────────────────────────────────────────────

    private static void login(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) { json(ex, 405, "{\"error\":\"Método no soportado\"}"); return; }
        String body = leerCuerpo(ex);
        Usuario u = Usuario.buscarPorCorreo(campo(body, "correo"));
        if (u == null || !u.getContrasena().equals(campo(body, "contrasena"))) {
            json(ex, 401, "{\"error\":\"Credenciales incorrectas\"}");
            return;
        }
        String token = UUID.randomUUID().toString();
        sesiones.put(token, u.getId());
        json(ex, 200, String.format("{\"token\":\"%s\",\"nombre\":\"%s\"}", token, u.getNombre()));
    }

    // ── Dashboard ───────────────────────────────────────────────────────────────

    private static void inicio(HttpExchange ex) throws IOException {
        Integer uid = auth(ex); if (uid == null) return;
        Usuario u           = Usuario.leerPerfil(uid);
        List<Ingreso> ingresos = Ingreso.listarIngresosPorUsuario(uid);
        List<Gasto>   gastos   = Gasto.listarGastosPorUsuario(uid);
        double ti = AnalizadorFinanciero.calcularTotalIngresos(ingresos);
        double tg = AnalizadorFinanciero.calcularTotalGastos(gastos);
        String estado = AnalizadorFinanciero.determinarEstadoFinanciero(ti, tg);
        double pct    = AnalizadorFinanciero.calcularPorcentajeGasto(tg, ti);
        Map<String, Double> cats = AnalizadorFinanciero.agruparGastosPorCategoria(gastos);

        StringBuilder catJson = new StringBuilder("{");
        boolean primero = true;
        for (Map.Entry<String, Double> e : cats.entrySet()) {
            if (!primero) catJson.append(",");
            catJson.append('"').append(esc(e.getKey())).append("\":").append(String.format(Locale.US, "%.2f", e.getValue()));
            primero = false;
        }
        catJson.append("}");

        json(ex, 200, String.format(Locale.US,
            "{\"nombre\":\"%s\",\"saldo\":%.2f,\"totalIngresos\":%.2f,\"totalGastos\":%.2f," +
            "\"estado\":\"%s\",\"porcentajeGasto\":%.1f,\"gastosPorCategoria\":%s}",
            esc(u.getNombre()), u.getSaldoActual(), ti, tg, estado, pct, catJson));
    }

    // ── Ingresos ─────────────────────────────────────────────────────────────────

    private static void ingresos(HttpExchange ex) throws IOException {
        Integer uid = auth(ex); if (uid == null) return;
        String metodo = ex.getRequestMethod();
        String ruta   = ex.getRequestURI().getPath();

        if ("GET".equals(metodo)) {
            List<Ingreso> lista = Ingreso.listarIngresosPorUsuario(uid);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Ingreso ing = lista.get(i);
                if (i > 0) sb.append(",");
                sb.append(String.format(Locale.US, "{\"id\":%d,\"monto\":%.2f,\"fecha\":\"%s\",\"concepto\":\"%s\"}",
                    ing.getIdIngreso(), ing.getMonto(), ing.getFecha(), esc(ing.getConcepto())));
            }
            json(ex, 200, sb.append("]").toString());

        } else if ("POST".equals(metodo)) {
            String body  = leerCuerpo(ex);
            String fecha = campo(body, "fecha");
            if (fecha.isBlank()) fecha = LocalDate.now().toString();
            new Ingreso(0, uid, campoDouble(body, "monto"), LocalDate.parse(fecha), campo(body, "concepto"))
                .registrarIngreso();
            json(ex, 201, "{\"ok\":true}");

        } else if ("DELETE".equals(metodo) && ruta.matches("/api/ingresos/\\d+")) {
            Ingreso.eliminarIngreso(Integer.parseInt(ruta.substring("/api/ingresos/".length())));
            json(ex, 200, "{\"ok\":true}");

        } else {
            json(ex, 405, "{\"error\":\"Método no soportado\"}");
        }
    }

    // ── Gastos ───────────────────────────────────────────────────────────────────

    private static void gastos(HttpExchange ex) throws IOException {
        Integer uid = auth(ex); if (uid == null) return;
        String metodo = ex.getRequestMethod();
        String ruta   = ex.getRequestURI().getPath();

        if ("GET".equals(metodo)) {
            List<Gasto> lista = Gasto.listarGastosPorUsuario(uid);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Gasto g = lista.get(i);
                if (i > 0) sb.append(",");
                sb.append(String.format(Locale.US, "{\"id\":%d,\"nombre\":\"%s\",\"monto\":%.2f,\"fecha\":\"%s\",\"categoria\":\"%s\"}",
                    g.getIdGasto(), esc(g.getNombre()), g.getMonto(), g.getFecha(), esc(g.getCategoria())));
            }
            json(ex, 200, sb.append("]").toString());

        } else if ("POST".equals(metodo)) {
            String body  = leerCuerpo(ex);
            String fecha = campo(body, "fecha");
            if (fecha.isBlank()) fecha = LocalDate.now().toString();
            new Gasto(0, uid, campoDouble(body, "monto"), LocalDate.parse(fecha),
                campo(body, "nombre"), campo(body, "categoria")).agregarGasto();
            json(ex, 201, "{\"ok\":true}");

        } else if ("DELETE".equals(metodo) && ruta.matches("/api/gastos/\\d+")) {
            Gasto.eliminarGasto(Integer.parseInt(ruta.substring("/api/gastos/".length())));
            json(ex, 200, "{\"ok\":true}");

        } else {
            json(ex, 405, "{\"error\":\"Método no soportado\"}");
        }
    }

    // ── Metas ────────────────────────────────────────────────────────────────────

    private static void metas(HttpExchange ex) throws IOException {
        Integer uid = auth(ex); if (uid == null) return;
        String metodo = ex.getRequestMethod();
        String ruta   = ex.getRequestURI().getPath();

        if ("GET".equals(metodo)) {
            List<Meta> lista = Meta.listarMetasPorUsuario(uid);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Meta m = lista.get(i);
                if (i > 0) sb.append(",");
                sb.append(String.format(Locale.US,
                    "{\"id\":%d,\"nombre\":\"%s\",\"montoObjetivo\":%.2f,\"montoActual\":%.2f," +
                    "\"progreso\":%.1f,\"fechaEsperada\":\"%s\"}",
                    m.getIdMeta(), esc(m.getNombre()), m.getMontoObjetivo(),
                    m.getMontoActual(), m.calcularProgreso(), m.getFechaEsperada()));
            }
            json(ex, 200, sb.append("]").toString());

        } else if ("POST".equals(metodo)) {
            String body     = leerCuerpo(ex);
            String fechaEsp = campo(body, "fechaEsperada");
            if (fechaEsp.isBlank()) fechaEsp = LocalDate.now().plusMonths(3).toString();
            new Meta(0, uid, campoDouble(body, "montoObjetivo"), 0,
                LocalDate.now(), LocalDate.parse(fechaEsp), campo(body, "nombre")).agregarMeta();
            json(ex, 201, "{\"ok\":true}");

        } else if ("DELETE".equals(metodo) && ruta.matches("/api/metas/\\d+")) {
            Meta.eliminarMeta(Integer.parseInt(ruta.substring("/api/metas/".length())));
            json(ex, 200, "{\"ok\":true}");

        } else {
            json(ex, 405, "{\"error\":\"Método no soportado\"}");
        }
    }

    // ── Alertas ──────────────────────────────────────────────────────────────────

    private static void alertas(HttpExchange ex) throws IOException {
        Integer uid = auth(ex); if (uid == null) return;
        String metodo = ex.getRequestMethod();
        String ruta   = ex.getRequestURI().getPath();

        if ("GET".equals(metodo)) {
            List<Alerta> lista = Alerta.listarAlertasPorUsuario(uid);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Alerta a = lista.get(i);
                if (i > 0) sb.append(",");
                sb.append(String.format(
                    "{\"id\":%d,\"tipo\":\"%s\",\"mensaje\":\"%s\",\"fecha\":\"%s\",\"leida\":%b}",
                    a.getIdAlerta(), esc(a.getTipo()), esc(a.getMensaje()), a.getFecha(), a.isLeida()));
            }
            json(ex, 200, sb.append("]").toString());

        } else if ("PATCH".equals(metodo) && ruta.matches("/api/alertas/\\d+/leer")) {
            int id = Integer.parseInt(ruta.split("/")[3]);
            Alerta.listarAlertasPorUsuario(uid).stream()
                .filter(a -> a.getIdAlerta() == id)
                .findFirst()
                .ifPresent(Alerta::marcarComoLeida);
            json(ex, 200, "{\"ok\":true}");

        } else {
            json(ex, 405, "{\"error\":\"Método no soportado\"}");
        }
    }

    // ── Recompensas ───────────────────────────────────────────────────────────────

    private static void recompensas(HttpExchange ex) throws IOException {
        Integer uid = auth(ex); if (uid == null) return;
        List<Recompensa> lista = Recompensa.listarRecompensasPorUsuario(uid);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            Recompensa r = lista.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format(
                "{\"id\":%d,\"tipo\":\"%s\",\"mensaje\":\"%s\",\"fecha\":\"%s\",\"desbloqueada\":%b}",
                r.getIdRecompensa(), esc(r.getTipo()), esc(r.getMensaje()),
                r.getFechaDesbloqueo(), r.isDesbloqueada()));
        }
        json(ex, 200, sb.append("]").toString());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private static Integer auth(HttpExchange ex) throws IOException {
        String header = ex.getRequestHeaders().getFirst("Authorization");
        String token  = (header != null && header.startsWith("Bearer ")) ? header.substring(7).trim() : null;
        Integer uid   = (token != null) ? sesiones.get(token) : null;
        if (uid == null) json(ex, 401, "{\"error\":\"No autorizado\"}");
        return uid;
    }

    private static String leerCuerpo(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String campo(String json, String nombre) {
        Matcher m = Pattern.compile("\"" + nombre + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static double campoDouble(String json, String nombre) {
        Matcher m = Pattern.compile("\"" + nombre + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)").matcher(json);
        return m.find() ? Double.parseDouble(m.group(1)) : 0;
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }

    private static void json(HttpExchange ex, int code, String body) throws IOException {
        responder(ex, code, body, "application/json; charset=utf-8");
    }

    private static void responder(HttpExchange ex, int code, String body, String tipo) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", tipo);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}
