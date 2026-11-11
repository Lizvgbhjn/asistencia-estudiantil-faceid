package ec.asistencia;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;

public class MatriculasPanel extends javax.swing.JPanel {
    public MatriculasPanel() {
        initComponents();
        modelo = new Ui.Tabla(tabla, 1, "Cédula", "Estudiante", "Estado", "Foto", "Desde", "Retiro efectivo", "Paralelo");
        Ui.mostrarEstadoMatricula(tabla, 2);
        materia.addActionListener(e -> {
            if (!cargandoOpciones) Ui.seguro(this, this::cargarMatriculas);
        });
        alumno.addActionListener(e -> {
            if (!cargandoOpciones) seleccionarAlumno();
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarFila();
        });
        actualizarBotones();
    }

    private final Ui.Tabla modelo;
    private Protocolo.Cliente cliente;
    private Ui.Accion recargar;
    private boolean cargandoOpciones;

    void configurar(Protocolo.Cliente cliente, Ui.Accion recargar) {
        this.cliente = cliente;
        this.recargar = recargar;
    }

    void cargarOpciones(List<Ui.Opcion> materias, List<Ui.Opcion> estudiantes) {
        cargandoOpciones = true;
        try {
            Ui.opciones(materia, materias);
            Ui.opciones(alumno, estudiantes);
        } finally {
            cargandoOpciones = false;
        }
    }

    void cargarMatriculas() throws Exception {
        String materiaId = Ui.idOpcional(materia);
        modelo.cargar(List.of());
        if (!materiaId.isEmpty()) {
            modelo.cargar(Ui.consultar(this, cliente, "matriculas", materiaId));
        }
        seleccionarAlumno();
    }

    private void seleccionarFila() {
        String[] fila = modelo.seleccion();
        if (fila != null && !fila[0].equals(Ui.idOpcional(alumno))) {
            Ui.elegir(alumno, fila[0]);
        }
        actualizarBotones();
    }

    private void seleccionarAlumno() {
        modelo.seleccionarId(Ui.idOpcional(alumno));
        actualizarBotones();
    }

    private void actualizarBotones() {
        String[] fila = modelo.seleccion();
        String estado = fila == null ? "" : fila[3];
        boolean hayFoto = fila != null && !fila[4].isEmpty();
        boolean hayAlumnoYMateria = !Ui.idOpcional(alumno).isEmpty() && !Ui.idOpcional(materia).isEmpty();
        habilitar.setEnabled(hayAlumnoYMateria && Set.of("", "RECHAZADA").contains(estado));
        verFoto.setEnabled(hayFoto);
        legalizar.setEnabled(estado.equals("PENDIENTE") && hayFoto && !fila[7].isEmpty());
        String accion = switch (estado) {
            case "HABILITADA" -> "Quitar habilitación";
            case "RESERVADA" -> "Anular reserva";
            case "PENDIENTE", "FALTA FOTO" -> "Rechazar solicitud";
            case "APROBADA" -> "Retirar matrícula";
            default -> "";
        };
        gestionar.setEnabled(!accion.isEmpty());
        gestionar.setText(accion.isEmpty() ? "Cambiar estado" : accion);
    }

    private void cambiarMatricula(String accion, String titulo) throws Exception {
        String materiaId = Ui.id(materia);
        String alumnoId = Ui.id(alumno);
        int opcion = JOptionPane.showConfirmDialog(this, titulo + " · " + materia.getSelectedItem()
                + "\nEstudiante: " + alumno.getSelectedItem(), "Confirmar matrícula", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            Ui.consultar(this, cliente, "matricular", materiaId, alumnoId, accion);
            cargarMatriculas();
        }
    }

