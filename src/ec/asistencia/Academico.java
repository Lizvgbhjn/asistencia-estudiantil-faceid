package ec.asistencia;

import java.io.*;
import java.sql.SQLException;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javax.imageio.ImageIO;

final class Academico {
    record Usuario(long id, String rol, String nombre) {
        boolean admin() { return rol.equals("ADMIN"); }
        boolean estudiante() { return rol.equals("ESTUDIANTE"); }
    }

    static final DateTimeFormatter HORARIO = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    static final List<String> PARCIALES_HABITUALES = List.of("Primer parcial", "Segundo parcial");
    static final int TOLERANCIA_PREDETERMINADA = 5;
    static final int ESTADO_DETALLE = 7;
    static final int MOTIVO_DETALLE = 9;
    private static final String VIGENCIA = """
            e.estado IN ('APROBADA','RETIRADA')
            AND c.paralelo=COALESCE(e.paralelo,'A')
            AND e.desde<=DATE(c.inicio) AND (e.hasta IS NULL OR e.hasta>DATE(c.inicio))
            """;
    private static final String DETALLE = """
            SELECT c.id, s.id, s.usuario, CONCAT_WS(' ',s.nombre,s.apellidos), c.tema, DATE_FORMAT(c.inicio,'%Y-%m-%d %H:%i'),
                DATE_FORMAT(c.fin,'%Y-%m-%d %H:%i'),
                CASE WHEN a.estado='ASISTENCIA' THEN 'A tiempo'
                     WHEN a.estado='ATRASO' THEN 'Atraso'
                     WHEN c.fin<=? THEN IF(j.motivo IS NULL, 'Falta sin justificar', 'Falta justificada')
                     WHEN c.inicio<=? THEN 'En curso sin marca'
                     ELSE 'Programada' END,
                DATE_FORMAT(a.fecha,'%Y-%m-%d %H:%i:%s'), j.motivo,c.paralelo
            FROM clase c JOIN parcial p ON p.id=c.parcial_id
            JOIN matricula e ON e.materia_id=p.materia_id
            JOIN cuenta s ON s.id=e.estudiante_id
            LEFT JOIN asistencia_registro a ON a.clase_id=c.id AND a.estudiante_id=s.id
            LEFT JOIN justificacion j ON j.clase_id=c.id AND j.estudiante_id=s.id
            WHERE c.parcial_id=? AND c.cancelada=FALSE AND
            """ + VIGENCIA + " ORDER BY c.inicio,s.nombre,s.apellidos,s.usuario";

    private final Base bd;
    private final LocalDateTime ahora;
    private final Reservas reservas;

    Academico(Base bd, LocalDateTime ahora) {
        this.bd = bd;
        this.ahora = ahora;
        this.reservas = new Reservas(bd, ahora);
    }

