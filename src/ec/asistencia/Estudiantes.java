package ec.asistencia;

import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class Estudiantes {
    public static void main(String[] args) {
        Ui.iniciar(() -> acceso(Protocolo.direccionPredeterminada(), JFrame.EXIT_ON_CLOSE));
    }

    static void abrirDesdeGestion(String direccion) {
        acceso(direccion, JFrame.DISPOSE_ON_CLOSE);
    }

    private static void acceso(String direccion, int operacionAlCerrar) {
        JFrame ventana = Ui.ventana("Asistencia · Estudiantes");
        ventana.setDefaultCloseOperation(operacionAlCerrar);
        AccesoPanel acceso = new AccesoPanel();
        acceso.lUsuario.setText("Cédula");
        ventana.getRootPane().setDefaultButton(acceso.entrar);
        Ui.accion(acceso.entrar, acceso, () -> {
            var cliente = new Protocolo.Cliente(direccion);
            String cedula = acceso.usuario.getText();
            String clave = Ui.clave(acceso.clave);
            acceso.clave.setText("");
            var sesion = Ui.esperar(acceso, "Ingresando…", () -> cliente.ingresar("ingresarEstudiante", cedula, clave));
            new Portal(ventana, cliente, sesion[2], direccion).abrir();
        });
        Ui.accion(acceso.registro, acceso, () -> {
            var cliente = new Protocolo.Cliente(direccion);
            String cedula = registrarCuenta(acceso, cliente);
            if (cedula != null) {
                acceso.usuario.setText(cedula);
                acceso.clave.requestFocusInWindow();
            }
        });
        Ui.mostrar(ventana, "Asistencia estudiantil", "Portal de estudiantes", acceso);
    }

    private static String registrarCuenta(JComponent padre, Protocolo.Cliente cliente) throws Exception {
        RegistroPanel form = new RegistroPanel();
        while (JOptionPane.showConfirmDialog(padre, form, "Crear cuenta de estudiante",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String cedula = form.cedula.getText().strip();
            String nombre = form.nombre.getText();
            String apellidos = form.apellidos.getText();
            String clave = Ui.clave(form.clave);
            try {
                String mensaje = Ui.esperar(padre, "Creando cuenta…",
                        () -> cliente.pedir("registrar", cedula, nombre, apellidos, clave).get(0)[0]);
                JOptionPane.showMessageDialog(padre, mensaje);
                return cedula;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(padre, e.getMessage(), "Revisa los datos de tu cuenta", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    static String confirmarFoto(JComponent padre, String titulo, String instruccion, String confirmar) throws Exception {
        var foto = CapturaFoto.pedir(padre, titulo, instruccion, confirmar);
        if (foto == null) return null;
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(foto, "jpg", bytes);
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    static Map<String, List<String[]>> agruparClases(List<String[]> inscripciones, List<String[]> clases) {
        Map<String, String> idsPorEtiqueta = new HashMap<>();
        for (String[] materia : inscripciones) idsPorEtiqueta.put(materia[1] + " · " + materia[2], materia[0]);
        Map<String, List<String[]>> grupos = new HashMap<>();
        for (String[] clase : clases) {
            // El servidor anterior identifica la materia por su código y nombre.
            String id = clase.length > 8 ? clase[8] : idsPorEtiqueta.get(clase[1]);
            if (id != null) grupos.computeIfAbsent(id, clave -> new ArrayList<>()).add(clase);
        }
        return grupos;
    }

    static List<String[]> resumirMaterias(List<String[]> inscripciones, Map<String, List<String[]>> clases) {
        return inscripciones.stream()
                .filter(f -> f[5].equals("SI") || Set.of("RESERVADA", "PENDIENTE").contains(f[3]))
                .map(f -> new String[]{f[0], f[1], f[2], f[6], f[3], Integer.toString(clases.getOrDefault(f[0], List.of()).size())})
                .toList();
    }

    static final class Portal {
        private final JFrame ventana;
        private final Protocolo.Cliente cliente;
        private final String nombre;
        private final String direccion;
        private final AsistenciaPanel panel = new AsistenciaPanel();
        private final FaceIdPanel faceId = new FaceIdPanel();
        private final ReservaPanel reserva = new ReservaPanel();
        private final JTabbedPane pestañas = new JTabbedPane();
        private final Ui.Tabla horarios = new Ui.Tabla(panel.horarios, 1, "Parcial", "Clase", "Inicio", "Fin", "Estado", "Paralelo");
        private final Ui.Tabla matriculas = new Ui.Tabla(panel.matriculas, 1, "Código", "Materia", "Matrícula", "Paralelo", "Retiro efectivo");
        private final Ui.Tabla oferta = new Ui.Tabla(reserva.oferta, 1, "Parcial", "Clase", "Inicio", "Fin");
        private final Ui.Tabla misMaterias = new Ui.Tabla(reserva.horario, 1, "Código", "Materia", "Paralelo", "Matrícula", "Clases en el horario");
        private List<String[]> inscripciones = List.of();
        private List<String[]> horariosOfertados = List.of();
        private String materiaOfertada = "";
        private boolean cargando;
        private boolean rostroRegistrado;

        Portal(JFrame ventana, Protocolo.Cliente cliente, String nombre, String direccion) {
            this.ventana = ventana;
            this.cliente = cliente;
            this.nombre = nombre;
            this.direccion = direccion;
        }

        void abrir() {
            panel.configurar(this);
            reserva.configurar(this);
            faceId.configurar(this);
            Ui.mostrarEstadoMatricula(panel.matriculas, 2);
            Ui.mostrarEstadoMatricula(reserva.horario, 3);
            panel.materia.addActionListener(e -> {
                if (!cargando) Ui.seguro(panel, this::cargarHorarios);
            });
            panel.horarios.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String[] fila = horarios.seleccion();
                    panel.capturar.setEnabled(rostroRegistrado && fila != null && fila[5].equals("ABIERTA"));
                }
            });
            JButton salir = new JButton("Cerrar sesión");
            salir.setPreferredSize(new java.awt.Dimension(154, 34));
            Ui.accion(salir, panel, () -> {
                try { pedir("salir"); }
                finally {
                    ventana.dispose();
                    acceso(direccion, ventana.getDefaultCloseOperation());
                }
            });
            reserva.materia.addActionListener(e -> {
                if (!cargando) Ui.seguro(reserva, this::cargarOferta);
            });
            reserva.paralelo.addActionListener(e -> {
                if (!cargando) mostrarHorarioParalelo();
            });
            reserva.oferta.setRowSelectionAllowed(false);
            pestañas.addTab("Matrícula", reserva);
            pestañas.addTab("Asistencia", panel);
            pestañas.addTab("Face ID", faceId);
            pestañas.addChangeListener(e -> Ui.seguro(pestañas, this::actualizar));
            JPanel contenido = new JPanel(new java.awt.BorderLayout());
            contenido.add(pestañas, java.awt.BorderLayout.CENTER);
            JPanel pie = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING));
            pie.add(salir);
            contenido.add(pie, java.awt.BorderLayout.SOUTH);
            ventana.getRootPane().setDefaultButton(null);
            Ui.mostrar(ventana, "Portal estudiantil", nombre, contenido);
            Ui.seguro(pestañas, this::actualizar);
        }

        private List<String[]> pedir(String accion, String... campos) throws Exception {
            return Ui.consultar(pestañas, cliente, accion, campos);
        }

        void actualizar() throws Exception {
            actualizarFaceId();
            cargando = true;
            try {
                inscripciones = pedir("misMaterias");
                matriculas.cargar(inscripciones.stream().map(f -> new String[]{f[0], f[1], f[2], f[3], f[6], f[4]}).toList());
                Ui.opciones(panel.materia, Ui.materias(inscripciones.stream().filter(f -> f[5].equals("SI")).toList()));
                Ui.opciones(reserva.materia, Ui.materias(inscripciones.stream()
                        .filter(f -> Set.of("HABILITADA", "RESERVADA", "FALTA FOTO").contains(f[3])).toList()));
            } finally {
                cargando = false;
            }
            if (pestañas.getSelectedComponent() == reserva) {
                cargarOferta();
                String[] seleccionada = misMaterias.seleccion();
                var clasesDelAlumno = agruparClases(inscripciones, pedir("miHorario"));
                misMaterias.cargar(resumirMaterias(inscripciones, clasesDelAlumno));
                if (seleccionada != null) misMaterias.seleccionarId(seleccionada[0]);
            } else if (pestañas.getSelectedComponent() == panel) {
                cargarHorarios();
            }
        }

        void actualizarFaceId() throws Exception {
            rostroRegistrado = pedir("estadoFacial").get(0)[0].equals("REGISTRADO");
            faceId.mostrarEstado(rostroRegistrado);
        }

        private void cargarOferta() throws Exception {
            String materia = Ui.idOpcional(reserva.materia);
            String grupoAnterior = materia.equals(materiaOfertada) ? Ui.idOpcional(reserva.paralelo) : "";
            materiaOfertada = materia;
            cargando = true;
            try {
                horariosOfertados = List.of();
                Ui.opciones(reserva.paralelo, List.of());
                if (!materia.isEmpty()) {
                    horariosOfertados = pedir("ofertaMatricula", materia);
                    Map<String, String> grupos = new LinkedHashMap<>();
                    for (String[] fila : horariosOfertados) grupos.putIfAbsent(fila[1], fila[6]);
                    Ui.opciones(reserva.paralelo, grupos.entrySet().stream()
                            .map(e -> new Ui.Opcion(e.getKey(), e.getKey() + " · " + e.getValue() + " cupos libres")).toList());
                    var actual = reservaActual();
                    if (!grupoAnterior.isEmpty()) Ui.elegir(reserva.paralelo, grupoAnterior);
                    else if (actual != null) Ui.elegir(reserva.paralelo, actual[6]);
                }
            } finally {
                cargando = false;
                mostrarHorarioParalelo();
            }
        }

        private void mostrarHorarioParalelo() {
            String grupo = Ui.idOpcional(reserva.paralelo);
            oferta.cargar(horariosOfertados.stream().filter(f -> f[1].equals(grupo))
                    .map(f -> new String[]{f[0], f[2], f[3], f[4], f[5]}).toList());
            String materia = Ui.idOpcional(reserva.materia);
            reserva.aviso.setText(materia.isEmpty() ? "Administración habilita las materias nuevas."
                    : grupo.isEmpty() ? "La materia aún no tiene clases futuras para reservar."
                    : "El cupo incluye todas las clases del paralelo " + grupo + ".");
            accionesReserva();
        }

        private String[] reservaActual() {
            String materia = Ui.idOpcional(reserva.materia);
            return inscripciones.stream().filter(f -> f[0].equals(materia) && f[3].equals("RESERVADA"))
                    .findFirst().orElse(null);
        }

        private void accionesReserva() {
            String grupo = Ui.idOpcional(reserva.paralelo);
            var reservadas = inscripciones.stream().filter(f -> f[3].equals("RESERVADA")).toList();
            var actual = reservaActual();
            boolean hayCupo = horariosOfertados.stream().anyMatch(f -> f[1].equals(grupo) && Integer.parseInt(f[6]) > 0);
            reserva.guardar.setEnabled(hayCupo && (actual == null || !grupo.equals(actual[6])));
            reserva.liberar.setEnabled(actual != null);
            reserva.foto.setEnabled(!reservadas.isEmpty());
            reserva.finalizar.setEnabled(!reservadas.isEmpty() && reservadas.stream().allMatch(f -> !f[7].isEmpty()));
        }

        void cambioMatricula(String accion, String... campos) throws Exception {
            String mensaje = pedir(accion, campos).get(0)[0];
            actualizar();
            JOptionPane.showMessageDialog(reserva, mensaje);
        }

        private void cargarHorarios() throws Exception {
            String materia = Ui.idOpcional(panel.materia);
            horarios.cargar(List.of());
            panel.capturar.setEnabled(false);
            if (!rostroRegistrado) {
                panel.estado.setText("Registra tu rostro en la pestaña Face ID para habilitar la asistencia.");
            }
            if (materia.isEmpty()) {
                if (rostroRegistrado) panel.estado.setText("Finaliza la solicitud en Matrícula. Las clases aparecen aquí cuando Administración la legaliza.");
                return;
            }
            var filas = pedir("horarios", materia);
            horarios.cargar(filas);
            var abiertas = filas.stream().filter(f -> f[5].equals("ABIERTA")).toList();
            if (abiertas.size() == 1) horarios.seleccionarId(abiertas.get(0)[0]);
            panel.estado.setText(!rostroRegistrado ? "Registra tu rostro en la pestaña Face ID para habilitar la asistencia."
                    : abiertas.isEmpty() ? "No hay clases abiertas sin marcar. Usa Actualizar cuando comience tu clase."
                    : "Selecciona una clase ABIERTA y pulsa Verificar y marcar.");
            panel.capturar.setEnabled(rostroRegistrado && !abiertas.isEmpty());
        }

        void verHorario() throws Exception {
            HorarioSemanal.abrir(reserva, cliente, nombre);
        }

        void registrarRostro() throws Exception {
            if (rostroRegistrado) {
                JOptionPane.showMessageDialog(faceId, "Ya existe un perfil facial registrado.");
                return;
            }
            int consentimiento = JOptionPane.showConfirmDialog(faceId,
                    "Aceptas registrar Face ID. Se guardará una plantilla matemática cifrada y la fecha de consentimiento; la captura usada para registrarla no se conservará.",
                    "Consentimiento para Face ID", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (consentimiento != JOptionPane.YES_OPTION) return;
            BufferedImage captura = CapturaFoto.pedir(faceId, "Registrar rostro",
                    "Colócate frente a la cámara con buena iluminación y un solo rostro visible.", "Guardar perfil Face ID");
            if (captura == null) return;
            byte[] descriptor = Ui.esperar(faceId, "Procesando rostro…", () -> FaceId.descriptor(captura));
            String respuesta = pedir("guardarPerfilFacial", Base64.getEncoder().encodeToString(descriptor)).get(0)[0];
            JOptionPane.showMessageDialog(faceId, respuesta);
            actualizarFaceId();
        }

        void marcar() throws Exception {
            var fila = horarios.exigirSeleccion();
            Academico.exigir(fila[5].equals("ABIERTA"), "Selecciona una clase abierta que aún no hayas registrado.");
            BufferedImage captura = CapturaFoto.pedir(panel, "Verificar Face ID · " + fila[2],
                    "Mira de frente. La captura se guardará como evidencia de asistencia.", "Verificar y marcar");
            if (captura == null) return;
            byte[] descriptor = Ui.esperar(panel, "Procesando rostro…", () -> FaceId.descriptor(captura));
            var fotoBytes = new ByteArrayOutputStream();
            ImageIO.write(captura, "jpg", fotoBytes);
            String muestra = Base64.getEncoder().encodeToString(descriptor);
            String foto = Base64.getEncoder().encodeToString(fotoBytes.toByteArray());
            String mensaje = Ui.esperar(panel, "Verificando el rostro…", () -> cliente.pedir("marcar", fila[0], muestra, foto)).get(0)[0];
            JOptionPane.showMessageDialog(panel, mensaje);
            cargarHorarios();
        }
    }
}
