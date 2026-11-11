package ec.asistencia;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class MateriasPanel extends javax.swing.JPanel {
    public MateriasPanel() {
        initComponents();
        modelo = new Ui.Tabla(tabla, 1, "Código", "Materia", "Docente", "Nivel curricular", "Período académico");
        codigo.setEditable(false);
        codigo.setToolTipText("El sistema asigna el código al guardar la materia.");
        List<Ui.Opcion> niveles = new ArrayList<>();
        niveles.add(new Ui.Opcion("", "Sin asignar"));
        for (int numero = 1; numero <= 16; numero++) {
            niveles.add(new Ui.Opcion(Integer.toString(numero), numero + ".º semestre"));
        }
        Ui.opciones(nivel, niveles);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarMateria();
        });
    }

    private final Ui.Tabla modelo;
    private Protocolo.Cliente cliente;
    private Ui.Accion recargar;
    private String materiaId = "";

    void configurar(Protocolo.Cliente cliente, Ui.Accion recargar) {
        this.cliente = cliente;
        this.recargar = recargar;
    }

    void cargarMaterias(List<String[]> filas) {
        List<String[]> datos = new ArrayList<>();
        for (String[] fila : filas) {
            String nivel = fila.length > 5 && !fila[5].isEmpty() ? fila[5] : "Sin asignar";
            String periodoNombre = fila.length > 7 ? fila[7] : "Sin período";
            datos.add(new String[]{fila[0], fila[1], fila[2], fila[3], nivel, periodoNombre, fila[4], fila.length > 6 ? fila[6] : ""});
        }
        modelo.cargar(datos);
    }

    void cargarPeriodos(List<String[]> filas) {
        Ui.opciones(periodo, filas.stream().filter(f -> !f[0].equals("0"))
                .map(f -> new Ui.Opcion(f[0], f[1] + " · " + f[2] + " a " + f[3])).toList());
        if (periodo.getSelectedIndex() < 0 && periodo.getItemCount() > 0) periodo.setSelectedIndex(0);
    }

    private void mostrarMateria() {
        String[] fila = modelo.seleccion();
        if (fila == null) return;
        materiaId = fila[0];
        codigo.setText(fila[1]);
        nombre.setText(fila[2]);
        Ui.elegir(docente, fila[6]);
        Ui.elegir(nivel, fila[4]);
        if (fila[7].isEmpty()) periodo.setSelectedIndex(-1);
        else Ui.elegir(periodo, fila[7]);
        guardar.setText("Guardar cambios");
    }

    private void limpiarMateria() {
        tabla.clearSelection();
        materiaId = "";
        codigo.setText("");
        nombre.setText("");
        Ui.elegir(nivel, "");
        Ui.elegir(periodo, "");
        guardar.setText("Crear materia");
    }

    void mostrarCodigoSiguiente() throws Exception {
        if (materiaId.isEmpty()) {
            codigo.setText(Ui.consultar(this, cliente, "siguienteCodigoMateria").get(0)[0]);
        }
    }

    private void guardarMateria() throws Exception {
        Ui.consultar(this, cliente, "guardarMateria", materiaId, "", nombre.getText(), Ui.id(docente),
                Ui.idOpcional(nivel), Ui.id(periodo));
        limpiarMateria();
        recargar.ejecutar();
    }

    private void nuevoActionPerformed(ActionEvent evt) {//GEN-FIRST:event_nuevoActionPerformed
        limpiarMateria();
        Ui.seguro(this, this::mostrarCodigoSiguiente);
    }//GEN-LAST:event_nuevoActionPerformed

    private void guardarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_guardarActionPerformed
        Ui.seguro(this, this::guardarMateria);
    }//GEN-LAST:event_guardarActionPerformed

    private void actualizarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_actualizarActionPerformed
        limpiarMateria();
        Ui.seguro(this, recargar);
    }//GEN-LAST:event_actualizarActionPerformed


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lCodigo = new javax.swing.JLabel();
        codigo = new javax.swing.JTextField();
        lNombre = new javax.swing.JLabel();
        nombre = new javax.swing.JTextField();
        lDocente = new javax.swing.JLabel();
        docente = new javax.swing.JComboBox<>();
        lNivel = new javax.swing.JLabel();
        nivel = new javax.swing.JComboBox<>();
        lPeriodo = new javax.swing.JLabel();
        periodo = new javax.swing.JComboBox<>();
        nuevo = new javax.swing.JButton();
        guardar = new javax.swing.JButton();
        actualizar = new javax.swing.JButton();
        tabla = new javax.swing.JTable();
        tablaScroll = new javax.swing.JScrollPane();

        lCodigo.setText("Código automático");
        lNombre.setText("Materia");
        lDocente.setText("Docente asignado");
        lNivel.setText("Semestre de la materia");
        lPeriodo.setText("Período académico de la oferta");
        nuevo.setText("Nueva materia");
        nuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nuevoActionPerformed(evt);
            }
        });
        guardar.setText("Crear materia");
        actualizar.setText("Actualizar");
        guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarActionPerformed(evt);
            }
        });
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
                        .addComponent(lCodigo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(codigo, javax.swing.GroupLayout.DEFAULT_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(lNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(nombre, javax.swing.GroupLayout.DEFAULT_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lDocente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(docente, javax.swing.GroupLayout.DEFAULT_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(lNivel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(nivel, javax.swing.GroupLayout.DEFAULT_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lPeriodo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(periodo, javax.swing.GroupLayout.DEFAULT_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(lCodigo, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(codigo, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nombre, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lDocente, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(docente, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lNivel, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nivel, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lPeriodo, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(periodo, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
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
    javax.swing.JLabel lCodigo;
    javax.swing.JTextField codigo;
    javax.swing.JLabel lNombre;
    javax.swing.JTextField nombre;
    javax.swing.JLabel lDocente;
    javax.swing.JComboBox<Ui.Opcion> docente;
    javax.swing.JLabel lNivel;
    javax.swing.JComboBox<Ui.Opcion> nivel;
    javax.swing.JLabel lPeriodo;
    javax.swing.JComboBox<Ui.Opcion> periodo;
    javax.swing.JButton nuevo;
    javax.swing.JButton guardar;
    javax.swing.JButton actualizar;
    javax.swing.JTable tabla;
    javax.swing.JScrollPane tablaScroll;
    // End of variables declaration//GEN-END:variables
}