    static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new IllegalArgumentException(mensaje);
    }

    static String texto(String valor, int maximo, String campo) {
        String limpio = valor.strip();
        exigir(!limpio.isEmpty() && limpio.length() <= maximo, "Revisa " + campo + " (máximo " + maximo + " caracteres).");
        return limpio;
    }

    static String usuario(String valor, boolean estudiante) {
        String limpio = valor.strip().toLowerCase(Locale.ROOT);
        exigir(limpio.matches(estudiante ? "[0-9]{10}" : "[a-z0-9._-]{3,64}"),
                estudiante ? "Ingresa una cédula de 10 dígitos." : "Usa de 3 a 64 letras, números, puntos, guiones o guiones bajos.");
        return limpio;
    }

    private static long id(String valor) {
        long n = Long.parseLong(valor);
        exigir(n > 0, "Selecciona un registro.");
        return n;
    }

    private static List<String[]> mensaje(String mensaje) { return Collections.singletonList(new String[]{mensaje}); }

    private void rol(Usuario u, boolean permitido) {
        exigir(u != null && permitido, "Tu cuenta no tiene permiso para esta acción.");
    }

    private long parcial(Usuario u, String valor) throws SQLException {
        long p = id(valor);
        var fila = bd.uno("""
                SELECT m.docente_id FROM parcial p JOIN materia m ON m.id=p.materia_id WHERE p.id=?
                """, p);
        exigir(fila != null && (u.admin() || Long.parseLong(fila[0]) == u.id()), "El parcial no está disponible para tu cuenta.");
        return p;
    }

    private void cuenta(long id, String rol) throws SQLException {
        exigir(bd.uno("SELECT id FROM cuenta WHERE id=? AND rol=?", id, rol) != null, "La cuenta seleccionada no es válida.");
    }

    private void materia(long id) throws SQLException {
        exigir(bd.uno("SELECT id FROM materia WHERE id=?", id) != null, "La materia seleccionada no existe.");
    }

    private void guardarCuenta(String[] d) throws SQLException {
        boolean estudiante = d[1].equals("ESTUDIANTE");
        exigir(estudiante || d[1].equals("DOCENTE"), "Selecciona Estudiante o Docente.");
        String usuario = usuario(d[2], estudiante);
        String nombres = texto(d[3], 120, "los nombres completos");
        String apellidos = texto(d[4], 120, "los apellidos completos");
        if (d[0].isEmpty()) {
            bd.cambiar("INSERT INTO cuenta(usuario,nombre,apellidos,clave,rol) VALUES(?,?,?,?,?)",
                    usuario, nombres, apellidos, Claves.crear(d[5]), d[1]);
        } else {
            long id = id(d[0]);
            cuenta(id, d[1]);
            if (d[5].isEmpty()) {
                bd.cambiar("UPDATE cuenta SET usuario=?,nombre=?,apellidos=? WHERE id=?", usuario, nombres, apellidos, id);
            } else {
                bd.cambiar("UPDATE cuenta SET usuario=?,nombre=?,apellidos=?,clave=? WHERE id=?",
                        usuario, nombres, apellidos, Claves.crear(d[5]), id);
            }
        }
    }

    private String siguienteCodigoMateria() throws SQLException {
        String numero = bd.uno("""
                SELECT COALESCE(MAX(CAST(codigo AS DECIMAL(24,0))),0)+1
                FROM materia WHERE codigo REGEXP '^[0-9]{1,24}$'
                """)[0];
        exigir(numero.length() <= 24, "Se agotaron los códigos numéricos de materias.");
        return "0".repeat(Math.max(0, 3 - numero.length())) + numero;
    }

    private void guardarMateria(String[] d) throws SQLException {
        String nombre = texto(d[2], 120, "la materia");
        long docente = id(d[3]);
        cuenta(docente, "DOCENTE");
        exigir(d.length == 6, "Selecciona el período académico de la oferta.");
        long periodo = id(d[5]);
        exigir(bd.uno("SELECT id FROM semestre_academico WHERE id=?", periodo) != null,
                "Selecciona un período académico válido.");
        Integer nivel = null;
        if (!d[4].isBlank()) {
            nivel = Integer.parseInt(d[4]);
            exigir(nivel >= 1 && nivel <= 16, "El semestre de la materia debe estar entre 1 y 16.");
        }
        if (d[0].isEmpty()) {
            bd.cambiar("INSERT INTO materia(codigo,nombre,docente_id,nivel,periodo_id) VALUES(?,?,?,?,?)",
                    siguienteCodigoMateria(), nombre, docente, nivel, periodo);
        } else {
            long materia = id(d[0]);
            var actual = bd.uno("SELECT periodo_id FROM materia WHERE id=?", materia);
            exigir(actual != null, "La oferta seleccionada ya no existe.");
            if (!actual[0].equals(Long.toString(periodo))) {
                exigir(bd.uno("SELECT materia_id FROM matricula WHERE materia_id=? LIMIT 1", materia) == null,
                        "No cambies el período de una oferta con matrículas. Crea otra oferta para el nuevo período.");
                var fechas = bd.uno("SELECT MIN(desde),MAX(hasta) FROM parcial WHERE materia_id=? HAVING COUNT(*)>0", materia);
                if (fechas != null) {
                    exigir(bd.uno("SELECT id FROM semestre_academico WHERE id=? AND desde<=? AND hasta>=?",
                            periodo, fechas[0], fechas[1]) != null,
                            "El período nuevo debe contener las fechas de todos los parciales de la oferta.");
                }
            }
            bd.cambiar("UPDATE materia SET nombre=?,docente_id=?,nivel=?,periodo_id=? WHERE id=?",
                    nombre, docente, nivel, periodo, materia);
        }
    }

    private void guardarParcial(String[] d) throws SQLException {
        long materia = id(d[0]);
        materia(materia);
        String nombre = texto(d[1], 60, "el nombre del parcial");
        LocalDate desde = LocalDate.parse(d[2]);
        LocalDate hasta = LocalDate.parse(d[3]);
        exigir(!hasta.isBefore(desde), "La fecha final del parcial debe ser igual o posterior a la inicial.");
        var oferta = bd.uno("SELECT periodo_id FROM materia WHERE id=?", materia);
        if (oferta != null && !oferta[0].isEmpty()) {
            exigir(bd.uno("SELECT id FROM semestre_academico WHERE id=? AND desde<=? AND hasta>=?",
                    oferta[0], desde, hasta) != null, "El parcial debe quedar dentro del período de esta oferta.");
        } else if (bd.uno("SELECT id FROM semestre_academico LIMIT 1") != null) {
            exigir(bd.uno("SELECT id FROM semestre_academico WHERE desde<=? AND hasta>=?", desde, hasta) != null,
                    "El parcial debe quedar dentro de un período académico configurado.");
        }
        var existente = bd.uno("SELECT id FROM parcial WHERE materia_id=? AND nombre=?", materia, nombre);
        long parcial = existente == null ? 0 : id(existente[0]);
        exigir(parcial != 0 || PARCIALES_HABITUALES.contains(nombre), "Elige Primer parcial o Segundo parcial.");
        exigir(bd.uno("SELECT id FROM parcial WHERE materia_id=? AND id<>? AND desde<=? AND hasta>=?", materia, parcial, hasta, desde) == null,
                "Las fechas se cruzan con otro parcial de esta materia.");
        if (nombre.equalsIgnoreCase(PARCIALES_HABITUALES.get(0))) {
            exigir(bd.uno("SELECT id FROM parcial WHERE materia_id=? AND nombre=? AND desde<=?",
                    materia, PARCIALES_HABITUALES.get(1), hasta) == null, "El primer parcial debe terminar antes de que inicie el segundo.");
        } else if (nombre.equalsIgnoreCase(PARCIALES_HABITUALES.get(1))) {
            exigir(bd.uno("SELECT id FROM parcial WHERE materia_id=? AND nombre=? AND hasta>=?",
                    materia, PARCIALES_HABITUALES.get(0), desde) == null, "El segundo parcial debe empezar después de que termine el primero.");
        }
        if (parcial == 0) {
            bd.cambiar("INSERT INTO parcial(materia_id,nombre,desde,hasta) VALUES(?,?,?,?)", materia, nombre, desde, hasta);
        } else {
            exigir(bd.uno("SELECT id FROM clase WHERE parcial_id=? AND (DATE(inicio)<? OR DATE(fin)>?)",
                    parcial, desde, hasta) == null, "Hay clases ya programadas que quedarían fuera de esas fechas.");
            bd.cambiar("UPDATE parcial SET desde=?,hasta=? WHERE id=?", desde, hasta, parcial);
        }
    }

    private void guardarClase(String[] d) throws SQLException {
        long parcial = id(d[1]);
        var fechas = bd.uno("SELECT desde,hasta,materia_id FROM parcial WHERE id=?", parcial);
        exigir(fechas != null, "Selecciona un parcial válido.");
        LocalDateTime inicio = LocalDateTime.parse(d[3], HORARIO);
        LocalDateTime fin = LocalDateTime.parse(d[4], HORARIO);
        int tolerancia = d[5].isBlank() ? TOLERANCIA_PREDETERMINADA : Integer.parseInt(d[5]);
        exigir(fin.isAfter(inicio) && inicio.isAfter(ahora), "Programa una clase futura con fin posterior al inicio.");
        exigir(!inicio.toLocalDate().isBefore(LocalDate.parse(fechas[0]))
                && !fin.toLocalDate().isAfter(LocalDate.parse(fechas[1])), "La clase debe quedar dentro del parcial.");
        exigir(tolerancia >= 0 && inicio.plusMinutes(tolerancia).isBefore(fin), "La tolerancia debe ser menor que la duración.");
        String tema = texto(d[2], 120, "el tema");
        long clase = d[0].isEmpty() ? 0 : id(d[0]);
        String[] anterior = clase == 0 ? null : bd.uno("SELECT paralelo FROM clase WHERE id=?", clase);
        String grupo = d.length == 7 ? Reservas.paralelo(d[6]) : anterior == null ? "A" : anterior[0];
        exigir(anterior == null || anterior[0].equals(grupo), "El paralelo de una clase creada no se puede cambiar.");
        exigir(inicio.getDayOfWeek().getValue() <= 5, "Programa las clases de lunes a viernes.");
        var jornada = Jornada.de(Jornada.minutos(inicio.toLocalTime()), Jornada.minutos(fin.toLocalTime()));
        exigir(inicio.toLocalDate().equals(fin.toLocalDate()) && jornada != null,
                "Programa la clase completa entre 07:00 y 13:00 o entre 14:00 y 20:00.");
        var periodo = bd.uno("SELECT desde,hasta FROM semestre_academico WHERE desde<=? AND hasta>=?",
                inicio.toLocalDate(), inicio.toLocalDate());
        if (periodo == null) periodo = reservas.fechasPeriodoGeneral();
        LocalDate desde = LocalDate.parse(periodo == null ? fechas[0] : periodo[0]);
        LocalDate hasta = LocalDate.parse(periodo == null ? fechas[1] : periodo[1]);
        var anteriorJornada = reservas.jornadaParalelo(Long.parseLong(fechas[2]), grupo, desde, hasta, clase);
        if (anteriorJornada != null) exigir(anteriorJornada == jornada,
                "El paralelo " + grupo + " ya tiene clases en la jornada de " + anteriorJornada.nombre + ".");
        exigir(bd.uno("""
                SELECT c.id FROM clase c JOIN parcial p ON p.id=c.parcial_id
                WHERE p.materia_id=? AND c.id<>? AND c.cancelada=FALSE AND c.inicio<? AND c.fin>?
                """, Long.parseLong(fechas[2]), clase, fin, inicio) == null, "El horario se cruza con otra clase.");
        if (clase == 0) {
            bd.cambiar("INSERT INTO clase(parcial_id,tema,inicio,fin,tolerancia,paralelo) VALUES(?,?,?,?,?,?)",
                    parcial, tema, inicio, fin, tolerancia, grupo);
        } else {
            exigir(bd.cambiar("""
                    UPDATE clase SET parcial_id=?,tema=?,inicio=?,fin=?,tolerancia=?
                    WHERE id=? AND inicio>? AND cancelada=FALSE
                    """, parcial, tema, inicio, fin, tolerancia, clase, ahora) == 1, "Solo se pueden editar clases futuras activas.");
        }
    }

    private void guardarSemestre(String[] d) throws SQLException {
        long semestre = d[0].isEmpty() ? 0 : id(d[0]);
        String nombre = texto(d[1], 80, "el nombre del semestre");
        LocalDate desde = LocalDate.parse(d[2]);
        LocalDate hasta = LocalDate.parse(d[3]);
        exigir(!hasta.isBefore(desde), "El fin del semestre debe ser igual o posterior al inicio.");
        exigir(bd.uno("SELECT id FROM semestre_academico WHERE id<>? AND desde<=? AND hasta>=?",
                semestre, hasta, desde) == null, "Las fechas se cruzan con otro semestre.");
        if (semestre == 0) {
            bd.cambiar("INSERT INTO semestre_academico(nombre,desde,hasta) VALUES(?,?,?)", nombre, desde, hasta);
        } else {
            var anterior = bd.uno("SELECT desde,hasta FROM semestre_academico WHERE id=?", semestre);
            exigir(anterior != null, "El período seleccionado ya no existe.");
            exigir(bd.uno("""
                    SELECT id FROM parcial
                    WHERE desde>=? AND hasta<=? AND (desde<? OR hasta>?) LIMIT 1
                    """, anterior[0], anterior[1], desde, hasta) == null,
                    "No cambies las fechas de un período si algún parcial quedaría fuera de su rango.");
            bd.cambiar("UPDATE semestre_academico SET nombre=?,desde=?,hasta=? WHERE id=?", nombre, desde, hasta, semestre);
        }
    }

    private void matricular(String[] d) throws SQLException {
        long materia = id(d[0]);
        long alumno = id(d[1]);
        materia(materia);
        cuenta(alumno, "ESTUDIANTE");
        switch (d[2]) {
            case "HABILITAR" -> {
                var fila = bd.uno("SELECT estado FROM matricula WHERE materia_id=? AND estudiante_id=?", materia, alumno);
                String estado = fila == null ? "" : fila[0];
                exigir(Set.of("", "RECHAZADA", "HABILITADA").contains(estado), "El estudiante ya tiene una solicitud o matrícula en esta materia.");
                if (fila == null) {
                    bd.cambiar("INSERT INTO matricula(materia_id,estudiante_id,estado) VALUES(?,?,'HABILITADA')", materia, alumno);
                } else if (estado.equals("RECHAZADA")) {
                    bd.cambiar("""
                            UPDATE matricula SET estado='HABILITADA',foto_matricula=NULL,paralelo=NULL
                            WHERE materia_id=? AND estudiante_id=? AND estado='RECHAZADA'
                            """, materia, alumno);
                }
            }
            case "LEGALIZAR", "APROBAR" -> exigir(bd.cambiar("""
                    UPDATE matricula SET estado='APROBADA',desde=?,hasta=NULL
                    WHERE materia_id=? AND estudiante_id=? AND estado='PENDIENTE' AND foto_matricula IS NOT NULL AND paralelo IS NOT NULL
                    """, ahora.toLocalDate(), materia, alumno) == 1,
                    "Para legalizar, el estudiante debe finalizar su matrícula con foto y paralelo.");
            case "RECHAZAR" -> exigir(bd.cambiar("""
                    UPDATE matricula SET estado='RECHAZADA' WHERE materia_id=? AND estudiante_id=? AND estado IN ('HABILITADA','RESERVADA','PENDIENTE')
                    """, materia, alumno) == 1, "Selecciona una materia habilitada, reservada o pendiente.");
            case "RETIRAR" -> exigir(bd.cambiar("""
                    UPDATE matricula SET estado='RETIRADA',hasta=? WHERE materia_id=? AND estudiante_id=? AND estado='APROBADA'
                    """, ahora.toLocalDate().plusDays(1), materia, alumno) == 1, "Solo se pueden retirar matrículas legalizadas.");
            default -> throw new IllegalArgumentException("Acción de matrícula desconocida.");
        }
    }

    private void registrar(String[] d) throws SQLException {
        String cedula = usuario(d[0], true);
        bd.cambiar("INSERT INTO cuenta(usuario,nombre,apellidos,clave,rol) VALUES(?,?,?,?,'ESTUDIANTE')",
                cedula, texto(d[1], 120, "los nombres completos"), texto(d[2], 120, "los apellidos completos"), Claves.crear(d[3]));
    }

    private List<String[]> horarios(long estudiante, long materia) throws SQLException {
        return bd.consultar("""
                SELECT c.id, p.nombre, c.tema, DATE_FORMAT(c.inicio,'%Y-%m-%d %H:%i'),
                    DATE_FORMAT(c.fin,'%Y-%m-%d %H:%i'), COALESCE(a.estado,IF(c.inicio<=?,'ABIERTA','PROGRAMADA')),c.paralelo
                FROM clase c JOIN parcial p ON p.id=c.parcial_id
                JOIN matricula e ON e.materia_id=p.materia_id AND e.estudiante_id=?
                LEFT JOIN asistencia_registro a ON a.clase_id=c.id AND a.estudiante_id=e.estudiante_id
                WHERE p.materia_id=? AND c.fin>? AND c.cancelada=FALSE AND
                """ + VIGENCIA + " ORDER BY c.inicio", ahora, estudiante, materia, ahora);
    }

    private List<String[]> marcar(long estudiante, String[] d) throws SQLException, IOException {
        long clase = id(d[0]);
        var fila = bd.uno("""
                SELECT DATE_FORMAT(c.inicio,'%Y-%m-%d %H:%i'),DATE_FORMAT(c.fin,'%Y-%m-%d %H:%i'),c.tolerancia,p.materia_id
                FROM clase c JOIN parcial p ON p.id=c.parcial_id
                JOIN matricula e ON e.materia_id=p.materia_id AND e.estudiante_id=?
                WHERE c.id=? AND c.cancelada=FALSE AND
                """ + VIGENCIA, estudiante, clase);
        exigir(fila != null, "Necesitas una matrícula legalizada para esta clase.");
        var previa = bd.uno("SELECT estado FROM asistencia_registro WHERE clase_id=? AND estudiante_id=?", clase, estudiante);
        if (previa != null) return mensaje("Ya consta tu registro: " + previa[0] + ".");
        LocalDateTime inicio = LocalDateTime.parse(fila[0], HORARIO);
        LocalDateTime fin = LocalDateTime.parse(fila[1], HORARIO);
        exigir(!ahora.isBefore(inicio) && ahora.isBefore(fin), "La clase está fuera de su horario de registro.");
        byte[] plantilla = bd.obtenerPerfilFacial(estudiante);
        exigir(plantilla != null, "Registra primero tu rostro en la pestaña Face ID.");
        byte[] muestra;
        try { muestra = Base64.getDecoder().decode(d[1]); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("No se pudo validar el rostro capturado."); }
        exigir(coincideRostro(plantilla, muestra), "El rostro no coincide con el perfil Face ID registrado.");
        byte[] foto = fotoValida(d[2]);
        String estado = ahora.isBefore(inicio.plusMinutes(Integer.parseInt(fila[2]) + 1L)) ? "ASISTENCIA" : "ATRASO";
        bd.cambiar("INSERT INTO asistencia_registro(clase_id,estudiante_id,fecha,estado,foto) VALUES(?,?,?,?,?)",
                clase, estudiante, ahora, estado, foto);
        return mensaje("Registro guardado: " + estado + ".");
    }

    private static boolean coincideRostro(byte[] referencia, byte[] muestra) {
        if (referencia == null || muestra == null || referencia.length != 512 || muestra.length != 512) return false;
        java.nio.ByteBuffer a = java.nio.ByteBuffer.wrap(referencia).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        java.nio.ByteBuffer b = java.nio.ByteBuffer.wrap(muestra).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        double producto = 0, normaA = 0, normaB = 0;
        for (int i = 0; i < 128; i++) {
            float x = a.getFloat(), y = b.getFloat();
            if (!Float.isFinite(x) || !Float.isFinite(y)) return false;
            producto += x * y;
            normaA += x * x;
            normaB += y * y;
        }
        return normaA > 0 && normaB > 0 && producto / Math.sqrt(normaA * normaB) >= 0.363;
    }

    private void guardarPerfilFacial(Usuario u, String valor) throws SQLException {
        rol(u, u.estudiante());
        exigir(bd.obtenerPerfilFacial(u.id()) == null, "Ya existe un perfil facial para esta cuenta.");
        byte[] descriptor;
        try { descriptor = Base64.getDecoder().decode(valor); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("La captura facial no es válida."); }
        bd.guardarPerfilFacial(u.id(), descriptor, "SFACE_2021DEC");
    }

    private static byte[] fotoValida(String imagen) {
        byte[] foto;
        try {
            foto = Base64.getDecoder().decode(imagen);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Captura una foto JPEG válida.", e);
        }
        exigir(foto.length > 3 && foto.length <= 1_000_000
                && foto[0] == (byte) 0xff && foto[1] == (byte) 0xd8, "Captura una foto JPEG válida de hasta 1 MB.");
        try (var entrada = new javax.imageio.stream.MemoryCacheImageInputStream(new ByteArrayInputStream(foto))) {
            var lectores = ImageIO.getImageReaders(entrada);
            exigir(lectores.hasNext(), "La imagen no se pudo abrir.");
            var lector = lectores.next();
            try {
                lector.setInput(entrada);
                exigir(lector.getWidth(0) <= 1920 && lector.getHeight(0) <= 1080, "La imagen supera la resolución permitida.");
                lector.read(0);
            } finally {
                lector.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("La foto está incompleta o dañada.", e);
        }
        return foto;
    }

    private List<String[]> detalle(Usuario u, String parcial) throws SQLException {
        return bd.consultar(DETALLE, ahora, ahora, parcial(u, parcial));
    }

    private List<String[]> totalLegalizadas(Usuario u, long materia) throws SQLException {
        var fila = bd.uno("""
                SELECT COUNT(e.estudiante_id)
                FROM materia m LEFT JOIN matricula e ON e.materia_id=m.id
                    AND e.estado IN ('APROBADA','RETIRADA')
                    AND e.desde<=? AND (e.hasta IS NULL OR e.hasta>?)
                WHERE m.id=? AND (? OR m.docente_id=?)
                GROUP BY m.id
                """, ahora.toLocalDate(), ahora.toLocalDate(), materia, u.admin(), u.id());
        rol(u, fila != null);
        return Collections.singletonList(fila);
    }

    static List<String[]> resumen(List<String[]> detalle) {
        Map<String, int[]> conteos = new LinkedHashMap<>();
        Map<String, String[]> alumnos = new LinkedHashMap<>();
        for (String[] fila : detalle) {
            alumnos.putIfAbsent(fila[1], fila);
            int[] c = conteos.computeIfAbsent(fila[1], k -> new int[5]);
            switch (fila[ESTADO_DETALLE]) {
                case "A tiempo" -> c[0]++;
                case "Atraso" -> c[1]++;
                case "Falta sin justificar" -> c[2]++;
                case "Falta justificada" -> c[3]++;
                case "Programada", "En curso sin marca" -> c[4]++;
                default -> throw new IllegalStateException("Estado de asistencia desconocido");
            }
        }
        List<String[]> filas = new ArrayList<>();
        conteos.forEach((id, c) -> {
            String[] a = alumnos.get(id);
            int total = c[0] + c[1] + c[2] + c[3];
            filas.add(new String[]{a[2], a[3], "" + c[0], "" + c[1], "" + c[2], "" + c[3],
                "" + c[4], "" + total, total == 0 ? "" : String.format(Locale.ROOT, "%.2f", 100.0 * (c[0] + c[1]) / total)});
        });
        return filas;
    }

    private long validarClase(long parcial, String clase) throws SQLException {
        long id = id(clase);
        exigir(bd.uno("SELECT id FROM clase WHERE id=? AND parcial_id=? AND cancelada=FALSE", id, parcial) != null,
                "La clase no pertenece al parcial seleccionado.");
        return id;
    }

    private void justificar(Usuario u, String[] d) throws SQLException {
        exigir(u.rol().equals("DOCENTE"), "Solo el docente asignado puede justificar faltas.");
        long parcial = parcial(u, d[0]);
        long clase = validarClase(parcial, d[1]);
        long estudiante = id(d[2]);
        var fila = bd.uno("""
                SELECT c.id FROM clase c JOIN parcial p ON p.id=c.parcial_id
                JOIN matricula e ON e.materia_id=p.materia_id AND e.estudiante_id=?
                LEFT JOIN asistencia_registro a ON a.clase_id=c.id AND a.estudiante_id=e.estudiante_id
                WHERE c.id=? AND c.fin<=? AND a.clase_id IS NULL AND
                """ + VIGENCIA, estudiante, clase, ahora);
        exigir(fila != null, "Solo se puede justificar una falta de una clase finalizada.");
        bd.cambiar("""
                INSERT INTO justificacion(clase_id,estudiante_id,responsable_id,motivo,fecha) VALUES(?,?,?,?,?)
                ON DUPLICATE KEY UPDATE responsable_id=VALUES(responsable_id),motivo=VALUES(motivo),fecha=VALUES(fecha)
                """, clase, estudiante, u.id(), texto(d[3], 500, "el motivo"), ahora);
    }

    List<String[]> ejecutar(Usuario u, String accion, String[] d) throws SQLException, IOException {
        int campos = switch (accion) {
            case "cuentas", "materias", "misMaterias", "siguienteCodigoMateria", "miHorario", "finalizarMatricula", "semestres", "estadoFacial" -> 0;
            case "parciales", "clases", "matriculas", "horarios", "cancelar", "detalle", "totalLegalizadas",
                    "ofertaMatricula", "liberarCupo", "fotoReserva", "cupos", "horarioSemestre" -> 1;
            case "marcar" -> 3;
            case "fotoMatricula", "guardarCupo", "horarioSemanal" -> 2;
            case "guardarPerfilFacial" -> 1;
            case "matricular", "foto", "configurarCupos" -> 3;
            case "guardarParcial", "justificar", "guardarSemestre", "registrar" -> 4;
            case "guardarMateria" -> 6;
            case "guardarCuenta" -> 6;
            case "guardarClase" -> d.length == 7 ? 7 : 6;
            default -> throw new IllegalArgumentException("Operación desconocida.");
        };
        exigir(d.length == campos, "La operación tiene campos incompletos.");
        if (accion.equals("registrar")) {
            registrar(d);
            return mensaje("Cuenta creada. Administración debe habilitar tus materias para que puedas solicitarlas con una foto.");
        }
        rol(u, u != null);
        if (accion.equals("semestres")) {
            var periodos = bd.consultar("SELECT id,nombre,desde,hasta,? FROM semestre_academico ORDER BY desde DESC", ahora.toLocalDate());
            if (!periodos.isEmpty()) return periodos;
            var fechas = reservas.fechasPeriodoGeneral();
            if (fechas == null) return List.of();
            return Collections.singletonList(new String[]{"0", "Período de los parciales", fechas[0], fechas[1], ahora.toLocalDate().toString()});
        }
        if (u.estudiante()) return switch (accion) {
            case "estadoFacial" -> mensaje(bd.obtenerPerfilFacial(u.id()) == null ? "SIN_REGISTRAR" : "REGISTRADO");
            case "guardarPerfilFacial" -> {
                guardarPerfilFacial(u, d[0]);
                yield mensaje("Perfil facial Face ID registrado.");
            }
            case "misMaterias" -> bd.consultar("""
                    SELECT m.id,m.codigo,m.nombre,
                        IF(e.estado='PENDIENTE' AND e.foto_matricula IS NULL,'FALTA FOTO',e.estado),DATE_FORMAT(e.hasta,'%Y-%m-%d'),
                        IF(e.estado IN ('APROBADA','RETIRADA') AND e.desde<=? AND (e.hasta IS NULL OR e.hasta>?), 'SI','NO'),
                        e.paralelo,IF(e.foto_matricula IS NULL,'','Sí')
                    FROM matricula e JOIN materia m ON m.id=e.materia_id WHERE e.estudiante_id=? ORDER BY m.nombre,m.codigo
                    """, ahora.toLocalDate(), ahora.toLocalDate(), u.id());
            case "horarios" -> horarios(u.id(), id(d[0]));
            case "ofertaMatricula" -> reservas.oferta(u.id(), id(d[0]));
            case "miHorario" -> reservas.horario(u.id());
            case "horarioSemanal" -> reservas.horarioSemanal(u.id(), Long.parseLong(d[0]), LocalDate.parse(d[1]));
            case "horarioSemestre" -> reservas.horarioSemestre(u.id(), Long.parseLong(d[0]));
            case "guardarCupo" -> {
                reservas.reservar(u.id(), id(d[0]), d[1]);
                yield mensaje("Cupo guardado para el paralelo " + d[1] + ".");
            }
            case "liberarCupo" -> {
                reservas.liberar(u.id(), id(d[0]));
                yield mensaje("Cupo liberado.");
            }
            case "fotoReserva" -> {
                byte[] foto = fotoValida(d[0]);
                reservas.foto(u.id(), foto);
                yield mensaje("Foto guardada. Revisa tu horario y finaliza la matrícula.");
            }
            case "finalizarMatricula" -> {
                reservas.finalizar(u.id());
                yield mensaje("Matrícula enviada a Administración para su legalización.");
            }
            case "marcar" -> marcar(u.id(), d);
            default -> throw new IllegalArgumentException("Tu cuenta no tiene permiso para esta acción.");
        };
        switch (accion) {
            case "materias":
                if (u.admin()) return bd.consultar("""
                        SELECT m.id,m.codigo,m.nombre,CONCAT_WS(' ',a.nombre,a.apellidos),a.id,m.nivel,
                            m.periodo_id,COALESCE(s.nombre,'Sin período')
                        FROM materia m JOIN cuenta a ON a.id=m.docente_id
                        LEFT JOIN semestre_academico s ON s.id=m.periodo_id ORDER BY m.nombre,s.desde
                        """);
                return bd.consultar("""
                        SELECT m.id,m.codigo,m.nombre,CONCAT_WS(' ',a.nombre,a.apellidos),a.id,m.nivel,
                            m.periodo_id,COALESCE(s.nombre,'Sin período')
                        FROM materia m JOIN cuenta a ON a.id=m.docente_id
                        LEFT JOIN semestre_academico s ON s.id=m.periodo_id
                        WHERE m.docente_id=? ORDER BY m.nombre,s.desde
                        """, u.id());
            case "parciales":
                return bd.consultar("""
                        SELECT p.id,p.nombre,p.desde,p.hasta FROM parcial p JOIN materia m ON m.id=p.materia_id
                        WHERE m.id=? AND (? OR m.docente_id=?) ORDER BY p.desde
                        """, id(d[0]), u.admin(), u.id());
            case "detalle": return detalle(u, d[0]);
            case "totalLegalizadas": return totalLegalizadas(u, id(d[0]));
            case "justificar":
                justificar(u, d);
                return mensaje("Justificación guardada.");
            case "foto":
                byte[] foto = bd.foto(validarClase(parcial(u, d[0]), d[1]), id(d[2]));
                exigir(foto != null, "Este registro no tiene fotografía.");
                return mensaje(Base64.getEncoder().encodeToString(foto));
            default:
                rol(u, u.admin());
        }
        switch (accion) {
            case "cuentas": return bd.consultar("""
                    SELECT id,rol,usuario,CONCAT_WS(' ',nombre,apellidos),nombre,apellidos
                    FROM cuenta WHERE rol<>'ADMIN' ORDER BY rol,nombre,apellidos
                    """);
            case "siguienteCodigoMateria": return mensaje(siguienteCodigoMateria());
            case "matriculas": return bd.consultar("""
                    SELECT s.id,s.usuario,CONCAT_WS(' ',s.nombre,s.apellidos),
                        IF(e.estado='PENDIENTE' AND e.foto_matricula IS NULL,'FALTA FOTO',e.estado),
                        IF(e.foto_matricula IS NULL,'','Sí'),e.desde,e.hasta,e.paralelo
                    FROM matricula e JOIN cuenta s ON s.id=e.estudiante_id WHERE e.materia_id=? ORDER BY s.nombre,s.apellidos
                    """, id(d[0]));
            case "fotoMatricula":
                byte[] evidencia = bd.fotoMatricula(id(d[0]), id(d[1]));
                exigir(evidencia != null, "Esta matrícula no tiene una foto guardada.");
                return mensaje(Base64.getEncoder().encodeToString(evidencia));
            case "clases": return bd.consultar("""
                    SELECT c.id,c.tema,DATE_FORMAT(c.inicio,'%Y-%m-%d %H:%i'),DATE_FORMAT(c.fin,'%Y-%m-%d %H:%i'),
                    c.tolerancia,IF(c.cancelada,'Cancelada','Activa'),c.paralelo
                    FROM clase c WHERE c.parcial_id=? ORDER BY c.inicio
                    """, id(d[0]));
            case "guardarCuenta": guardarCuenta(d); break;
            case "guardarMateria": guardarMateria(d); break;
            case "guardarParcial": guardarParcial(d); break;
            case "guardarSemestre": guardarSemestre(d); break;
            case "guardarClase": guardarClase(d); break;
            case "cupos": return reservas.cupos(id(d[0]));
            case "configurarCupos": reservas.configurar(id(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2])); break;
            case "matricular": matricular(d); break;
            case "cancelar":
                exigir(bd.cambiar("UPDATE clase SET cancelada=TRUE WHERE id=? AND inicio>? AND cancelada=FALSE", id(d[0]), ahora) == 1,
                        "Solo se pueden cancelar clases futuras activas.");
                break;
            default: throw new IllegalArgumentException("Operación desconocida.");
        }
        return mensaje("Cambios guardados.");
    }
}
