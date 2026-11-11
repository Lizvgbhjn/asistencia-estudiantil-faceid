package ec.asistencia;

import java.time.LocalTime;

enum Jornada {
    MANANA(7, 13, "mañana"),
    TARDE(14, 20, "tarde");

    final int desde;
    final int hasta;
    final String nombre;

    Jornada(int desde, int hasta, String nombre) {
        this.desde = desde;
        this.hasta = hasta;
        this.nombre = nombre;
    }

    static int minutos(LocalTime hora) {
        return hora.toSecondOfDay() / 60;
    }

    static Jornada de(int inicio, long fin) {
        for (Jornada jornada : values()) {
            if (inicio >= jornada.desde * 60 && inicio < fin && fin <= jornada.hasta * 60) return jornada;
        }
        return null;
    }
}
