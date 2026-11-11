package ec.asistencia;

import com.github.sarxos.webcam.Webcam;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

final class CapturaFoto extends JPanel {
    private final Vista vista = new Vista();
    private final JTextArea estado = new JTextArea(2, 1);
    private final JButton tomar = new JButton("Tomar foto");
    private final JButton repetir = new JButton("Volver a tomar");
    private final JButton aceptar;
    private final JButton cancelar = new JButton("Cancelar");
    private final String instruccion;
    private BufferedImage seleccion;
    private BufferedImage resultado;

    private CapturaFoto(String instruccion, String confirmar) {
        super(new BorderLayout(0, 10));
        this.instruccion = instruccion;
        aceptar = new JButton(confirmar);
        estado.setText("Iniciando la vista previa de la cámara…");
        estado.setEditable(false);
        estado.setFocusable(false);
        estado.setOpaque(false);
        estado.setFont(new JLabel().getFont());
        estado.setLineWrap(true);
        estado.setWrapStyleWord(true);
        add(estado, BorderLayout.NORTH);
        add(vista, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
        for (JButton boton : List.of(tomar, repetir, aceptar, cancelar)) {
            boton.setPreferredSize(new Dimension(130, 34));
            acciones.add(boton);
        }
        add(acciones, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tomar.setEnabled(false);
        repetir.setEnabled(false);
        aceptar.setEnabled(false);
        tomar.addActionListener(e -> congelar());
        repetir.addActionListener(e -> {
            seleccion = null;
            vista.mostrar(null);
            estado.setText("Volviendo a la vista previa…");
            actualizarBotones();
        });
    }

    static BufferedImage pedir(JComponent padre, String titulo, String instruccion, String confirmar) throws Exception {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(padre), titulo, Dialog.ModalityType.APPLICATION_MODAL);
        CapturaFoto contenido = new CapturaFoto(instruccion, confirmar);
        dialogo.setContentPane(contenido);
        dialogo.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        AtomicBoolean cerrar = new AtomicBoolean();
        Runnable cerrarCamara = () -> {
            cerrar.set(true);
            contenido.estado.setText("Cerrando la cámara…");
            for (JButton boton : List.of(contenido.tomar, contenido.repetir, contenido.aceptar, contenido.cancelar)) {
                boton.setEnabled(false);
            }
        };
        contenido.aceptar.addActionListener(e -> {
            contenido.resultado = contenido.seleccion;
            cerrarCamara.run();
        });
        contenido.cancelar.addActionListener(e -> cerrarCamara.run());
        dialogo.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { cerrarCamara.run(); }
        });
        dialogo.pack();
        dialogo.setLocationRelativeTo(padre);

        SwingWorker<Void, BufferedImage> camara = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                Webcam dispositivo;
                try {
                    dispositivo = Webcam.getDefault(8, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    throw new IOException("No se detectó una cámara a tiempo.", e);
                }
                if (cerrar.get()) return null;
                if (dispositivo == null) throw new IOException("No se detectó una cámara. Conéctala y verifica el permiso de cámara.");
                var vga = new Dimension(640, 480);
                if (Arrays.asList(dispositivo.getViewSizes()).contains(vga)) dispositivo.setViewSize(vga);
                try {
                    if (!dispositivo.open()) throw new IOException("No se pudo abrir la cámara. Cierra otras aplicaciones que la estén usando.");
                    long ultimoFotograma = System.nanoTime();
                    while (!cerrar.get()) {
                        BufferedImage imagen = dispositivo.getImage();
                        if (imagen != null) {
                            publish(imagen);
                            ultimoFotograma = System.nanoTime();
                        } else if (System.nanoTime() - ultimoFotograma > TimeUnit.SECONDS.toNanos(8)) {
                            throw new IOException("La cámara no está enviando imagen.");
                        }
                        Thread.sleep(90);
                    }
                } finally {
                    dispositivo.close();
                }
                return null;
            }

            @Override protected void process(List<BufferedImage> imagenes) {
                if (!cerrar.get() && dialogo.isDisplayable()) contenido.mostrarVivo(imagenes.get(imagenes.size() - 1));
            }

            @Override protected void done() {
                dialogo.dispose();
            }
        };
        camara.execute();
        try {
            dialogo.setVisible(true);
        } finally {
            cerrar.set(true);
            dialogo.dispose();
        }
        try {
            if (camara.isDone()) camara.get();
            else Ui.esperar(padre, "Cerrando la cámara…", camara::get);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception causa) throw causa;
            throw new IOException("No se pudo completar la captura.", e.getCause());
        }
        return contenido.resultado;
    }

    private void mostrarVivo(BufferedImage imagen) {
        if (seleccion != null) return;
        boolean primerFotograma = vista.imagen == null;
        vista.mostrar(imagen);
        if (primerFotograma) {
            estado.setText("Colócate frente a la cámara y pulsa Tomar foto cuando estés listo.");
            actualizarBotones();
        }
    }

    private void congelar() {
        BufferedImage imagen = vista.imagen;
        if (imagen == null) return;
        seleccion = new BufferedImage(imagen.getWidth(), imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
        var dibujo = seleccion.createGraphics();
        try {
            dibujo.drawImage(imagen, 0, 0, null);
        } finally {
            dibujo.dispose();
        }
        vista.mostrar(seleccion);
        estado.setText(instruccion);
        actualizarBotones();
    }

    private void actualizarBotones() {
        tomar.setEnabled(seleccion == null && vista.imagen != null);
        repetir.setEnabled(seleccion != null);
        aceptar.setEnabled(seleccion != null);
    }

    private static final class Vista extends JPanel {
        private BufferedImage imagen;

        Vista() {
            setPreferredSize(new Dimension(560, 420));
            setBackground(Color.BLACK);
        }

        void mostrar(BufferedImage nueva) {
            imagen = nueva;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagen == null) return;
            double escala = Math.min((double) getWidth() / imagen.getWidth(), (double) getHeight() / imagen.getHeight());
            int ancho = (int) (imagen.getWidth() * escala);
            int alto = (int) (imagen.getHeight() * escala);
            g.drawImage(imagen, (getWidth() - ancho) / 2, (getHeight() - alto) / 2, ancho, alto, null);
        }
    }
}
