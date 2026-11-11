package ec.asistencia;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

final class Protocolo {
    static final int LIMITE = 2_500_000;
    static final String RUTA = "/asistencia";
    static final int PUERTO_PREDETERMINADO = 18190;

    static String direccionPredeterminada() {
        String direccion = System.getProperty("asistencia.servidor", "").strip();
        return direccion.isEmpty() ? direccionLocal() : direccion;
    }

    static String direccionLocal() {
        int puerto = PUERTO_PREDETERMINADO;
        Path archivo = Path.of("servidor.properties");
        if (Files.isRegularFile(archivo)) {
            try (var entrada = Files.newBufferedReader(archivo)) {
                var configuracion = new Properties();
                configuracion.load(entrada);
                int valor = Integer.parseInt(configuracion.getProperty("servidor.puerto", "").strip());
                if (valor > 0 && valor <= 65535) puerto = valor;
            } catch (IOException | IllegalArgumentException ignorado) {
                // Los clientes pueden ejecutarse en un equipo sin configuración del servidor.
            }
        }
        return "http://127.0.0.1:" + puerto;
    }

    static byte[] codificar(String[]... filas) throws IOException {
        var bytes = new ByteArrayOutputStream();
        try (var salida = new DataOutputStream(bytes)) {
            salida.writeInt(filas.length);
            for (String[] fila : filas) {
                salida.writeInt(fila.length);
                for (String campo : fila) {
                    byte[] valor = campo.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    salida.writeInt(valor.length);
                    salida.write(valor);
                }
            }
        }
        if (bytes.size() > LIMITE) throw new IOException("La respuesta supera el límite de tamaño.");
        return bytes.toByteArray();
    }

    static List<String[]> leer(InputStream entrada) throws IOException {
        byte[] bytes = entrada.readNBytes(LIMITE + 1);
        if (bytes.length > LIMITE) throw new IOException("Mensaje demasiado grande.");
        try (var datos = new DataInputStream(new ByteArrayInputStream(bytes))) {
            int total = datos.readInt();
            if (total < 0 || total > 50_000) throw new IOException("Número de filas inválido.");
            List<String[]> filas = new ArrayList<>();
            for (int i = 0; i < total; i++) {
                int columnas = datos.readInt();
                if (columnas < 0 || columnas > 32) throw new IOException("Número de campos inválido.");
                String[] fila = new String[columnas];
                for (int j = 0; j < columnas; j++) {
                    int longitud = datos.readInt();
                    if (longitud < 0 || longitud > datos.available()) throw new IOException("Campo inválido.");
                    fila[j] = new String(datos.readNBytes(longitud), java.nio.charset.StandardCharsets.UTF_8);
                }
                filas.add(fila);
            }
            if (datos.available() != 0) throw new IOException("Mensaje inválido.");
            return filas;
        }
    }

    static final class Cliente {
        private final URI uri;
        private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        private String sesion = "";

        Cliente(String url) {
            URI base = URI.create(url.strip());
            String host = base.getHost();
            if (host == null || base.getUserInfo() != null || base.getRawQuery() != null
                    || base.getRawFragment() != null) throw new IllegalArgumentException("Dirección inválida.");
            boolean local = Set.of("localhost", "127.0.0.1", "[::1]").contains(host.toLowerCase(Locale.ROOT));
            if (!"https".equalsIgnoreCase(base.getScheme()) && !(local && "http".equalsIgnoreCase(base.getScheme()))) {
                throw new IllegalArgumentException("Usa HTTPS para conectarte a otra computadora.");
            }
            uri = URI.create(base.toString().replaceFirst("/+$", "") + RUTA);
        }

        String[] ingresar(String tipo, String usuario, String clave) throws Exception {
            String[] respuesta = pedir(tipo, usuario, clave).get(0);
            sesion = respuesta[0];
            return respuesta;
        }

        List<String[]> pedir(String accion, String... campos) throws Exception {
            String[] fila = new String[campos.length + 2];
            fila[0] = accion;
            fila[1] = sesion;
            System.arraycopy(campos, 0, fila, 2, campos.length);
            var solicitud = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(codificar(fila)))
                    .header("Content-Type", "application/octet-stream").build();
            HttpResponse<InputStream> respuesta = http.send(solicitud, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream cuerpo = respuesta.body()) {
                if (!respuesta.headers().firstValue("Content-Type").orElse("").equals("application/octet-stream")) {
                    throw new IOException("El servicio en " + uri.getAuthority()
                            + " no es el de Asistencia (HTTP " + respuesta.statusCode() + "). Revisa la dirección.");
                }
                List<String[]> filas = leer(cuerpo);
                if (respuesta.statusCode() != 200) throw new IllegalArgumentException(filas.get(0)[0]);
                return filas;
            }
        }
    }
}
