package ec.asistencia;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

final class Ui {
    record Opcion(String id, String nombre) {
        @Override public String toString() { return nombre; }
    }

    interface Accion { void ejecutar() throws Exception; }

    static void mostrarEstadoMatricula(JTable tabla, int columna) {
        tabla.getColumnModel().getColumn(columna).setCellRenderer(new DefaultTableCellRenderer() {
            @Override protected void setValue(Object valor) {
                super.setValue(switch (Objects.toString(valor, "")) {
                    case "PENDIENTE" -> "Pendiente de legalizar";
                    case "APROBADA" -> "Legalizada";
                    default -> valor;
                });
            }
        });
    }

    static final class Tabla extends DefaultTableModel {
        private final JTable vista;
        private final String[] titulos;
        private final int ocultas;
        private List<String[]> filas = List.of();

        Tabla(JTable vista, int ocultas, String... titulos) {
            super(titulos, 0);
            this.vista = vista;
            this.ocultas = ocultas;
            this.titulos = titulos;
            vista.setModel(this);
            vista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            vista.setRowHeight(25);
            vista.setFillsViewportHeight(true);
            vista.setAutoCreateRowSorter(true);
        }

        void cargar(List<String[]> datos) {
            vista.clearSelection();
            filas = List.copyOf(datos);
            dataVector.clear();
            for (String[] fila : filas) {
                dataVector.add(convertToVector(Arrays.copyOfRange(fila, ocultas, ocultas + getColumnCount())));
            }
            fireTableDataChanged();
        }

        String[] seleccion() {
            int fila = vista.getSelectedRow();
            return fila == -1 ? null : filas.get(vista.convertRowIndexToModel(fila));
        }

        void seleccionarId(String id) {
            for (int i = 0; i < filas.size(); i++) {
                if (filas.get(i)[0].equals(id)) {
                    int fila = vista.convertRowIndexToView(i);
                    if (fila >= 0 && vista.getSelectedRow() != fila) vista.setRowSelectionInterval(fila, fila);
                    return;
                }
            }
            vista.clearSelection();
        }

        String[] exigirSeleccion() {
            String[] fila = seleccion();
            Academico.exigir(fila != null, "Selecciona una fila de la tabla.");
            return fila;
        }

        @Override public boolean isCellEditable(int fila, int columna) { return false; }

        void exportar(Component padre, String nombre, String[] contexto) throws IOException {
            Academico.exigir(!filas.isEmpty(), "No hay filas para exportar.");
            var selector = new JFileChooser();
            selector.setSelectedFile(new java.io.File(nombre + ".csv"));
            if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) return;
            Path destino = selector.getSelectedFile().toPath();
            if (!destino.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
                destino = destino.resolveSibling(destino.getFileName() + ".csv");
            }
            if (Files.exists(destino) && JOptionPane.showConfirmDialog(padre, "¿Reemplazar el archivo existente?",
                    "Exportar CSV", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try (var archivo = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
                archivo.write('\ufeff');
                List<String> encabezado = new ArrayList<>(List.of("Materia", "Parcial"));
                encabezado.addAll(List.of(titulos));
                archivo.write(csv(encabezado));
                for (String[] fila : filas) {
                    List<String> campos = new ArrayList<>(List.of(contexto));
                    campos.addAll(Arrays.asList(fila).subList(ocultas, ocultas + titulos.length));
                    archivo.write(csv(campos));
                }
            }
        }

        private String csv(List<String> campos) {
            return campos.stream().map(valor -> {
                String texto = valor.stripLeading();
                if (!texto.isEmpty() && "=+-@".indexOf(texto.charAt(0)) >= 0) valor = "'" + valor;
                return "\"" + valor.replace("\"", "\"\"") + "\"";
            }).collect(java.util.stream.Collectors.joining(";")) + "\r\n";
        }
    }

