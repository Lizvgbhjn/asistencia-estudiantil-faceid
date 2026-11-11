package ec.asistencia;

public class HorarioSemanalPanel extends javax.swing.JPanel {
    public HorarioSemanalPanel() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lSemestre = new javax.swing.JLabel();
        semestre = new javax.swing.JComboBox<>();
        imprimir = new javax.swing.JButton();
        aviso = new javax.swing.JLabel();
        tablaScroll = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();

        lSemestre.setText("Semestre");

        imprimir.setText("Imprimir");

        aviso.setText("Mañana 07:00–13:00 · Tarde 14:00–20:00");

        tablaScroll.setViewportView(tabla);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lSemestre)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(semestre, 0, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(imprimir, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(aviso))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tablaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 950, Short.MAX_VALUE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lSemestre, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(semestre, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(imprimir, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(aviso, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tablaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JLabel aviso;
    javax.swing.JButton imprimir;
    javax.swing.JLabel lSemestre;
    javax.swing.JComboBox<Ui.Opcion> semestre;
    javax.swing.JTable tabla;
    javax.swing.JScrollPane tablaScroll;
    // End of variables declaration//GEN-END:variables
}
