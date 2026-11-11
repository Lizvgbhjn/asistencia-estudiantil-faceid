    package ec.asistencia;

import java.awt.event.ActionEvent;

public class AsistenciaPanel extends javax.swing.JPanel {
    public AsistenciaPanel() {
        initComponents();
        capturar.setEnabled(false);

    }

    private Estudiantes.Portal portal;

    void configurar(Estudiantes.Portal portal) {
        this.portal = portal;
    }

    private void actualizarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_actualizarActionPerformed
        Ui.seguro(this, portal::actualizar);
    }//GEN-LAST:event_actualizarActionPerformed

    private void capturarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_capturarActionPerformed
        Ui.seguro(this, portal::marcar);
    }//GEN-LAST:event_capturarActionPerformed


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lMateria = new javax.swing.JLabel();
        materia = new javax.swing.JComboBox<>();
        actualizar = new javax.swing.JButton();
        estado = new javax.swing.JLabel();
        horariosScroll = new javax.swing.JScrollPane();
        horarios = new javax.swing.JTable();
        capturar = new javax.swing.JButton();
        lMatriculas = new javax.swing.JLabel();
        matriculasScroll = new javax.swing.JScrollPane();
        matriculas = new javax.swing.JTable();

        lMateria.setText("Materia matriculada");

        actualizar.setText("Actualizar");
        actualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizarActionPerformed(evt);
            }
        });

        estado.setText("Selecciona el horario en la tabla y registra tu asistencia.");

        horariosScroll.setViewportView(horarios);

        capturar.setText("Verificar y marcar");
        capturar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                capturarActionPerformed(evt);
            }
        });

        lMatriculas.setText("Estado de tus matrículas");

        matriculasScroll.setViewportView(matriculas);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lMateria)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(materia, 0, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(actualizar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(estado))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(horariosScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 790, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(capturar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lMatriculas))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(matriculasScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 790, Short.MAX_VALUE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lMateria, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(materia, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(actualizar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(estado, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(horariosScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(capturar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lMatriculas, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(matriculasScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton actualizar;
    javax.swing.JButton capturar;
    javax.swing.JLabel estado;
    javax.swing.JTable horarios;
    javax.swing.JScrollPane horariosScroll;
    javax.swing.JLabel lMateria;
    javax.swing.JLabel lMatriculas;
    javax.swing.JComboBox<Ui.Opcion> materia;
    javax.swing.JTable matriculas;
    javax.swing.JScrollPane matriculasScroll;
    // End of variables declaration//GEN-END:variables
}
