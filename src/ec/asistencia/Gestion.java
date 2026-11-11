package ec.asistencia;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import javax.swing.*;

public final class Gestion {
    private static boolean servidorLocalIniciado;

    public static void main(String[] args) {
        if (args.length == 0) {
            Ui.iniciar(Gestion::acceso);
            return;
        }
        try {
            if (args[0].equals("--servidor") && args.length <= 2) {
                new Servidor(Path.of(args.length == 2 ? args[1] : "servidor.properties")).arrancar();
            } else if (args[0].equals("--crear-admin") && (args.length == 3 || args.length == 4)) {
                var servidor = new Servidor(Path.of(args.length == 4 ? args[3] : "servidor.properties"));
                Console consola = System.console();
                String clave = System.getenv("ASISTENCIA_ADMIN_CLAVE");
                if (consola != null && clave == null) {
                    char[] chars = consola.readPassword("Clave del administrador: ");
                    if (chars != null) {
                        clave = new String(chars);
                        Arrays.fill(chars, '\0');
                    }
                }
                Academico.exigir(clave != null, "Abre una terminal para ingresar la clave del administrador.");
                servidor.crearAdministrador(args[1], args[2], clave);
                System.out.println("Administrador creado.");
            } else {
                throw new IllegalArgumentException("Uso: --servidor [config] o --crear-admin usuario nombre [config]");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void acceso() {
        acceso(Protocolo.direccionPredeterminada(), false);
    }

    private static void acceso(String direccion, boolean soloDocentes) {
        JFrame ventana = Ui.ventana(soloDocentes ? "Asistencia · Docentes" : "Asistencia · Gestión");
        AccesoPanel acceso = new AccesoPanel();
        acceso.registro.setVisible(false);
        if (soloDocentes) ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        boolean iniciarLocal = !soloDocentes && direccion.replaceFirst("/+$", "").equals(Protocolo.direccionLocal());
        ventana.getRootPane().setDefaultButton(acceso.entrar);
        Ui.accion(acceso.entrar, acceso, () -> {
            if (iniciarLocal && !iniciarServidorLocal(acceso)) return;
            var cliente = new Protocolo.Cliente(direccion);
            String usuario = acceso.usuario.getText();
            String clave = Ui.clave(acceso.clave);
            acceso.clave.setText("");
            String accion = soloDocentes ? "ingresarDocente" : "ingresarGestion";
            var sesion = Ui.esperar(acceso, "Ingresando…", () -> cliente.ingresar(accion, usuario, clave));
            Runnable volver = () -> acceso(direccion, soloDocentes);
            new Escritorio(ventana, cliente, sesion[1].equals("ADMIN"), sesion[2], direccion, volver).abrir();
        });
        Ui.mostrar(ventana, soloDocentes ? "Portal docente" : "Asistencia estudiantil",
                soloDocentes ? "Ingresa con tu cuenta de docente" : "Administración y docentes", acceso);
        if (iniciarLocal) Ui.seguro(acceso, () -> iniciarServidorLocal(acceso));
    }

    private static boolean iniciarServidorLocal(AccesoPanel acceso) throws Exception {
        Path configuracion = Path.of("servidor.properties");
        if (servidorLocalIniciado || !Files.isRegularFile(configuracion)) return true;

        var servidor = Ui.esperar(acceso, "Conectando con MariaDB…", () -> new Servidor(configuracion));
        boolean hayAdministrador = Ui.esperar(acceso, "Comprobando el acceso a MariaDB…", servidor::hayAdministrador);
        if (!hayAdministrador) {
            var clave = new JPasswordField(18);
            var formulario = new JPanel(new java.awt.GridLayout(0, 1, 0, 6));
            formulario.add(new JLabel("Elige la clave de admin (mínimo 8 caracteres)."));
            formulario.add(clave);
            if (JOptionPane.showConfirmDialog(acceso, formulario, "Crear administrador",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return false;
            String valor = Ui.clave(clave);
            clave.setText("");
            Ui.esperar(acceso, "Creando la cuenta de administración…", () -> {
                servidor.crearAdministrador("admin", "Administrador", valor);
                return null;
            });
            acceso.usuario.setText("admin");
            acceso.clave.requestFocusInWindow();
        }
        Ui.esperar(acceso, "Iniciando el servicio de asistencia…", () -> {
            servidor.arrancar();
            return null;
        });
        servidorLocalIniciado = true;
        return true;
    }

    private static final class Escritorio {
        private final JFrame ventana;
        private final Protocolo.Cliente cliente;
        private final boolean admin;
        private final String nombre;
        private final String servidor;
        private final Runnable volverAlAcceso;
        private final JTabbedPane pestañas = new JTabbedPane();
        private final CuentasPanel cuentas = new CuentasPanel();
        private final MateriasPanel materias = new MateriasPanel();
        private final MatriculasPanel matriculas = new MatriculasPanel();
        private final ClasesPanel clases = new ClasesPanel();
        private final ReportesPanel reportes = new ReportesPanel();
        private final SemestresPanel semestres = new SemestresPanel();
        private final Ui.Tabla tClases = new Ui.Tabla(clases.tabla, 1, "Tema", "Inicio", "Fin", "Tolerancia (min)", "Estado", "Paralelo");
        private String claseId = "";
        private boolean cargando;

        Escritorio(JFrame ventana, Protocolo.Cliente cliente, boolean admin, String nombre,
                String servidor, Runnable volverAlAcceso) {
            this.ventana = ventana;
            this.cliente = cliente;
            this.admin = admin;
            this.nombre = nombre;
            this.servidor = servidor;
            this.volverAlAcceso = volverAlAcceso;
        }

        void abrir() {
            if (admin) {
                cuentas.configurar(cliente, this::recargar);
                materias.configurar(cliente, this::recargar);
                matriculas.configurar(cliente, this::recargar);
                semestres.configurar(cliente, this::recargar);
                configurarClases();
                pestañas.addTab("Cuentas", cuentas);
                pestañas.addTab("Materias", materias);
                pestañas.addTab("Matrículas", matriculas);
                pestañas.addTab("Clases y parciales", clases);
                pestañas.addTab("Períodos", semestres);
            }
            configurarReportes();
            pestañas.addTab("Reportes", reportes);
            var contenedor = new JPanel(new java.awt.BorderLayout());
            contenedor.add(pestañas, java.awt.BorderLayout.CENTER);
            var pie = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 16, 8));
            var tamañoBoton = new java.awt.Dimension(154, 34);
            if (admin) {
                var portalDocente = new JButton("Portal docente");
                portalDocente.setPreferredSize(tamañoBoton);
                Ui.accion(portalDocente, pestañas, () -> acceso(servidor, true));
                pie.add(portalDocente);
                var portalEstudiantil = new JButton("Portal estudiantil");
                portalEstudiantil.setPreferredSize(tamañoBoton);
                Ui.accion(portalEstudiantil, pestañas, () -> Estudiantes.abrirDesdeGestion(servidor));
                pie.add(portalEstudiantil);
            }
            var salir = new JButton("Cerrar sesión");
            salir.setPreferredSize(tamañoBoton);
            pie.add(salir);
            contenedor.add(pie, java.awt.BorderLayout.SOUTH);
            Ui.accion(salir, pestañas, () -> {
                try { pedir("salir"); }
                finally {
                    ventana.dispose();
                    volverAlAcceso.run();
                }
            });
            ventana.getRootPane().setDefaultButton(null);
            Ui.mostrar(ventana, admin ? "Administración" : "Portal docente", nombre, contenedor);
            Ui.seguro(pestañas, this::recargar);
            pestañas.addChangeListener(e -> Ui.seguro(pestañas, this::cargarPestaña));
        }

        private List<String[]> pedir(String accion, String... campos) throws Exception {
            return Ui.consultar(pestañas, cliente, accion, campos);
        }

        private void recargar() throws Exception {
            cargando = true;
            try {
                List<Ui.Opcion> estudiantes = List.of();
                if (admin) {
                    var filas = pedir("cuentas");
                    cuentas.cargarCuentas(filas);
                    Ui.opciones(materias.docente, opcionesCuenta(filas, "DOCENTE"));
                    estudiantes = opcionesCuenta(filas, "ESTUDIANTE");
                }
                var filas = pedir("materias");
                var opciones = Ui.materias(filas);
                if (admin) {
                    materias.cargarMaterias(filas);
                    materias.cargarPeriodos(pedir("semestres"));
                    matriculas.cargarOpciones(opciones, estudiantes);
                }
                Ui.opciones(clases.materia, opciones);
                Ui.opciones(reportes.materia, opciones);
            } finally {
                cargando = false;
            }
            cargarPestaña();
        }

        private List<Ui.Opcion> opcionesCuenta(List<String[]> filas, String tipo) {
            return filas.stream().filter(f -> f[1].equals(tipo))
                    .map(f -> new Ui.Opcion(f[0], f[3] + " · " + f[2])).toList();
        }

        private void cargarPestaña() throws Exception {
            var activa = pestañas.getSelectedComponent();
            if (activa == materias) materias.mostrarCodigoSiguiente();
            if (activa == matriculas) matriculas.cargarMatriculas();
            if (activa == clases) cargarParciales(clases.materia, clases.parcial, this::cargarClases);
            if (activa == semestres) semestres.cargar();
            if (activa == reportes) cargarParciales(reportes.materia, reportes.parcial, reportes::cargarReporte);
        }

        private void elegirTabla(JTable tabla, Ui.Accion accion) {
            tabla.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) Ui.seguro(tabla, accion);
            });
        }

        private void cambiarCombo(JComboBox<?> caja, Ui.Accion accion) {
            caja.addActionListener(e -> {
                if (!cargando) Ui.seguro(caja, accion);
            });
        }

        private void cargarParciales(JComboBox<Ui.Opcion> materia, JComboBox<Ui.Opcion> parcial, Ui.Accion siguiente) throws Exception {
            cargando = true;
            try {
                String id = Ui.idOpcional(materia);
                Ui.opciones(parcial, id.isEmpty() ? List.of() : Ui.parciales(pedir("parciales", id)));
            } catch (Exception e) {
                Ui.opciones(parcial, List.of());
                siguiente.ejecutar();
                throw e;
            } finally {
                cargando = false;
            }
            siguiente.ejecutar();
        }

        private void configurarClases() {
            Ui.opciones(clases.paralelo, List.of(new Ui.Opcion("A", "A"), new Ui.Opcion("B", "B")));
            cambiarCombo(clases.materia, () -> cargarParciales(clases.materia, clases.parcial, this::cargarClases));
            cambiarCombo(clases.parcial, this::cargarClases);
            Ui.accion(clases.configurarParciales, clases, this::configurarParcial);
            Ui.accion(clases.cupos, clases, this::configurarCupos);
            Calendario.conectar(clases.calInicio, clases.inicio, "Seleccionar fecha y hora de inicio", true, this::inicioSugerido);
            Calendario.conectar(clases.calFin, clases.fin, "Seleccionar fecha y hora de fin", true, () -> inicioSugerido().plusHours(1));
            clases.tolerancia.setToolTipText("Margen para marcar a tiempo desde el inicio. Si lo dejas vacío, se usan 5 minutos.");
            elegirTabla(clases.tabla, () -> {
                var fila = tClases.seleccion();
                if (fila == null) return;
                claseId = fila[0];
                clases.tema.setText(fila[1]);
                clases.inicio.setText(fila[2]);
                clases.fin.setText(fila[3]);
                clases.tolerancia.setText(fila[4]);
                Ui.elegir(clases.paralelo, fila[6]);
                clases.paralelo.setEnabled(false);
            });
            Ui.accion(clases.nuevo, clases, this::limpiarClase);
            Ui.accion(clases.guardar, clases, () -> {
                pedir("guardarClase", claseId, Ui.id(clases.parcial), clases.tema.getText(), clases.inicio.getText().strip(),
                        clases.fin.getText().strip(), clases.tolerancia.getText().strip(), Ui.id(clases.paralelo));
                cargarClases();
            });
            Ui.accion(clases.cancelar, clases, () -> {
                var fila = tClases.exigirSeleccion();
                if (JOptionPane.showConfirmDialog(clases, "¿Cancelar " + fila[1] + "?", "Cancelar clase",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    pedir("cancelar", fila[0]);
                    cargarClases();
                }
            });
            Ui.accion(clases.actualizar, clases, this::recargar);
        }

        private void configurarCupos() throws Exception {
            String materia = Ui.id(clases.materia);
            var cupos = pedir("cupos", materia);
            JSpinner a = new JSpinner(new SpinnerNumberModel(Integer.parseInt(cupos.get(0)[1]), 0, 500, 1));
            JSpinner b = new JSpinner(new SpinnerNumberModel(Integer.parseInt(cupos.get(1)[1]), 0, 500, 1));
            JPanel formulario = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
            formulario.add(new JLabel("Paralelo A · ocupados: " + cupos.get(0)[2]));
            formulario.add(a);
            formulario.add(new JLabel("Paralelo B · ocupados: " + cupos.get(1)[2]));
            formulario.add(b);
            if (JOptionPane.showConfirmDialog(clases, formulario, "Cupos de " + clases.materia.getSelectedItem(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                a.commitEdit();
                b.commitEdit();
                pedir("configurarCupos", materia, a.getValue().toString(), b.getValue().toString());
            }
        }

        private void configurarParcial() throws Exception {
            String materia = Ui.id(clases.materia);
            var formulario = new ParcialPanel();
            Map<String, String[]> parciales = new LinkedHashMap<>();
            for (String nombre : Academico.PARCIALES_HABITUALES) {
                parciales.put(nombre.toLowerCase(Locale.ROOT), new String[]{"", nombre, "", ""});
            }
            for (String[] fila : pedir("parciales", materia)) {
                parciales.put(fila[1].toLowerCase(Locale.ROOT), fila);
            }
            Ui.opciones(formulario.nombre, parciales.entrySet().stream()
                    .map(e -> new Ui.Opcion(e.getKey(), e.getValue()[1])).toList());
            String seleccionado = Ui.idOpcional(clases.parcial);
            parciales.forEach((clave, fila) -> {
                if (!seleccionado.isEmpty() && seleccionado.equals(fila[0])) Ui.elegir(formulario.nombre, clave);
            });
            Runnable cargarFechas = () -> {
                var fila = parciales.get(Ui.id(formulario.nombre));
                formulario.desde.setText(fila[2]);
                formulario.hasta.setText(fila[3]);
            };
            formulario.nombre.addActionListener(e -> cargarFechas.run());
            cargarFechas.run();
            Calendario.conectar(formulario.calDesde, formulario.desde, "Seleccionar inicio del parcial", false, LocalDateTime::now);
            Calendario.conectar(formulario.calHasta, formulario.hasta, "Seleccionar fin del parcial", false, LocalDateTime::now);
            while (JOptionPane.showConfirmDialog(clases, formulario, "Configurar parcial", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                var fila = parciales.get(Ui.id(formulario.nombre));
                try {
                    pedir("guardarParcial", materia, fila[1], formulario.desde.getText().strip(), formulario.hasta.getText().strip());
                    cargarParciales(clases.materia, clases.parcial, this::cargarClases);
                    return;
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(clases, e.getMessage(), "Revisa las fechas del parcial", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private LocalDateTime inicioSugerido() {
            try {
                return LocalDateTime.parse(clases.inicio.getText().strip(), Academico.HORARIO);
            } catch (DateTimeParseException e) {
                var siguiente = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);
                if (siguiente.getHour() < 7) return siguiente.withHour(7);
                if (siguiente.getHour() == 13) return siguiente.withHour(14);
                if (siguiente.getHour() >= 20) return siguiente.plusDays(1).withHour(7);
                return siguiente;
            }
        }

        private void limpiarClase() {
            clases.tabla.clearSelection();
            claseId = "";
            clases.tema.setText("");
            clases.inicio.setText("");
            clases.fin.setText("");
            clases.tolerancia.setText(Integer.toString(Academico.TOLERANCIA_PREDETERMINADA));
            clases.paralelo.setEnabled(true);
        }

        private void cargarClases() throws Exception {
            limpiarClase();
            String parcial = Ui.idOpcional(clases.parcial);
            tClases.cargar(List.of());
            if (!parcial.isEmpty()) tClases.cargar(pedir("clases", parcial));
        }

        private void configurarReportes() {
            reportes.configurar(cliente, this::recargar, !admin);
            cambiarCombo(reportes.materia, () -> cargarParciales(reportes.materia, reportes.parcial, reportes::cargarReporte));
            cambiarCombo(reportes.parcial, reportes::cargarReporte);
        }
    }
}
