
package ec.asistencia;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.FaceRecognizerSF;

public class RegistroFacial {

    private static final String YUNET =
            "modelos/face_detection_yunet_2023mar.onnx";

    private static final String SFACE =
            "modelos/face_recognition_sface_2021dec.onnx";

    public static void main(String[] args) {

        // Usar solamente una cuenta de prueba autorizada.
        final long estudianteId = 3;
        final String rutaFoto = "foto1.jpg";
        final String version = "SFACE_2021DEC";

        Mat descriptor = null;

        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            System.out.println(
                    "===== REGISTRO FACIAL DE PRUEBA ====="
            );

            Properties propiedades = new Properties();

            try (InputStream entrada = Files.newInputStream(
                    Path.of("servidor.properties"))) {

                propiedades.load(entrada);
            }

            try (Base bd = new Base(propiedades)) {

                String[] cuenta = bd.uno(
                    "SELECT rol FROM cuenta WHERE id = ?",
                    estudianteId
                );

                if (cuenta == null
                        || !"ESTUDIANTE".equals(cuenta[0])) {
                    throw new IllegalStateException(
                        "La cuenta no corresponde a un estudiante"
                    );
                }

                String[] existente = bd.uno(
                    "SELECT estudiante_id FROM perfil_facial "
                    + "WHERE estudiante_id = ?",
                    estudianteId
                );

                if (existente != null) {
                    throw new IllegalStateException(
                        "El estudiante ya tiene un perfil facial. "
                        + "No se modificara."
                    );
                }

                FaceDetectorYN detector =
                        FaceDetectorYN.create(
                                YUNET,
                                "",
                                new Size(320, 320),
                                0.9f,
                                0.3f,
                                5000
                        );

                FaceRecognizerSF reconocedor =
                        FaceRecognizerSF.create(SFACE, "");

                descriptor =
                        ReconocimientoFacial.obtenerDescriptor(
                                rutaFoto,
                                detector,
                                reconocedor
                        );

                byte[] datos =
                        DescriptorFacial.convertirABytes(
                                descriptor
                        );

                if (datos.length != 512) {
                    throw new IllegalStateException(
                        "Descriptor facial invalido"
                    );
                }

                System.out.println(
                        "Descriptor generado: " + datos.length
                        + " bytes"
                );

                // No conservar el perfil al finalizar la prueba
                bd.iniciar();

                try {
                    bd.guardarPerfilFacial(
                            estudianteId,
                            datos,
                            version
                    );

                    String[] almacenado = bd.uno(
                        "SELECT OCTET_LENGTH(descriptor) "
                        + "FROM perfil_facial "
                        + "WHERE estudiante_id = ?",
                        estudianteId
                    );

                    byte[] recuperado =
                            bd.obtenerPerfilFacial(estudianteId);

                    boolean iguales =
                            Arrays.equals(datos, recuperado);

                    System.out.println(
                            "Bytes cifrados en MariaDB: "
                            + almacenado[0]
                    );

                    System.out.println(
                            "Descriptor recuperado correctamente: "
                            + iguales
                    );

                    if (!iguales) {
                        throw new IllegalStateException(
                            "Los descriptores no coinciden"
                        );
                    }

                } finally {
                    bd.revertir();

                    System.out.println(
                        "Transaccion revertida: "
                        + "perfil de prueba no conservado"
                    );
                }
            }

            System.out.println(
                    "===== FIN DEL REGISTRO DE PRUEBA ====="
            );

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (descriptor != null) {
                descriptor.release();
            }
        }
    }
}
