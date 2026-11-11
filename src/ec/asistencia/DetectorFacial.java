/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.asistencia;
import java.io.File;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.FaceDetectorYN;
/**
 *
 * @author user
 */
public class DetectorFacial {
    
    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String modelo = "modelos/face_detection_yunet_2023mar.onnx";
        String foto = "foto_prueba.jpg";

        if (!new File(modelo).exists()) {
            System.out.println("No se encuentra el modelo YuNet.");
            return;
        }

        Mat imagen = Imgcodecs.imread(foto);

        if (imagen.empty()) {
            System.out.println("No se pudo abrir la fotografia.");
            return;
        }

        FaceDetectorYN detector = null;
        Mat rostros = new Mat();

        try {
            detector = FaceDetectorYN.create(
                modelo,
                "",
                new Size(320, 320),
                0.9f,
                0.3f,
                5000
            );

            detector.setInputSize(
                new Size(imagen.cols(), imagen.rows())
            );

            detector.detect(imagen, rostros);

            int cantidad = rostros.rows();

            System.out.println("OpenCV: " + Core.VERSION);
            System.out.println("Rostros detectados: " + cantidad);

            if (cantidad == 1) {
                System.out.println("Un rostro detectado correctamente.");
            } else if (cantidad == 0) {
                System.out.println("No se detectaron rostros.");
            } else {
                System.out.println("Se detectaron varios rostros.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rostros.release();
            imagen.release();
        }
    }


}