    static void iniciar(Runnable abrir) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (ReflectiveOperationException | UnsupportedLookAndFeelException ignorado) {}
            abrir.run();
        });
    }

    static JFrame ventana(String titulo) {
        JFrame ventana = new JFrame(titulo);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return ventana;
    }

    static void mostrar(JFrame ventana, String titulo, String nota, JComponent contenido) {
        JPanel base = new JPanel(new BorderLayout());
        JPanel cabecera = new JPanel(new GridLayout(2, 1, 0, 3));
        cabecera.setBackground(new Color(24, 52, 71));
        cabecera.setBorder(BorderFactory.createEmptyBorder(13, 16, 13, 16));
        JLabel principal = new JLabel(titulo);
        principal.setForeground(Color.WHITE);
        principal.setFont(principal.getFont().deriveFont(Font.BOLD, 20));
        JLabel secundaria = new JLabel(nota);
        secundaria.setForeground(new Color(206, 228, 236));
        cabecera.add(principal);
        cabecera.add(secundaria);
        base.add(cabecera, BorderLayout.NORTH);
        base.add(contenido, BorderLayout.CENTER);
        ventana.setContentPane(base);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    static void accion(AbstractButton boton, Component padre, Accion accion) {
        boton.addActionListener(e -> seguro(padre, accion));
    }

    static void seguro(Component padre, Accion accion) {
        try { accion.ejecutar(); }
        catch (Exception e) {
            String mensaje = e instanceof java.net.ConnectException
                    ? "No se pudo conectar. Para uso local, abre Gestión y mantenla abierta."
                    : Objects.toString(e.getMessage(), e.getClass().getSimpleName());
            JOptionPane.showMessageDialog(padre, mensaje, "Asistencia estudiantil", JOptionPane.ERROR_MESSAGE);
        }
    }

    static <T> T esperar(Component padre, String mensaje, Callable<T> tarea) throws Exception {
        var dialogo = new JDialog(SwingUtilities.getWindowAncestor(padre), "Asistencia estudiantil",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JLabel aviso = new JLabel(mensaje, SwingConstants.CENTER);
        aviso.setBorder(BorderFactory.createEmptyBorder(16, 32, 16, 32));
        dialogo.add(aviso);
        dialogo.pack();
        dialogo.setLocationRelativeTo(padre);
        SwingWorker<T, Void> trabajo = new SwingWorker<>() {
            @Override protected T doInBackground() throws Exception { return tarea.call(); }
            @Override protected void done() { dialogo.dispose(); }
        };
        trabajo.execute();
        dialogo.setVisible(true);
        try { return trabajo.get(); }
        catch (ExecutionException e) {
            if (e.getCause() instanceof Exception causa) throw causa;
            throw new IOException("No se pudo completar la operación.", e.getCause());
        }
    }

    static List<String[]> consultar(Component padre, Protocolo.Cliente cliente, String accion, String... campos) throws Exception {
        return esperar(padre, "Consultando el servidor…", () -> cliente.pedir(accion, campos));
    }

    static String clave(JPasswordField campo) {
        char[] chars = campo.getPassword();
        try { return new String(chars); }
        finally { Arrays.fill(chars, '\0'); }
    }

    static void opciones(JComboBox<Opcion> caja, List<Opcion> opciones) {
        String id = idOpcional(caja);
        caja.setModel(new DefaultComboBoxModel<>(opciones.toArray(Opcion[]::new)));
        if (!id.isEmpty()) elegir(caja, id);
    }

    static void elegir(JComboBox<Opcion> caja, String id) {
        for (int i = 0; i < caja.getItemCount(); i++) {
            if (caja.getItemAt(i).id().equals(id)) {
                caja.setSelectedIndex(i);
                break;
            }
        }
    }

    static String idOpcional(JComboBox<Opcion> caja) {
        return caja.getSelectedItem() instanceof Opcion o ? o.id() : "";
    }

    static String id(JComboBox<Opcion> caja) {
        String id = idOpcional(caja);
        Academico.exigir(!id.isEmpty(), "Selecciona una opción válida.");
        return id;
    }

    static List<Opcion> materias(List<String[]> filas) {
        return filas.stream().map(f -> new Opcion(f[0], f[1] + " · " + f[2])).toList();
    }

    static List<Opcion> parciales(List<String[]> filas) {
        return filas.stream().map(f -> new Opcion(f[0], f[1])).toList();
    }

    static ImageIcon imagen(BufferedImage imagen) {
        double escala = Math.min(1, Math.min(640.0 / imagen.getWidth(), 450.0 / imagen.getHeight()));
        return new ImageIcon(imagen.getScaledInstance((int) (imagen.getWidth() * escala),
                (int) (imagen.getHeight() * escala), Image.SCALE_SMOOTH));
    }

    static void mostrarFoto(JComponent padre, String evidencia, String titulo) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(evidencia);
        BufferedImage foto = ImageIO.read(new ByteArrayInputStream(bytes));
        Academico.exigir(foto != null, "No se pudo abrir la foto.");
        JOptionPane.showMessageDialog(padre, new JLabel(imagen(foto)), titulo, JOptionPane.PLAIN_MESSAGE);
    }
}
