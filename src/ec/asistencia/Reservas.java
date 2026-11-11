package ec.asistencia;

import java.sql.SQLException;
import java.time.*;
import java.util.List;

import static ec.asistencia.Academico.exigir;

final class Reservas {
    private static final String OCUPA_CUPO = """
            (estado IN ('RESERVADA','APROBADA') OR (estado='PENDIENTE' AND foto_matricula IS NOT NULL)
                OR (estado='RETIRADA' AND hasta>?))
            """;
    private static final String CLASES_DEL_ALUMNO = """
            FROM matricula e JOIN materia m ON m.id=e.materia_id JOIN parcial p ON p.materia_id=m.id
            JOIN clase c ON c.parcial_id=p.id AND c.paralelo=COALESCE(e.paralelo,'A')
            WHERE e.estudiante_id=? AND c.cancelada=FALSE AND
            """;
    private static final String HORARIO = """
            SELECT c.id,CONCAT(m.codigo,' · ',m.nombre),c.paralelo,p.nombre,c.tema,
                DATE_FORMAT(c.inicio,'%Y-%m-%d %H:%i'),DATE_FORMAT(c.fin,'%Y-%m-%d %H:%i'),e.estado,m.id
            """ + CLASES_DEL_ALUMNO;
    private static final String MATRICULA_EN_CLASE = """
            (e.estado='RESERVADA' OR (e.estado='PENDIENTE' AND e.foto_matricula IS NOT NULL)
             OR (e.estado IN ('APROBADA','RETIRADA') AND e.desde<=DATE(c.inicio)
                 AND (e.hasta IS NULL OR e.hasta>DATE(c.inicio))))
            """;
    private static final String JORNADA_DEL_PARALELO = """
            SELECT IF(HOUR(j.inicio)<13,'MANANA','TARDE')
            FROM clase j JOIN parcial jp ON jp.id=j.parcial_id
            WHERE j.inicio>=? AND j.inicio<? AND j.cancelada=FALSE AND WEEKDAY(j.inicio)<5
                AND DATE(j.inicio)=DATE(j.fin)
                AND ((TIME(j.inicio)>='07:00:00' AND TIME(j.fin)<='13:00:00')
                    OR (TIME(j.inicio)>='14:00:00' AND TIME(j.fin)<='20:00:00'))
            """;
    private final Base bd;
    private final LocalDateTime ahora;

    Reservas(Base bd, LocalDateTime ahora) {
        this.bd = bd;
        this.ahora = ahora;
    }

    static String paralelo(String valor) {
        exigir(valor.equals("A") || valor.equals("B"), "Elige el paralelo A o B.");
        return valor;
    }

    private String[] matricula(long alumno, long materia) throws SQLException {
        var fila = bd.uno("""
                SELECT estado,paralelo,IF(foto_matricula IS NULL,'NO','SI')
                FROM matricula WHERE estudiante_id=? AND materia_id=?
                """, alumno, materia);
        exigir(fila != null, "Administración debe habilitarte esta materia.");
        return fila;
    }

    private String[] limites(long materia) throws SQLException {
        var fila = bd.uno("SELECT cupos_a,cupos_b FROM materia WHERE id=? FOR UPDATE", materia);
        exigir(fila != null, "Selecciona una materia válida.");
        return fila;
    }

    private int ocupados(long materia, String paralelo) throws SQLException {
        return Integer.parseInt(bd.uno("""
                SELECT COUNT(*) FROM matricula WHERE materia_id=? AND COALESCE(paralelo,'A')=? AND
                """ + OCUPA_CUPO, materia, paralelo, ahora.toLocalDate())[0]);
    }

    List<String[]> cupos(long materia) throws SQLException {
        var fila = limites(materia);
        return List.of(new String[]{"A", fila[0], "" + ocupados(materia, "A")},
                new String[]{"B", fila[1], "" + ocupados(materia, "B")});
    }

    void configurar(long materia, int a, int b) throws SQLException {
        exigir(a >= 0 && a <= 500 && b >= 0 && b <= 500, "Ingresa entre 0 y 500 cupos por paralelo.");
        limites(materia);
        exigir(a >= ocupados(materia, "A") && b >= ocupados(materia, "B"), "Los cupos no pueden ser menores que las reservas y matrículas vigentes.");
        bd.cambiar("UPDATE materia SET cupos_a=?,cupos_b=? WHERE id=?", a, b, materia);
    }