    private void habilitarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_habilitarActionPerformed
        Ui.seguro(this, () -> cambiarMatricula("HABILITAR", habilitar.getText()));
    }//GEN-LAST:event_habilitarActionPerformed

    private void legalizarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_legalizarActionPerformed
        Ui.seguro(this, () -> cambiarMatricula("LEGALIZAR", "Legalizar matrícula"));
    }//GEN-LAST:event_legalizarActionPerformed

    private void gestionarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_gestionarActionPerformed
        Ui.seguro(this, () -> {
            String estado = modelo.exigirSeleccion()[3];
            cambiarMatricula(estado.equals("APROBADA") ? "RETIRAR" : "RECHAZAR", gestionar.getText());
        });
    }//GEN-LAST:event_gestionarActionPerformed

    private void verFotoActionPerformed(ActionEvent evt) {//GEN-FIRST:event_verFotoActionPerformed
        Ui.seguro(this, () -> {
            String[] fila = modelo.exigirSeleccion();
            String foto = Ui.consultar(this, cliente, "fotoMatricula", Ui.id(materia), fila[0]).get(0)[0];
            Ui.mostrarFoto(this, foto, "Foto de matrícula · " + fila[2]);
        });
    }//GEN-LAST:event_verFotoActionPerformed

    private void actualizarActionPerformed(ActionEvent evt) {//GEN-FIRST:event_actualizarActionPerformed
        Ui.seguro(this, recargar);
    }//GEN-LAST:event_actualizarActionPerformed


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lMateria = new javax.swing.JLabel();
        materia = new javax.swing.JComboBox<>();
        lAlumno = new javax.swing.JLabel();
        alumno = new javax.swing.JComboBox<>();
        habilitar = new javax.swing.JButton();
        verFoto = new javax.swing.JButton();
        legalizar = new javax.swing.JButton();
        gestionar = new javax.swing.JButton();
        actualizar = new javax.swing.JButton();
        ayuda = new javax.swing.JLabel();
        tabla = new javax.swing.JTable();
        tablaScroll = new javax.swing.JScrollPane();

        lMateria.setText("Materia");
        lAlumno.setText("Estudiante");
        habilitar.setText("Habilitar materia");
        habilitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                habilitarActionPerformed(evt);
            }
        });
        verFoto.setText("Ver foto");
        verFoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verFotoActionPerformed(evt);
            }
        });
        legalizar.setText("Legalizar");
        legalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                legalizarActionPerformed(evt);
            }
        });
        gestionar.setText("Cambiar estado");
        gestionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gestionarActionPerformed(evt);
            }
        });
        actualizar.setText("Actualizar");
        actualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizarActionPerformed(evt);
            }
        });
        ayuda.setText("Revisa la foto de la solicitud y pulsa Legalizar después de que el estudiante finalice la matrícula.");
        tablaScroll.setViewportView(tabla);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup().addGap(16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lMateria, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(materia, javax.swing.GroupLayout.DEFAULT_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(lAlumno, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(alumno, javax.swing.GroupLayout.DEFAULT_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(habilitar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(verFoto, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(legalizar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(gestionar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8)
                        .addComponent(actualizar, javax.swing.GroupLayout.DEFAULT_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ayuda, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tablaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 820, Short.MAX_VALUE)))
                .addGap(16, 16, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup().addGap(16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lMateria, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(materia, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lAlumno, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(alumno, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(habilitar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(verFoto, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(legalizar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gestionar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(actualizar, javax.swing.GroupLayout.DEFAULT_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(ayuda, javax.swing.GroupLayout.DEFAULT_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tablaScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addGap(16, 16, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JLabel lMateria;
    javax.swing.JComboBox<Ui.Opcion> materia;
    javax.swing.JLabel lAlumno;
    javax.swing.JComboBox<Ui.Opcion> alumno;
    javax.swing.JButton habilitar;
    javax.swing.JButton verFoto;
    javax.swing.JButton legalizar;
    javax.swing.JButton gestionar;
    javax.swing.JButton actualizar;
    javax.swing.JLabel ayuda;
    javax.swing.JTable tabla;
    javax.swing.JScrollPane tablaScroll;
    // End of variables declaration//GEN-END:variables
}
