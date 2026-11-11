package ec.asistencia;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.JOptionPane;

public class ReportesPanel extends javax.swing.JPanel {
    public ReportesPanel() {
        initComponents();
        modeloDetalle = new Ui.Tabla(detalle, 2, "Cédula", "Estudiante", "Clase", "Inicio programado",
                "Fin programado", "Estado de asistencia", "Marca de asistencia", "Motivo de justificación", "Paralelo");
        modeloResumen = new Ui.Tabla(resumen, 0, "Cédula", "Estudiante", "A tiempo",
                "Atrasos", "Faltas sin justificar", "Faltas justificadas", "Pendientes", "Clases computadas", "Asistencia (%)");
        ajustarAnchos(detalle, 108, 160, 140, 132, 132, 150, 137, 160, 75);
        ajustarAnchos(resumen, 108, 165, 66, 65, 132, 127, 82, 125, 103);
        detalle.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) actualizarBotonesDetalle();
        });
        actualizarBotonesDetalle();
        csvDetalle.setEnabled(false);
        csvResumen.setEnabled(false);
    }

    private final Ui.Tabla modeloDetalle;
    private final Ui.Tabla modeloResumen;
    private Protocolo.Cliente cliente;
    private Ui.Accion recargar;
    private boolean puedeJustificar;
    private String[] contexto = {};

    void configurar(Protocolo.Cliente cliente, Ui.Accion recargar, boolean puedeJustificar) {
        this.cliente = cliente;
        this.recargar = recargar;
        this.puedeJustificar = puedeJustificar;
        justificar.setVisible(puedeJustificar);
        actualizarBotonesDetalle();
    }

    void cargarReporte() throws Exception {
        String materiaId = Ui.idOpcional(materia);
        String parcialId = Ui.idOpcional(parcial);
        modeloDetalle.cargar(List.of());
        modeloResumen.cargar(List.of());
        csvDetalle.setEnabled(false);
        csvResumen.setEnabled(false);
        actualizarBotonesDetalle();
        totalLegalizadas.setText("Selecciona una materia para consultar el total de matrículas legalizadas.");
        if (!materiaId.isEmpty()) {
            String total = Ui.consultar(this, cliente, "totalLegalizadas", materiaId).get(0)[0];
            totalLegalizadas.setText("Estudiantes con matrícula legalizada vigente: " + total);
        }
        if (parcialId.isEmpty()) return;
        List<String[]> filas = Ui.consultar(this, cliente, "detalle", parcialId);
        modeloDetalle.cargar(filas);
        modeloResumen.cargar(Academico.resumen(filas));
        csvDetalle.setEnabled(!filas.isEmpty());
        csvResumen.setEnabled(!filas.isEmpty());
        contexto = new String[]{materia.getSelectedItem().toString(), parcial.getSelectedItem().toString()};
    }

    private void ajustarAnchos(JTable tabla, int... anchos) {
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    private void actualizarBotonesDetalle() {
        String[] fila = modeloDetalle.seleccion();
        String estado = fila == null ? "" : fila[Academico.ESTADO_DETALLE];
        foto.setEnabled(Set.of("A tiempo", "Atraso").contains(estado));
        justificar.setEnabled(puedeJustificar && Set.of("Falta sin justificar", "Falta justificada").contains(estado));
        justificar.setText(estado.equals("Falta justificada") ? "Editar justificación" : "Justificar falta");
    }

    private void actualizarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_actualizarActionPerformed
        Ui.seguro(this, recargar);
    }//GEN-LAST:event_actualizarActionPerformed

    private void fotoActionPerformed(ActionEvent evt) {//GEN-FIRST:event_fotoActionPerformed
        Ui.seguro(this, () -> {
            String[] fila = modeloDetalle.exigirSeleccion();
            String evidencia = Ui.consultar(this, cliente, "foto", Ui.id(parcial), fila[0], fila[1]).get(0)[0];
            Ui.mostrarFoto(this, evidencia, fila[3] + " · " + fila[5]);
        });
    }//GEN-LAST:event_fotoActionPerformed

    private void justificarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_justificarActionPerformed
        Ui.seguro(this, () -> {
            String[] fila = modeloDetalle.exigirSeleccion();
            String motivo = JOptionPane.showInputDialog(this, "Motivo de la justificación", fila[Academico.MOTIVO_DETALLE]);
            if (motivo != null) {
                Ui.consultar(this, cliente, "justificar", Ui.id(parcial), fila[0], fila[1], motivo);
                cargarReporte();
            }
        });
    }//GEN-LAST:event_justificarActionPerformed

    private void csvDetalleActionPerformed(ActionEvent evt) {//GEN-FIRST:event_csvDetalleActionPerformed
        Ui.seguro(this, () -> modeloDetalle.exportar(this, "detalle-asistencia", contexto));
    }//GEN-LAST:event_csvDetalleActionPerformed

    private void csvResumenActionPerformed(ActionEvent evt) {//GEN-FIRST:event_csvResumenActionPerformed
        Ui.seguro(this, () -> modeloResumen.exportar(this, "resumen-parcial", contexto));
    }//GEN-LAST:event_csvResumenActionPerformed


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lMateria = new javax.swing.JLabel();
        materia = new javax.swing.JComboBox<>();
        lParcial = new javax.swing.JLabel();
        parcial = new javax.swing.JComboBox<>();
        actualizar = new javax.swing.JButton();
        totalLegalizadas = new javax.swing.JLabel();
        tDetalle = new javax.swing.JLabel();
        detalle = new javax.swing.JTable();
        detalleScroll = new javax.swing.JScrollPane();
        foto = new javax.swing.JButton();
        justificar = new javax.swing.JButton();
        csvDetalle = new javax.swing.JButton();
        csvResumen = new javax.swing.JButton();
        tResumen = new javax.swing.JLabel();
        resumen = new javax.swing.JTable();
        resumenScroll = new javax.swing.JScrollPane();
        ayuda = new javax.swing.JLabel();
        lPendientes = new javax.swing.JLabel();

        lMateria.setText("Materia");
        lParcial.setText("Parcial");
        actualizar.setText("Actualizar");
        actualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizarActionPerformed(evt);
            }
        });
        totalLegalizadas.setText("Selecciona una materia para consultar el total de matrículas legalizadas.");
        tDetalle.setText("Detalle de asistencia por clase y estudiante");
        detalleScroll.setViewportView(detalle);
        foto.setText("Ver foto de asistencia");
        foto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fotoActionPerformed(evt);
            }
        });
        justificar.setText("Justificar falta");
        justificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                justificarActionPerformed(evt);
            }
        });
        csvDetalle.setText("Exportar detalle");
        csvDetalle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvDetalleActionPerformed(evt);
            }
        });
        csvResumen.setText("Exportar resumen");
        csvResumen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvResumenActionPerformed(evt);
            }
        });
        tResumen.setText("Resumen por estudiante del parcial seleccionado");
        resumenScroll.setViewportView(resumen);
        ayuda.setText("% = (a tiempo + atrasos) ÷ clases computadas × 100. Las faltas justificadas cuentan como faltas.");
        lPendientes.setText("Pendientes: clases programadas o en curso sin marca. No cuentan en el porcentaje.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup().addGap(16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lMateria, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(materia, javax.swing.GroupLayout.DEFAULT_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(lParcial, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(parcial, javax.swing.GroupLayout.DEFAULT_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(actualizar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(totalLegalizadas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(detalleScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 990, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(foto, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(justificar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(csvDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(csvResumen, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tResumen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resumenScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 990, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ayuda, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lPendientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup().addGap(16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lMateria, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(materia, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lParcial, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(parcial, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(actualizar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(totalLegalizadas, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(detalleScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(foto, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(justificar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(csvDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(csvResumen, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tResumen, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(resumenScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(ayuda, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lPendientes, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JLabel lMateria;
    javax.swing.JComboBox<Ui.Opcion> materia;
    javax.swing.JLabel lParcial;
    javax.swing.JComboBox<Ui.Opcion> parcial;
    javax.swing.JButton actualizar;
    javax.swing.JLabel totalLegalizadas;
    javax.swing.JLabel tDetalle;
    javax.swing.JTable detalle;
    javax.swing.JScrollPane detalleScroll;
    javax.swing.JButton foto;
    javax.swing.JButton justificar;
    javax.swing.JButton csvDetalle;
    javax.swing.JButton csvResumen;
    javax.swing.JLabel tResumen;
    javax.swing.JTable resumen;
    javax.swing.JScrollPane resumenScroll;
    javax.swing.JLabel ayuda;
    javax.swing.JLabel lPendientes;
    // End of variables declaration//GEN-END:variables
}