    List<String[]> oferta(long alumno, long materia) throws SQLException {
        exigir(puedeReservar(matricula(alumno, materia)), "Esta matrícula ya no está disponible para reservar.");
        var cupos = cupos(materia);
        return bd.consultar("""
                SELECT c.id,c.paralelo,p.nombre,c.tema,DATE_FORMAT(c.inicio,'%Y-%m-%d %H:%i'),
                    DATE_FORMAT(c.fin,'%Y-%m-%d %H:%i'),IF(c.paralelo='A',?,?)
                FROM clase c JOIN parcial p ON p.id=c.parcial_id
                WHERE p.materia_id=? AND c.cancelada=FALSE AND c.inicio>?
                ORDER BY c.paralelo,c.inicio
                """, disponibles(cupos.get(0)), disponibles(cupos.get(1)), materia, ahora);
    }

    private int disponibles(String[] fila) {
        return Math.max(0, Integer.parseInt(fila[1]) - Integer.parseInt(fila[2]));
    }

    private boolean puedeReservar(String[] fila) {
        return fila[0].equals("HABILITADA") || fila[0].equals("RESERVADA")
                || (fila[0].equals("PENDIENTE") && fila[2].equals("NO"));
    }

    void reservar(long alumno, long materia, String grupo) throws SQLException {
        paralelo(grupo);
        String[] cupos = limites(materia);
        var fila = matricula(alumno, materia);
        exigir(puedeReservar(fila), "Esta matrícula ya no está disponible para reservar.");
        if (fila[0].equals("RESERVADA") && fila[1].equals(grupo)) return;
        exigir(bd.uno("""
                SELECT c.id FROM clase c JOIN parcial p ON p.id=c.parcial_id
                WHERE p.materia_id=? AND c.paralelo=? AND c.cancelada=FALSE AND c.inicio>?
                """, materia, grupo, ahora) != null, "El paralelo aún no tiene clases futuras disponibles.");
        int capacidad = Integer.parseInt(cupos[grupo.equals("A") ? 0 : 1]);
        exigir(capacidad > ocupados(materia, grupo), "El paralelo ya no tiene cupos disponibles.");
        exigir(bd.uno("""
                SELECT c.id FROM clase c JOIN parcial p ON p.id=c.parcial_id
                JOIN matricula e ON e.estudiante_id=? AND e.materia_id<>p.materia_id
                JOIN parcial otro ON otro.materia_id=e.materia_id
                JOIN clase o ON o.parcial_id=otro.id AND o.paralelo=COALESCE(e.paralelo,'A')
                WHERE p.materia_id=? AND c.paralelo=? AND c.cancelada=FALSE AND o.cancelada=FALSE
                    AND c.inicio>? AND o.fin>? AND c.inicio<o.fin AND o.inicio<c.fin
                    AND (e.estado IN ('RESERVADA','APROBADA') OR (e.estado='PENDIENTE' AND e.foto_matricula IS NOT NULL) OR
                        (e.estado='RETIRADA' AND e.hasta>DATE(o.inicio)))
                LIMIT 1
                """, alumno, materia, grupo, ahora, ahora) == null, "Ese paralelo se cruza con otra materia de tu horario.");
        exigir(bd.cambiar("""
                UPDATE matricula SET estado='RESERVADA',paralelo=?,foto_matricula=NULL
                WHERE estudiante_id=? AND materia_id=? AND (estado IN ('HABILITADA','RESERVADA') OR
                    (estado='PENDIENTE' AND foto_matricula IS NULL))
                """, grupo, alumno, materia) == 1, "No se pudo guardar el cupo. Actualiza tus matrículas.");
    }

    void liberar(long alumno, long materia) throws SQLException {
        limites(materia);
        exigir(bd.cambiar("""
                UPDATE matricula SET estado='HABILITADA',paralelo=NULL,foto_matricula=NULL
                WHERE estudiante_id=? AND materia_id=? AND estado='RESERVADA'
                """, alumno, materia) == 1, "Solo se pueden liberar cupos aún reservados.");
    }

    void foto(long alumno, byte[] foto) throws SQLException {
        exigir(bd.uno("SELECT materia_id FROM matricula WHERE estudiante_id=? AND estado='RESERVADA' LIMIT 1", alumno) != null,
                "Guarda al menos un cupo antes de tomar la foto.");
        bd.cambiar("UPDATE matricula SET foto_matricula=? WHERE estudiante_id=? AND estado='RESERVADA'", foto, alumno);
    }

