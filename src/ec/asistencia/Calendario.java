package ec.asistencia;

import java.awt.*;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Supplier;
import javax.swing.*;

final class Calendario extends JPanel {
    private static final Locale ESPANOL = Locale.forLanguageTag("es");
    private static final Icon ICONO = new Icon() {
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics dibujo = g.create();
            try {
                dibujo.translate(x, y);
                dibujo.setColor(c.getForeground());
                dibujo.drawRoundRect(1, 3, 15, 13, 3, 3);
                dibujo.drawLine(1, 7, 16, 7);
                dibujo.drawLine(5, 1, 5, 5);
                dibujo.drawLine(12, 1, 12, 5);
                dibujo.fillRect(5, 10, 2, 2);
                dibujo.fillRect(10, 10, 2, 2);
            } finally {
                dibujo.dispose();
            }
        }
    };

    private final JPanel dias = new JPanel(new GridLayout(7, 7, 3, 3));
    private final JLabel mes = new JLabel("", SwingConstants.CENTER);
    private final JLabel seleccion = new JLabel();
    private final JSpinner hora;
    private LocalDate fecha;
    private YearMonth mesVisible;

    private Calendario(LocalDateTime inicial, boolean conHora) {
        super(new BorderLayout(0, 8));
        fecha = inicial.toLocalDate();
        mesVisible = YearMonth.from(fecha);
        JButton anterior = new JButton("‹");
        JButton siguiente = new JButton("›");
        anterior.setToolTipText("Mes anterior");
        siguiente.setToolTipText("Mes siguiente");
        anterior.addActionListener(e -> moverMes(-1));
        siguiente.addActionListener(e -> moverMes(1));
        JPanel cabecera = new JPanel(new BorderLayout(6, 0));
        cabecera.add(anterior, BorderLayout.WEST);
        cabecera.add(mes, BorderLayout.CENTER);
        cabecera.add(siguiente, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);
        add(dias, BorderLayout.CENTER);

        hora = new JSpinner(new SpinnerNumberModel(inicial.getHour(), 0, 23, 1));
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        pie.add(seleccion);
        if (conHora) {
            hora.setEditor(new JSpinner.NumberEditor(hora, "00"));
            hora.getAccessibleContext().setAccessibleName("Hora");
            pie.add(new JLabel("Hora:"));
            pie.add(hora);
            pie.add(new JLabel(":00"));
        }
        add(pie, BorderLayout.SOUTH);
        mostrarMes();
    }

    static void conectar(JButton boton, JTextField campo, String nombre, boolean conHora, Supplier<LocalDateTime> inicial) {
        boton.setText("");
        boton.setIcon(ICONO);
        boton.setMargin(new Insets(2, 2, 2, 2));
        boton.setToolTipText(nombre);
        boton.getAccessibleContext().setAccessibleName(nombre);
        boton.addActionListener(e -> {
            LocalDateTime valor = inicial.get();
            try {
                String texto = campo.getText().strip();
                valor = conHora ? LocalDateTime.parse(texto, Academico.HORARIO) : LocalDate.parse(texto).atStartOfDay();
            } catch (DateTimeParseException ignorado) {
                // El selector comienza con el valor sugerido si el campo está vacío o incompleto.
            }
            Calendario calendario = new Calendario(valor, conHora);
            while (JOptionPane.showConfirmDialog(boton, calendario, nombre, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    campo.setText(calendario.valor(conHora));
                    return;
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(boton, "Ingresa una hora entre 00:00 y 23:59.");
                }
            }
        });
    }

    private void moverMes(int cantidad) {
        mesVisible = mesVisible.plusMonths(cantidad);
        mostrarMes();
    }

    private void mostrarMes() {
        dias.removeAll();
        mes.setText(mesVisible.getMonth().getDisplayName(TextStyle.FULL, ESPANOL) + " " + mesVisible.getYear());
        for (DayOfWeek dia : DayOfWeek.values()) {
            dias.add(new JLabel(dia.getDisplayName(TextStyle.SHORT, ESPANOL), SwingConstants.CENTER));
        }
        int offset = mesVisible.atDay(1).getDayOfWeek().getValue() - 1;
        ButtonGroup grupo = new ButtonGroup();
        for (int casilla = 0; casilla < 42; casilla++) {
            int numero = casilla - offset + 1;
            if (numero < 1 || numero > mesVisible.lengthOfMonth()) {
                dias.add(new JLabel());
                continue;
            }
            LocalDate dia = mesVisible.atDay(numero);
            JToggleButton boton = new JToggleButton(Integer.toString(numero));
            boton.setMargin(new Insets(4, 8, 4, 8));
            boton.setSelected(dia.equals(fecha));
            boton.addActionListener(e -> {
                fecha = dia;
                seleccion.setText("Fecha: " + fecha);
            });
            grupo.add(boton);
            dias.add(boton);
        }
        seleccion.setText("Fecha: " + fecha);
        dias.revalidate();
        dias.repaint();
    }

    private String valor(boolean conHora) throws ParseException {
        if (!conHora) return fecha.toString();
        hora.commitEdit();
        return fecha.atTime((int) hora.getValue(), 0).format(Academico.HORARIO);
    }
}
