package ec.asistencia;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.*;

final class FaceIdPanel extends JPanel {
    final JButton registrar = new JButton("Registrar rostro");
    private final JButton actualizar = new JButton("Actualizar estado");
    private final JLabel estado = new JLabel("Consulta el estado de tu perfil facial.");
    private Estudiantes.Portal portal;

    FaceIdPanel() {
        super(new BorderLayout(12, 12));
        var contenido = new JPanel(new java.awt.GridLayout(0, 1, 8, 8));
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        contenido.add(new JLabel("Face ID se compara al marcar asistencia en una clase abierta."));
        contenido.add(new JLabel("Se guarda una plantilla facial cifrada; las capturas de registro no se conservan."));
        contenido.add(estado);
        var acciones = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        acciones.add(registrar);
        acciones.add(actualizar);
        contenido.add(acciones);
        add(contenido, BorderLayout.NORTH);
        registrar.addActionListener(e -> Ui.seguro(this, portal::registrarRostro));
        actualizar.addActionListener(e -> Ui.seguro(this, portal::actualizarFaceId));
    }

    void configurar(Estudiantes.Portal portal) { this.portal = portal; }

    void mostrarEstado(boolean registrado) {
        estado.setText(registrado ? "Estado facial: Registrado" : "Estado facial: Sin registrar");
        registrar.setEnabled(!registrado);
    }
}