    void finalizar(long alumno) throws SQLException {
        var fila = bd.uno("""
                SELECT COUNT(*),COALESCE(SUM(foto_matricula IS NULL OR paralelo IS NULL),0)
                FROM matricula WHERE estudiante_id=? AND estado='RESERVADA'
                """, alumno);
        exigir(!fila[0].equals("0"), "Primero guarda un cupo.");
        exigir(fila[1].equals("0"), "Toma la foto de matrícula antes de finalizar.");
        bd.cambiar("UPDATE matricula SET estado='PENDIENTE' WHERE estudiante_id=? AND estado='RESERVADA'", alumno);
    }

    List<String[]> horario(long alumno) throws SQLException {
        return bd.consultar(HORARIO + """
                c.fin>? AND
                    (e.estado IN ('RESERVADA','APROBADA') OR (e.estado='PENDIENTE' AND e.foto_matricula IS NOT NULL)
                        OR (e.estado='RETIRADA' AND e.hasta>DATE(c.inicio)))
                ORDER BY c.inicio,m.codigo
                """, alumno, ahora);
    }

    List<String[]> horarioSemanal(long alumno, long semestre, LocalDate lunes) throws SQLException {
        exigir(lunes.getDayOfWeek() == DayOfWeek.MONDAY, "Selecciona una semana que empiece el lunes.");
        var fechas = fechasSemestre(semestre);
        LocalDate desde = LocalDate.parse(fechas[0]);
        LocalDate hasta = LocalDate.parse(fechas[1]).plusDays(1);
        if (lunes.isAfter(desde)) desde = lunes;
        LocalDate sabado = lunes.plusDays(5);
        if (sabado.isBefore(hasta)) hasta = sabado;
        if (!desde.isBefore(hasta)) return List.of();
        return bd.consultar(HORARIO + """
                c.inicio>=? AND c.inicio<? AND
                """ + MATRICULA_EN_CLASE + " ORDER BY c.inicio,m.codigo", alumno, desde.atStartOfDay(), hasta.atStartOfDay());
    }

    List<String[]> horarioSemestre(long alumno, long semestre) throws SQLException {
        var fechas = fechasSemestre(semestre);
        var inicio = LocalDate.parse(fechas[0]).atStartOfDay();
        var fin = LocalDate.parse(fechas[1]).plusDays(1).atStartOfDay();
        return bd.consultar("""
                SELECT DISTINCT WEEKDAY(c.inicio),DATE_FORMAT(c.inicio,'%H:%i'),
                    TIMESTAMPDIFF(MINUTE,c.inicio,LEAST(c.fin,?)),CONCAT(m.codigo,' · ',m.nombre),c.paralelo,m.nivel,
                    (
                """ + JORNADA_DEL_PARALELO + """
                AND jp.materia_id=m.id AND j.paralelo=c.paralelo ORDER BY j.id LIMIT 1
                )
                """ + CLASES_DEL_ALUMNO + """
                c.inicio>=? AND c.inicio<? AND WEEKDAY(c.inicio)<5 AND
                """ + MATRICULA_EN_CLASE + " ORDER BY 1,2,4,5,3", fin, inicio, fin, alumno, inicio, fin);
    }

    Jornada jornadaParalelo(long materia, String paralelo, LocalDate desde, LocalDate hasta, long excluirClase) throws SQLException {
        var fila = bd.uno(JORNADA_DEL_PARALELO + """
                AND jp.materia_id=? AND j.paralelo=? AND j.id<>? ORDER BY j.id LIMIT 1
                """, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), materia, paralelo, excluirClase);
        return fila == null ? null : Jornada.valueOf(fila[0]);
    }

    String[] fechasPeriodoGeneral() throws SQLException {
        return bd.uno("""
                SELECT MIN(desde),MAX(hasta) FROM parcial
                WHERE NOT EXISTS (SELECT 1 FROM semestre_academico)
                HAVING COUNT(*)>0
                """);
    }

    private String[] fechasSemestre(long semestre) throws SQLException {
        var fechas = semestre == 0 ? fechasPeriodoGeneral()
                : bd.uno("SELECT desde,hasta FROM semestre_academico WHERE id=?", semestre);
        exigir(fechas != null, "Selecciona un semestre válido.");
        return fechas;
    }
}
