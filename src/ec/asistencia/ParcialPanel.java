package ec.asistencia;

public class ParcialPanel extends javax.swing.JPanel {
    public ParcialPanel() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lNombre = new javax.swing.JLabel();
        nombre = new javax.swing.JComboBox<>();
        lDesde = new javax.swing.JLabel();
        desde = new javax.swing.JTextField();
        calDesde = new javax.swing.JButton();
        lHasta = new javax.swing.JLabel();
        hasta = new javax.swing.JTextField();
        calHasta = new javax.swing.JButton();

        lNombre.setText("Parcial");
        lDesde.setText("Desde (AAAA-MM-DD)");
        calDesde.setText("▦");
        lHasta.setText("Hasta (AAAA-MM-DD)");
        calHasta.setText("▦");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup().addGap(16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(nombre, javax.swing.GroupLayout.DEFAULT_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lDesde, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(desde, javax.swing.GroupLayout.DEFAULT_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(calDesde, javax.swing.GroupLayout.DEFAULT_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lHasta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(hasta, javax.swing.GroupLayout.DEFAULT_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(calHasta, javax.swing.GroupLayout.DEFAULT_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup().addGap(16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nombre, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lDesde, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(desde, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(calDesde, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lHasta, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hasta, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(calHasta, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JLabel lNombre;
    javax.swing.JComboBox<Ui.Opcion> nombre;
    javax.swing.JLabel lDesde;
    javax.swing.JTextField desde;
    javax.swing.JButton calDesde;
    javax.swing.JLabel lHasta;
    javax.swing.JTextField hasta;
    javax.swing.JButton calHasta;
    // End of variables declaration//GEN-END:variables
}
