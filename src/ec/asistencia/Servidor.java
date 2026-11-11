package ec.asistencia;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.*;
import java.util.*;

final class Servidor {
    private record Sesion(Academico.Usuario usuario, Instant vence) {}
    private record Intentos(int total, Instant vence) {}
    private final Map<String, Sesion> sesiones = new HashMap<>();
    private final Map<String, Intentos> intentos = new HashMap<>();
    private final Properties configuracion = new Properties();
    private final String claveFicticia = Claves.crear(Claves.token());
    private final Clock reloj;

    Servidor(Path archivo) throws IOException {
        try { CifradoFacial.validarClave(); }
        catch (RuntimeException e) {
            throw new IllegalArgumentException("Configura ASISTENCIA_CLAVE_FACIAL antes de iniciar Gestión.", e);
        }
        try (var lector = Files.newBufferedReader(archivo)) { configuracion.load(lector); }
        for (String campo : List.of("bd.url", "bd.usuario", "bd.clave")) {
            Academico.exigir(!configuracion.getProperty(campo, "").isBlank(), "Completa " + campo + " en la configuración.");
        }
        reloj = Clock.system(ZoneId.of(configuracion.getProperty("servidor.zona", "America/Guayaquil")));
    }

    void arrancar() throws IOException, SQLException {
        try (Base bd = new Base(configuracion)) {
            try {
                bd.uno("SELECT apellidos FROM cuenta LIMIT 0");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1054) {
                    throw new IllegalArgumentException("Importa bd/actualizar-apellidos.sql para habilitar los nombres y apellidos.", e);
                }
                throw e;
            }
            bd.uno("SELECT foto_matricula,paralelo FROM matricula LIMIT 0");
            bd.uno("SELECT paralelo FROM clase LIMIT 0");
            bd.uno("SELECT cupos_a,cupos_b FROM materia LIMIT 0");
            try {
                bd.uno("SELECT nivel FROM materia LIMIT 0");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1054) {
                    throw new IllegalArgumentException("Importa bd/actualizar-jornadas.sql para activar el semestre de las materias.", e);
                }
                throw e;
            }
            try {
                bd.uno("SELECT periodo_id FROM materia LIMIT 0");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1054) {
                    throw new IllegalArgumentException("Importa bd/actualizar-ofertas-periodo.sql para asociar materias a sus períodos.", e);
                }
                throw e;
            }
            try {
                bd.uno("SELECT id FROM semestre_academico LIMIT 0");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1146) {
                    throw new IllegalArgumentException("Importa bd/actualizar-semestres.sql para activar el horario semanal.", e);
                }
                throw e;
            }
            try {
                bd.uno("SELECT estudiante_id,descriptor,consentimiento_en FROM perfil_facial LIMIT 0");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1146 || e.getErrorCode() == 1054) {
                    throw new IllegalArgumentException("Importa bd/actualizar-faceid.sql para preparar las plantillas Face ID.", e);
                }
                throw e;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1054) {
                throw new IllegalArgumentException("Importa bd/actualizar-paralelos.sql para actualizar la base de datos.", e);
            }
            throw e;
        }
        String host = configuracion.getProperty("servidor.host", "127.0.0.1");
        int puerto = Integer.parseInt(configuracion.getProperty("servidor.puerto", Integer.toString(Protocolo.PUERTO_PREDETERMINADO)));
        HttpServer servidor = HttpServer.create(new InetSocketAddress(host, puerto), 32);
        servidor.createContext(Protocolo.RUTA, this::atender);
        // Un solo ejecutor conserva el orden de las validaciones y cambios académicos.
        servidor.start();
        System.out.printf("Servidor listo en %s:%d (%s)%n", host, servidor.getAddress().getPort(), reloj.getZone());
    }

    void crearAdministrador(String usuario, String nombre, String clave) throws SQLException {
        try (Base bd = new Base(configuracion)) {
            bd.cambiar("INSERT INTO cuenta(usuario,nombre,clave,rol) VALUES(?,?,?,'ADMIN')",
                    Academico.usuario(usuario, false), Academico.texto(nombre, 120, "el nombre"), Claves.crear(clave));
        }
    }

    boolean hayAdministrador() throws SQLException {
        try (Base bd = new Base(configuracion)) {
            return bd.uno("SELECT id FROM cuenta WHERE rol='ADMIN' LIMIT 1") != null;
        }
    }

    private List<String[]> ingresar(Base bd, String accion, String[] campos, Instant ahora) throws SQLException {
        Academico.exigir(campos.length == 2, "Ingresa tu usuario y clave.");
        boolean estudiante = accion.equals("ingresarEstudiante");
        String usuario = Academico.usuario(campos[0], estudiante);
        Intentos previos = intentos.get(usuario);
        Academico.exigir(previos == null || previos.total() < 8, "Demasiados intentos. Vuelve a probar en unos minutos.");
        String[] fila = bd.uno("SELECT id,rol,CONCAT_WS(' ',nombre,apellidos),clave FROM cuenta WHERE usuario=?", usuario);
        boolean coincide = Claves.verificar(campos[1], fila == null ? claveFicticia : fila[3]);
        boolean rolPermitido = fila != null && fila[1].equals("ESTUDIANTE") == estudiante
                && (!accion.equals("ingresarDocente") || fila[1].equals("DOCENTE"));
        if (!coincide || !rolPermitido) {
            int total = previos == null ? 1 : previos.total() + 1;
            if (intentos.size() < 10_000 || previos != null) {
                intentos.put(usuario, new Intentos(total, previos == null ? ahora.plusSeconds(300) : previos.vence()));
            }
            throw new IllegalArgumentException("Usuario o clave incorrectos.");
        }
        intentos.remove(usuario);
        Academico.exigir(sesiones.size() < 10_000, "Hay demasiadas sesiones activas. Intenta más tarde.");
        var cuenta = new Academico.Usuario(Long.parseLong(fila[0]), fila[1], fila[2]);
        String token = Claves.token();
        sesiones.put(token, new Sesion(cuenta, ahora.plus(Duration.ofHours(8))));
        return Collections.singletonList(new String[]{token, cuenta.rol(), cuenta.nombre()});
    }

    private List<String[]> ejecutar(String[] peticion) throws SQLException, IOException {
        String accion = peticion[0];
        String token = peticion[1];
        String[] campos = Arrays.copyOfRange(peticion, 2, peticion.length);
        Instant ahora = reloj.instant();
        sesiones.values().removeIf(s -> !s.vence().isAfter(ahora));
        intentos.values().removeIf(i -> !i.vence().isAfter(ahora));
        if (accion.equals("salir")) {
            sesiones.remove(token);
            return List.of();
        }
        try (Base bd = new Base(configuracion)) {
            if (accion.equals("ingresarGestion") || accion.equals("ingresarDocente") || accion.equals("ingresarEstudiante")) {
                return ingresar(bd, accion, campos, ahora);
            }
            Sesion sesion = sesiones.get(token);
            if (!accion.equals("registrar")) {
                Academico.exigir(sesion != null, "Ingresa de nuevo. Tu sesión no está activa.");
            }
            var academico = new Academico(bd, LocalDateTime.ofInstant(ahora, reloj.getZone()));
            bd.iniciar();
            try {
                var respuesta = academico.ejecutar(sesion == null ? null : sesion.usuario(), accion, campos);
                bd.confirmar();
                if (accion.equals("guardarCuenta") && !campos[0].isEmpty() && !campos[5].isEmpty()) {
                    long id = Long.parseLong(campos[0]);
                    sesiones.values().removeIf(s -> s.usuario().id() == id);
                }
                return respuesta;
            } catch (SQLException | IOException | RuntimeException e) {
                bd.revertir();
                throw e;
            }
        }
    }

    private void atender(HttpExchange intercambio) throws IOException {
        try (intercambio) {
            if (!intercambio.getRequestMethod().equals("POST") || !intercambio.getRequestURI().getPath().equals(Protocolo.RUTA)) {
                intercambio.sendResponseHeaders(404, -1);
                return;
            }
            byte[] respuesta;
            int codigo = 200;
            try {
                var filas = Protocolo.leer(intercambio.getRequestBody());
                Academico.exigir(filas.size() == 1 && filas.get(0).length >= 2, "Solicitud inválida.");
                respuesta = Protocolo.codificar(ejecutar(filas.get(0)).toArray(String[][]::new));
            } catch (java.time.DateTimeException | NumberFormatException e) {
                codigo = 400;
                respuesta = Protocolo.codificar(new String[]{"Revisa las fechas, horas y campos numéricos."});
            } catch (IllegalArgumentException | IOException e) {
                codigo = 400;
                respuesta = Protocolo.codificar(new String[]{Objects.toString(e.getMessage(), "Solicitud inválida.")});
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    codigo = 409;
                    respuesta = Protocolo.codificar(new String[]{"Ya existe un registro con esos datos."});
                } else {
                    System.err.println("Error de MariaDB. SQLState=" + e.getSQLState() + " código=" + e.getErrorCode());
                    codigo = 500;
                    respuesta = Protocolo.codificar(new String[]{"La base de datos no pudo completar la operación."});
                }
            } catch (RuntimeException e) {
                System.err.println("Error interno al procesar la solicitud: " + e.getClass().getSimpleName());
                codigo = 500;
                respuesta = Protocolo.codificar(new String[]{"El servidor no pudo completar la operación."});
            }
            intercambio.getResponseHeaders().set("Content-Type", "application/octet-stream");
            intercambio.getResponseHeaders().set("Cache-Control", "no-store");
            intercambio.sendResponseHeaders(codigo, respuesta.length);
            intercambio.getResponseBody().write(respuesta);
        }
    }
}
