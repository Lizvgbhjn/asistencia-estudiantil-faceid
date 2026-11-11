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
        modeloResumen = new Ui.Tabla(detalle, 0, "Cédula", "Estudiante", "A tiempo",
                "Atrasos", "Faltas sin justificar", "Faltas justificadas", "Pendientes", "Clases computadas", "Asistencia (%)");
        ajustarAnchos(detalle, 108, 160, 140, 132, 132, 150, 137, 160, 75);
        ajustarAnchos(detalle, 108, 165, 66, 65, 132, 127, 82, 125, 103);
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        Materia = new javax.swing.JLabel();
        materia = new javax.swing.JComboBox<>();
        Parcial = new javax.swing.JLabel();
        parcial = new javax.swing.JComboBox<>();
        actualizar = new javax.swing.JButton();
        totalLegalizadas = new javax.swing.JLabel();
        tDetalle = new javax.swing.JLabel();
        detalleScroll = new javax.swing.JScrollPane();
        detalle = new javax.swing.JTable();
        foto = new javax.swing.JButton();
        justificar = new javax.swing.JButton();
        csvDetalle = new javax.swing.JButton();
        csvResumen = new javax.swing.JButton();
        tResumen = new javax.swing.JLabel();
        resumenScroll = new javax.swing.JScrollPane();
        resumen = new javax.swing.JTable();
        ayuda = new javax.swing.JLabel();
        Pendientes = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Materia.setText("Materia");

        materia.addActionListener(this::materiaActionPerformed);

        Parcial.setText("Parcial");

        actualizar.setText("Actualizar");
        actualizar.addActionListener(this::actualizarActionPerformed);

        totalLegalizadas.setText("Selecciona una materia para consultar el total de matriculas legalizadas");

        tDetalle.setText("Detalle  de asistencia por clase y detalle");

        detalle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        detalle.setToolTipText("");
        detalleScroll.setViewportView(detalle);

        foto.setText("Ver foto de Asistencia");
        foto.addActionListener(this::fotoActionPerformed);

        justificar.setText("Justificar Falta");
        justificar.addActionListener(this::justificarActionPerformed);

        csvDetalle.setText("Exportar Detalle");
        csvDetalle.addActionListener(this::csvDetalleActionPerformed);

        csvResumen.setText("Exportar Resumen");
        csvResumen.addActionListener(this::csvResumenActionPerformed);

        tResumen.setText("Resumen por estudiante del Parcial Seleccionado");

        resumen.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        resumenScroll.setViewportView(resumen);

        ayuda.setText("% = (a tiempo + atrasos) ÷ clases computadas × 100. Las faltas justificadas cuentan como faltas.");

        Pendientes.setText("Pendientes: clases programadas o en curso sin marca. No cuentan en el porcentaje.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(detalleScroll, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tDetalle)
                                    .addComponent(totalLegalizadas)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(foto, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(justificar, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(csvDetalle, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(csvResumen, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(tResumen))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(Materia)
                                .addGap(18, 18, 18)
                                .addComponent(materia, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(Parcial, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(parcial, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(actualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 172, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(resumenScroll)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ayuda)
                    .addComponent(Pendientes))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Materia)
                    .addComponent(materia, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Parcial)
                    .addComponent(parcial, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(actualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(totalLegalizadas)
                .addGap(18, 18, 18)
                .addComponent(tDetalle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(detalleScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(foto, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(justificar, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(csvDetalle, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(csvResumen, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(tResumen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resumenScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ayuda, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Pendientes, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        materia.getAccessibleContext().setAccessibleParent(this);
        parcial.getAccessibleContext().setAccessibleParent(this);
        detalleScroll.getAccessibleContext().setAccessibleParent(detalleScroll);
        resumenScroll.getAccessibleContext().setAccessibleParent(resumenScroll);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void materiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_materiaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_materiaActionPerformed

    private void actualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actualizarActionPerformed
       Ui.seguro(this, recargar);
    }//GEN-LAST:event_actualizarActionPerformed

    private void fotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fotoActionPerformed
        Ui.seguro(this, () -> {
            String[] fila = modeloDetalle.exigirSeleccion();
            String evidencia = Ui.consultar(this, cliente, "foto", Ui.id(parcial), fila[0], fila[1]).get(0)[0];
            Ui.mostrarFoto(this, evidencia, fila[3] + " · " + fila[5]);
        });
    }//GEN-LAST:event_fotoActionPerformed

    private void justificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_justificarActionPerformed
         Ui.seguro(this, () -> {
            String[] fila = modeloDetalle.exigirSeleccion();
            String motivo = JOptionPane.showInputDialog(this, "Motivo de la justificación", fila[Academico.MOTIVO_DETALLE]);
            if (motivo != null) {
                Ui.consultar(this, cliente, "justificar", Ui.id(parcial), fila[0], fila[1], motivo);
                cargarReporte();
            }
        });
    }//GEN-LAST:event_justificarActionPerformed

    private void csvDetalleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvDetalleActionPerformed
        Ui.seguro(this, () -> modeloDetalle.exportar(this, "detalle-asistencia", contexto));
    }//GEN-LAST:event_csvDetalleActionPerformed

    private void csvResumenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvResumenActionPerformed
        Ui.seguro(this, () -> modeloResumen.exportar(this, "resumen-parcial", contexto));
    }//GEN-LAST:event_csvResumenActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ReportesPanel().setVisible(true));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Materia;
    private javax.swing.JLabel Parcial;
    private javax.swing.JLabel Pendientes;
    private javax.swing.JButton actualizar;
    private javax.swing.JLabel ayuda;
    private javax.swing.JButton csvDetalle;
    private javax.swing.JButton csvResumen;
    private javax.swing.JTable detalle;
    private javax.swing.JScrollPane detalleScroll;
    private javax.swing.JButton foto;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton justificar;
    private javax.swing.JComboBox<Ui.Opcion> materia;
    private javax.swing.JComboBox<Ui.Opcion> parcial;
    private javax.swing.JTable resumen;
    private javax.swing.JScrollPane resumenScroll;
    private javax.swing.JLabel tDetalle;
    private javax.swing.JLabel tResumen;
    private javax.swing.JLabel totalLegalizadas;
    // End of variables declaration//GEN-END:variables
}
