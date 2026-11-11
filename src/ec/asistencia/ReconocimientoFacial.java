
package ec.asistencia;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.FaceRecognizerSF;

public class ReconocimientoFacial {

    // ==========================================
    // RUTAS DE LOS MODELOS
    // ==========================================

    private static final String YUNET =
            "modelos/face_detection_yunet_2023mar.onnx";

    private static final String SFACE =
            "modelos/face_recognition_sface_2021dec.onnx";

    // ==========================================
    // EXTRAER DESCRIPTOR FACIAL
    // ==========================================

    public static Mat obtenerDescriptor(
            String rutaFoto,
            FaceDetectorYN detector,
            FaceRecognizerSF reconocedor) {

        Mat imagen = Imgcodecs.imread(rutaFoto);

        if (imagen.empty()) {
            imagen.release();

            throw new IllegalArgumentException(
                    "No se pudo abrir: " + rutaFoto
            );
        }

        Mat rostros = new Mat();
        Mat rostroAlineado = new Mat();
        Mat descriptor = new Mat();

        try {

            // Ajustar el detector al tamaño de imagen
            detector.setInputSize(
                    new Size(imagen.cols(), imagen.rows())
            );

            // Detectar rostros
            detector.detect(imagen, rostros);

            System.out.println(
                    rutaFoto + " - Rostros detectados: "
                    + rostros.rows()
            );

            // Exigir exactamente un rostro
            if (rostros.rows() != 1) {
                throw new IllegalStateException(
                        "Se requiere exactamente un rostro. "
                        + "Detectados: " + rostros.rows()
                );
            }

            Mat rostroDetectado = rostros.row(0);

            try {

                // Alinear el rostro detectado
                reconocedor.alignCrop(
                        imagen,
                        rostroDetectado,
                        rostroAlineado
                );

                // Extraer características faciales
                reconocedor.feature(
                        rostroAlineado,
                        descriptor
                );

            } finally {
                rostroDetectado.release();
            }

            if (descriptor.empty()) {
                throw new IllegalStateException(
                        "SFace no genero el descriptor"
                );
            }

            // Devolver una copia independiente
            return descriptor.clone();

        } finally {

            // Liberar recursos temporales
            descriptor.release();
            rostroAlineado.release();
            rostros.release();
            imagen.release();
        }
    }

    // ==========================================
    // PROGRAMA PRINCIPAL DE PRUEBA
    // ==========================================

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat descriptor1 = null;
        Mat descriptor2 = null;

        try {

            String foto1 = "foto1.jpg";
            String foto2 = "foto2.jpg";

            // ==================================
            // VERIFICAR ARCHIVOS
            // ==================================

            System.out.println(
                    "===== VERIFICAR ARCHIVOS ====="
            );

            System.out.println(
                    "Ruta foto 1: "
                    + new File(foto1).getCanonicalPath()
            );

            System.out.println(
                    "Ruta foto 2: "
                    + new File(foto2).getCanonicalPath()
            );

            byte[] bytes1 = Files.readAllBytes(
                    Path.of(foto1)
            );

            byte[] bytes2 = Files.readAllBytes(
                    Path.of(foto2)
            );

            System.out.println(
                    "Tamaño foto 1: " + bytes1.length
            );

            System.out.println(
                    "Tamaño foto 2: " + bytes2.length
            );

            System.out.println(
                    "¿Archivos idénticos?: "
                    + Arrays.equals(bytes1, bytes2)
            );

            // ==================================
            // CARGAR MODELOS FACIALES
            // ==================================

            System.out.println();
            System.out.println(
                    "===== CARGAR MODELOS ====="
            );

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
                    FaceRecognizerSF.create(
                            SFACE,
                            ""
                    );

            System.out.println(
                    "OpenCV: " + Core.VERSION
            );

            // ==================================
            // EXTRAER DESCRIPTORES
            // ==================================

            System.out.println();
            System.out.println(
                    "===== EXTRAER ROSTROS ====="
            );

            descriptor1 = obtenerDescriptor(
                    foto1,
                    detector,
                    reconocedor
            );

            descriptor2 = obtenerDescriptor(
                    foto2,
                    detector,
                    reconocedor
            );

            // ==================================
            // CONVERTIR MAT A BYTE[]
            // ==================================

            System.out.println();
            System.out.println(
                    "===== PRUEBA DE CONVERSION ====="
            );

            byte[] datos =
                    DescriptorFacial.convertirABytes(
                            descriptor1
                    );

            System.out.println(
                    "Tamaño del descriptor en bytes: "
                    + datos.length
            );

            // Recuperar descriptor desde byte[]
            Mat descriptorRecuperado =
                    DescriptorFacial.convertirAMat(
                            datos
                    );

            try {

                double diferenciaConversion =
                        Core.norm(
                                descriptor1,
                                descriptorRecuperado,
                                Core.NORM_L2
                        );

                System.out.println(
                        "Diferencia despues de convertir: "
                        + diferenciaConversion
                );

                double similitudRecuperada =
                        reconocedor.match(
                                descriptor1,
                                descriptorRecuperado,
                                FaceRecognizerSF.FR_COSINE
                        );

                System.out.printf(
                        "Similitud con descriptor recuperado: %.4f%n",
                        similitudRecuperada
                );

            } finally {
                descriptorRecuperado.release();
            }

            // ==================================
            // COMPARAR LAS DOS FOTOGRAFIAS
            // ==================================

            System.out.println();
            System.out.println(
                    "===== COMPARAR DESCRIPTORES ====="
            );

            double diferencia =
                    Core.norm(
                            descriptor1,
                            descriptor2,
                            Core.NORM_L2
                    );

            System.out.println(
                    "Diferencia entre descriptores: "
                    + diferencia
            );

            double similitud =
                    reconocedor.match(
                            descriptor1,
                            descriptor2,
                            FaceRecognizerSF.FR_COSINE
                    );

            System.out.printf(
                    "Similitud facial: %.4f%n",
                    similitud
            );

            // ==================================
            // RESULTADO DE COMPARACION
            // ==================================

            // Umbral provisional solo para pruebas.
            // No utilizar directamente en produccion.
            double umbral = 0.363;

            if (similitud >= umbral) {

                System.out.println(
                        "Coincidencia facial de prueba"
                );

            } else {

                System.out.println(
                        "Rostros diferentes o "
                        + "coincidencia insuficiente"
                );
            }

            System.out.println();
            System.out.println(
                    "===== FIN DE PRUEBA ====="
            );

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (descriptor1 != null) {
                descriptor1.release();
            }

            if (descriptor2 != null) {
                descriptor2.release();
            }
        }
    }
}
