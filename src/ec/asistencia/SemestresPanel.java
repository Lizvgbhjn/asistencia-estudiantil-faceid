package ec.asistencia;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.*;

final class SemestresPanel extends JPanel {
    private final JTextField nombre = new JTextField(24);
    private final JTextField desde = new JTextField(10);
    private final JTextField hasta = new JTextField(10);
    private final JButton calendarioDesde = new JButton("…");
    private final JButton calendarioHasta = new JButton("…");
    private final JButton nuevo = new JButton("Nuevo período");
    private final JButton guardar = new JButton("Guardar período");
    private final JButton actualizar = new JButton("Actualizar");
    private final JTable tabla = new JTable();
    private final Ui.Tabla modelo = new Ui.Tabla(tabla, 1, "Período académico", "Desde", "Hasta");
    private Protocolo.Cliente cliente;
    private Ui.Accion recargar;
    private String periodoId = "";

    SemestresPanel() {
        super(new BorderLayout(8, 8));
        var formulario = new JPanel(new GridLayout(0, 1, 6, 6));
        formulario.setBorder(BorderFactory.createEmptyBorder(16, 16, 4, 16));
        var filaNombre = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        filaNombre.add(new JLabel("Nombre del período"));
        filaNombre.add(nombre);
        formulario.add(filaNombre);
        var filaFechas = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        filaFechas.add(new JLabel("Desde")); filaFechas.add(desde); filaFechas.add(calendarioDesde);
        filaFechas.add(new JLabel("Hasta")); filaFechas.add(hasta); filaFechas.add(calendarioHasta);
        formulario.add(filaFechas);
        var acciones = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        acciones.add(nuevo); acciones.add(guardar); acciones.add(actualizar);
        formulario.add(acciones);
        add(formulario, BorderLayout.NORTH);
        var scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        add(scroll, BorderLayout.CENTER);

        Calendario.conectar(calendarioDesde, desde, "Inicio del período", false, java.time.LocalDateTime::now);
        Calendario.conectar(calendarioHasta, hasta, "Fin del período", false, java.time.LocalDateTime::now);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            var fila = modelo.seleccion();
            if (fila == null) return;
            periodoId = fila[0];
            nombre.setText(fila[1]);
            desde.setText(fila[2]);
            hasta.setText(fila[3]);
            guardar.setText("Guardar cambios");
        });
        nuevo.addActionListener(e -> limpiar());
        guardar.addActionListener(e -> Ui.seguro(this, this::guardar));
        actualizar.addActionListener(e -> Ui.seguro(this, this::cargar));
    }

    void configurar(Protocolo.Cliente cliente, Ui.Accion recargar) { this.cliente = cliente; this.recargar = recargar; }

    void cargar() throws Exception { modelo.cargar(Ui.consultar(this, cliente, "semestres")); }

    private void limpiar() {
        tabla.clearSelection();
        periodoId = "";
        nombre.setText(""); desde.setText(""); hasta.setText("");
        guardar.setText("Guardar período");
    }

    private void guardar() throws Exception {
        Ui.consultar(this, cliente, "guardarSemestre", periodoId, nombre.getText().strip(),
                desde.getText().strip(), hasta.getText().strip());
        limpiar();
        recargar.ejecutar();
    }
}
