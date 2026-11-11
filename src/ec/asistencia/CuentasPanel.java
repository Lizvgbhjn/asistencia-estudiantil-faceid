package ec.asistencia;

import java.awt.event.ActionEvent;
import java.util.List;

public class CuentasPanel extends javax.swing.JPanel {
    public CuentasPanel() {
        initComponents();
        modelo = new Ui.Tabla(tabla, 1, "Tipo", "Usuario / cédula", "Nombres y apellidos");
        Ui.opciones(rol, List.of(new Ui.Opcion("ESTUDIANTE", "Estudiante"), new Ui.Opcion("DOCENTE", "Docente")));
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarCuenta();
        });
    }

    private final Ui.Tabla modelo;
    private Protocolo.Cliente cliente;
    private Ui.Accion recargar;
    private String cuentaId = "";

    void configurar(Protocolo.Cliente cliente, Ui.Accion recargar) {
        this.cliente = cliente;
        this.recargar = recargar;
    }

    void cargarCuentas(List<String[]> filas) {
        modelo.cargar(filas);
    }

    private void mostrarCuenta() {
        String[] fila = modelo.seleccion();
        if (fila == null) return;
        cuentaId = fila[0];
        Ui.elegir(rol, fila[1]);
        rol.setEnabled(false);
        usuario.setText(fila[2]);
        nombre.setText(fila[4]);
        apellidos.setText(fila[5]);
        nombre.setToolTipText(fila[5].isEmpty() ? "Separa los nombres y apellidos de esta cuenta al guardarla." : null);
        clave.setText("");
    }

    private void limpiarCuenta() {
        tabla.clearSelection();
        cuentaId = "";
        rol.setEnabled(true);
        usuario.setText("");
        nombre.setText("");
        nombre.setToolTipText(null);
        apellidos.setText("");
        clave.setText("");
    }

    private void guardarCuenta() throws Exception {
        Ui.consultar(this, cliente, "guardarCuenta", cuentaId, Ui.id(rol),
                usuario.getText(), nombre.getText(), apellidos.getText(), Ui.clave(clave));
        limpiarCuenta();
        recargar.ejecutar();
    }

    private void nuevoActionPerformed(ActionEvent evt) {//GEN-FIRST:event_nuevoActionPerformed
        limpiarCuenta();
    }//GEN-LAST:event_nuevoActionPerformed

    private void guardarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guardarActionPerformed
        Ui.seguro(this, this::guardarCuenta);
    }//GEN-LAST:event_guardarActionPerformed

    private void actualizarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_actualizarActionPerformed
        limpiarCuenta();
        Ui.seguro(this, recargar);
    }//GEN-LAST:event_actualizarActionPerformed


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lRol = new javax.swing.JLabel();
        rol = new javax.swing.JComboBox<>();
        lUsuario = new javax.swing.JLabel();
        usuario = new javax.swing.JTextField();
        lNombre = new javax.swing.JLabel();
        nombre = new javax.swing.JTextField();
        lApellidos = new javax.swing.JLabel();
        apellidos = new javax.swing.JTextField();
        lClave = new javax.swing.JLabel();
        clave = new javax.swing.JPasswordField();
        nuevo = new javax.swing.JButton();
        guardar = new javax.swing.JButton();
        actualizar = new javax.swing.JButton();
        tabla = new javax.swing.JTable();
        tablaScroll = new javax.swing.JScrollPane();

        lRol.setText("Tipo de cuenta");
        lUsuario.setText("Usuario o cédula");
        lNombre.setText("Nombres completos");
        lApellidos.setText("Apellidos completos");
        lClave.setText("Clave nueva");
        nuevo.setText("Nueva cuenta");
        nuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevoActionPerformed(evt);
            }
        });
        guardar.setText("Guardar");
        guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarActionPerformed(evt);
            }
        });
        actualizar.setText("Actualizar");
        actualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizarActionPerformed(evt);
            }
        });
        tablaScroll.setViewportView(tabla);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup().addGap(16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lRol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(rol, javax.swing.GroupLayout.DEFAULT_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(lUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(usuario, javax.swing.GroupLayout.DEFAULT_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(nombre, javax.swing.GroupLayout.DEFAULT_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(lApellidos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(apellidos, javax.swing.GroupLayout.DEFAULT_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lClave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(clave, javax.swing.GroupLayout.DEFAULT_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nuevo, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(guardar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(actualizar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tablaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 820, Short.MAX_VALUE)))
                .addGap(16, 16, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup().addGap(16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lRol, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rol, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usuario, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nombre, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lApellidos, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(apellidos, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lClave, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clave, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(nuevo, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(guardar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(actualizar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tablaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addGap(16, 16, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JLabel lRol;
    javax.swing.JComboBox<Ui.Opcion> rol;
    javax.swing.JLabel lUsuario;
    javax.swing.JTextField usuario;
    javax.swing.JLabel lNombre;
    javax.swing.JTextField nombre;
    javax.swing.JLabel lApellidos;
    javax.swing.JTextField apellidos;
    javax.swing.JLabel lClave;
    javax.swing.JPasswordField clave;
    javax.swing.JButton nuevo;
    javax.swing.JButton guardar;
    javax.swing.JButton actualizar;
    javax.swing.JTable tabla;
    javax.swing.JScrollPane tablaScroll;
    // End of variables declaration//GEN-END:variables
}
