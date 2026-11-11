package ec.asistencia;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Size;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.FaceRecognizerSF;

final class FaceId {
    private static final String YUNET = "modelos/face_detection_yunet_2023mar.onnx";
    private static final String SFACE = "modelos/face_recognition_sface_2021dec.onnx";
    private static boolean cargado;
    private static FaceDetectorYN detector;
    private static FaceRecognizerSF reconocedor;

    private FaceId() {}

    static synchronized byte[] descriptor(BufferedImage imagen) throws Exception {
        if (!cargado) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            detector = FaceDetectorYN.create(YUNET, "", new Size(320, 320), 0.9f, 0.3f, 5000);
            reconocedor = FaceRecognizerSF.create(SFACE, "");
            cargado = true;
        }
        Path temporal = Files.createTempFile("asistencia-rostro-", ".jpg");
        try {
            ImageIO.write(imagen, "jpg", temporal.toFile());
            var descriptor = ReconocimientoFacial.obtenerDescriptor(temporal.toString(), detector, reconocedor);
            try { return DescriptorFacial.convertirABytes(descriptor); }
            finally { descriptor.release(); }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
