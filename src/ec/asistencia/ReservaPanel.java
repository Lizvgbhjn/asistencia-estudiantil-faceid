package ec.asistencia;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

public class ReservaPanel extends javax.swing.JPanel {
    public ReservaPanel() {
        initComponents();

    }

    private Estudiantes.Portal portal;

    void configurar(Estudiantes.Portal portal) {
        this.portal = portal;
    }

    private void guardarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guardarActionPerformed
        Ui.seguro(this, () -> portal.cambioMatricula("guardarCupo", Ui.id(materia), Ui.id(paralelo)));
    }//GEN-LAST:event_guardarActionPerformed

    private void liberarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_liberarActionPerformed
        if (JOptionPane.showConfirmDialog(this, "¿Liberar el cupo de esta materia?", "Liberar cupo",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Ui.seguro(this, () -> portal.cambioMatricula("liberarCupo", Ui.id(materia)));
        }
    }//GEN-LAST:event_liberarActionPerformed

    private void fotoActionPerformed(ActionEvent evt) {//GEN-FIRST:event_fotoActionPerformed
        Ui.seguro(this, () -> {
            String evidencia = Estudiantes.confirmarFoto(this, "Foto de matrícula",
                    "Mira de frente. Esta foto servirá para verificar tu rostro al marcar asistencia.", "Guardar foto");
            if (evidencia != null) portal.cambioMatricula("fotoReserva", evidencia);
        });
    }//GEN-LAST:event_fotoActionPerformed

    private void finalizarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_finalizarActionPerformed
        if (JOptionPane.showConfirmDialog(this, "¿Enviar todos los cupos reservados a revisión de Administración?",
                "Finalizar matrícula", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Ui.seguro(this, () -> portal.cambioMatricula("finalizarMatricula"));
        }
    }//GEN-LAST:event_finalizarActionPerformed

    private void actualizarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_actualizarActionPerformed
        Ui.seguro(this, portal::actualizar);
    }//GEN-LAST:event_actualizarActionPerformed

    private void verHorarioActionPerformed(ActionEvent evt) {//GEN-FIRST:event_verHorarioActionPerformed
        Ui.seguro(this, portal::verHorario);
    }//GEN-LAST:event_verHorarioActionPerformed


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lMateria = new javax.swing.JLabel();
        materia = new javax.swing.JComboBox<>();
        actualizar = new javax.swing.JButton();
        lParalelo = new javax.swing.JLabel();
        paralelo = new javax.swing.JComboBox<>();
        aviso = new javax.swing.JLabel();
        ofertaScroll = new javax.swing.JScrollPane();
        oferta = new javax.swing.JTable();
        guardar = new javax.swing.JButton();
        liberar = new javax.swing.JButton();
        foto = new javax.swing.JButton();
        finalizar = new javax.swing.JButton();
        lHorario = new javax.swing.JLabel();
        verHorario = new javax.swing.JButton();
        horarioScroll = new javax.swing.JScrollPane();
        horario = new javax.swing.JTable();

        lMateria.setText("Materia habilitada");

        actualizar.setText("Actualizar");
        actualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizarActionPerformed(evt);
            }
        });

        lParalelo.setText("Paralelo");

        aviso.setText("Elige una materia y su paralelo.");

        ofertaScroll.setViewportView(oferta);

        guardar.setText("Guardar cupo");
        guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarActionPerformed(evt);
            }
        });

        liberar.setText("Liberar cupo");
        liberar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                liberarActionPerformed(evt);
            }
        });

        foto.setText("Foto de matrícula");
        foto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fotoActionPerformed(evt);
            }
        });

        finalizar.setText("Finalizar matrícula");
        finalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finalizarActionPerformed(evt);
            }
        });

        lHorario.setText("Mis materias · Una fila por asignatura");

        verHorario.setText("Ver horario");
        verHorario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verHorarioActionPerformed(evt);
            }
        });

        horarioScroll.setViewportView(horario);

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
                        .addComponent(materia, 0, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(actualizar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lParalelo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(paralelo, 0, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(aviso))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ofertaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 850, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(guardar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(liberar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(foto, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(finalizar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lHorario)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(verHorario, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(horarioScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 850, Short.MAX_VALUE)))
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
                    .addComponent(lParalelo, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(paralelo, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aviso, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(ofertaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(guardar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(liberar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(foto, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(finalizar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lHorario, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(verHorario, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(horarioScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton actualizar;
    javax.swing.JLabel aviso;
    javax.swing.JButton finalizar;
    javax.swing.JButton foto;
    javax.swing.JButton guardar;
    javax.swing.JTable horario;
    javax.swing.JScrollPane horarioScroll;
    javax.swing.JLabel lHorario;
    javax.swing.JLabel lMateria;
    javax.swing.JLabel lParalelo;
    javax.swing.JButton liberar;
    javax.swing.JComboBox<Ui.Opcion> materia;
    javax.swing.JTable oferta;
    javax.swing.JScrollPane ofertaScroll;
    javax.swing.JComboBox<Ui.Opcion> paralelo;
    javax.swing.JButton verHorario;
    // End of variables declaration//GEN-END:variables
}
