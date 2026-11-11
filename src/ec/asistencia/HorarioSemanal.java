package ec.asistencia;

import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

final class HorarioSemanal {
    private static final String[] COLUMNAS = {"Hora", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
    private static final int DIAS = 5;
    private static final String INICIO_TARDE = hora(Jornada.TARDE.desde * 60);
    private record Bloque(int dia, int desde, int hasta, String materia) {}
    record Cuadricula(String[][] filas, int omitidos) {}

    private final JComponent padre;
    private final Protocolo.Cliente cliente;
    private final String estudiante;
    private final HorarioSemanalPanel panel = new HorarioSemanalPanel();

    private HorarioSemanal(JComponent padre, Protocolo.Cliente cliente, String estudiante, List<String[]> periodos) {
        this.padre = padre;
        this.cliente = cliente;
        this.estudiante = estudiante;
        Ui.opciones(panel.semestre, periodos.stream().map(f -> new Ui.Opcion(f[0],
                f[1] + " · " + f[2] + " a " + f[3])).toList());
        var hoy = LocalDate.parse(periodos.get(0)[4]);
        periodos.stream().filter(f -> !hoy.isBefore(LocalDate.parse(f[2])) && !hoy.isAfter(LocalDate.parse(f[3])))
                .findFirst().ifPresent(f -> Ui.elegir(panel.semestre, f[0]));
        panel.semestre.addActionListener(e -> Ui.seguro(panel, this::cargar));
        Ui.accion(panel.imprimir, panel, () -> panel.tabla.print(JTable.PrintMode.FIT_WIDTH,
                textoImpreso("Horario semanal · " + this.estudiante), textoImpreso(panel.semestre.getSelectedItem().toString())));
        panel.tabla.setRowHeight(37);
        panel.tabla.setRowSelectionAllowed(false);
        panel.tabla.setShowGrid(true);
        panel.tabla.setFillsViewportHeight(true);
        panel.tabla.getTableHeader().setReorderingAllowed(false);
        var renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                    boolean foco, int fila, int columna) {
                super.getTableCellRendererComponent(tabla, valor, false, false, fila, columna);
                String texto = Objects.toString(valor, "");
                String seguro = html(texto).replace("\n", "<br>");
                int ancho = Math.max(20, tabla.getColumnModel().getColumn(columna).getWidth() - 14);
                setText(texto.isEmpty() ? "" : "<html><div style='width:" + ancho + "px'>" + seguro + "</div></html>");
                setHorizontalAlignment(SwingConstants.CENTER);
                setBackground(tabla.getValueAt(fila, 0).toString().compareTo(INICIO_TARDE) >= 0
                        ? new Color(239, 245, 249) : tabla.getBackground());
                setFont(tabla.getFont().deriveFont(columna == 0 ? Font.BOLD : Font.PLAIN));
                setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                setToolTipText(texto.isEmpty() ? null : "<html>" + seguro + "</html>");
                return this;
            }
        };
        panel.tabla.setDefaultRenderer(Object.class, renderer);
    }

    static void abrir(JComponent padre, Protocolo.Cliente cliente, String estudiante) throws Exception {
        var periodos = Ui.esperar(padre, "Consultando semestres…", () -> cliente.pedir("semestres"));
        Academico.exigir(!periodos.isEmpty(), "El horario estará disponible cuando se configuren las fechas de los parciales.");
        var horario = new HorarioSemanal(padre, cliente, estudiante, periodos);
        horario.cargar();
        JOptionPane.showMessageDialog(padre, horario.panel, "Horario semanal del semestre", JOptionPane.PLAIN_MESSAGE);
    }

    private void cargar() throws Exception {
        String semestre = Ui.id(panel.semestre);
        Component espera = SwingUtilities.getWindowAncestor(panel) == null ? padre : panel;
        panel.imprimir.setEnabled(false);
        var bloques = Ui.esperar(espera, "Consultando horario del semestre…", () -> cliente.pedir("horarioSemestre", semestre));
        var horario = cuadricula(bloques);
        String[][] filas = horario.filas();
        panel.tabla.setModel(new DefaultTableModel(filas, COLUMNAS) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        });
        var columnaHora = panel.tabla.getColumnModel().getColumn(0);
        columnaHora.setMinWidth(114);
        columnaHora.setMaxWidth(114);
        for (int fila = 0; fila < filas.length; fila++) {
            int lineas = Arrays.stream(filas[fila]).mapToInt(s -> s.split("\n", -1).length).max().orElse(1);
            panel.tabla.setRowHeight(fila, 18 + 21 * lineas);
        }
        String aviso = "";
        if (horario.omitidos() > 0) aviso = "Revisa en Gestión los bloques fuera de jornada (" + horario.omitidos() + ").";
        else if (filas.length == 0) aviso = "Todavía no tienes clases en este período.";
        panel.aviso.setText(aviso);
        panel.aviso.setVisible(!aviso.isEmpty());
        panel.imprimir.setEnabled(filas.length > 0);
    }

    private static MessageFormat textoImpreso(String texto) {
        return new MessageFormat("'" + texto.replace("'", "''") + "'");
    }

    static Cuadricula cuadricula(List<String[]> datos) {
        Set<Bloque> bloques = new LinkedHashSet<>();
        Set<Jornada> jornadas = EnumSet.noneOf(Jornada.class);
        Map<String, Jornada> jornadasAnteriores = new HashMap<>();
        int omitidos = 0;
        for (String[] fila : datos) {
            int dia = Integer.parseInt(fila[0]);
            if (dia < 0 || dia >= DIAS) continue;
            int inicio = Jornada.minutos(LocalTime.parse(fila[1]));
            long fin = inicio + Long.parseLong(fila[2]);
            var jornada = Jornada.de(inicio, fin);
            if (jornada == null) {
                omitidos++;
                continue;
            }
            String clave = fila[3] + "\u0000" + fila[4];
            var asignada = fila.length > 6 && !fila[6].isEmpty() ? Jornada.valueOf(fila[6])
                    : jornadasAnteriores.computeIfAbsent(clave, k -> jornada);
            if (jornada != asignada) {
                omitidos++;
                continue;
            }
            String nivel = fila.length > 5 ? fila[5] : "";
            int separador = fila[3].indexOf(" · ");
            String nombre = separador < 0 ? fila[3] : fila[3].substring(separador + 3);
            String materia = nombre + " (" + nivel + fila[4] + ")";
            jornadas.add(jornada);
            bloques.add(new Bloque(dia, inicio, (int) fin, materia));
        }
        List<String[]> filas = new ArrayList<>();
        for (Jornada jornada : jornadas) {
            for (int hora = jornada.desde; hora < jornada.hasta; hora++) {
                int inicio = hora * 60, fin = inicio + 60;
                String[] fila = new String[COLUMNAS.length];
                fila[0] = hora(inicio) + " – " + hora(fin);
                for (int dia = 0; dia < DIAS; dia++) {
                    Set<String> materias = new LinkedHashSet<>();
                    for (Bloque bloque : bloques) {
                        if (bloque.dia() == dia && bloque.desde() < fin && inicio < bloque.hasta()) materias.add(bloque.materia());
                    }
                    fila[dia + 1] = String.join("\n", materias);
                }
                filas.add(fila);
            }
        }
        return new Cuadricula(filas.toArray(String[][]::new), omitidos);
    }

    private static String hora(int minutos) {
        return String.format(Locale.ROOT, "%02d:%02d", minutos / 60, minutos % 60);
    }

    private static String html(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
