package ec.asistencia;

public class AccesoPanel extends javax.swing.JPanel {
    public AccesoPanel() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lUsuario = new javax.swing.JLabel();
        usuario = new javax.swing.JTextField();
        lClave = new javax.swing.JLabel();
        clave = new javax.swing.JPasswordField();
        entrar = new javax.swing.JButton();
        registro = new javax.swing.JButton();

        lUsuario.setText("Usuario");

        lClave.setText("Clave");

        entrar.setText("Ingresar");

        registro.setText("Registrarse");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(usuario, javax.swing.GroupLayout.DEFAULT_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lClave, javax.swing.GroupLayout.DEFAULT_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(clave, javax.swing.GroupLayout.DEFAULT_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(entrar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(registro, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usuario, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lClave, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clave, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(entrar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(registro, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JPasswordField clave;
    javax.swing.JButton entrar;
    javax.swing.JLabel lClave;
    javax.swing.JLabel lUsuario;
    javax.swing.JButton registro;
    javax.swing.JTextField usuario;
    // End of variables declaration//GEN-END:variables
}
