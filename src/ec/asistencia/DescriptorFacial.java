/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.asistencia;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
/**
 *
 * @author user
 */
public class DescriptorFacial {
    
    private static final int DIMENSIONES = 128;

    // Convertir Mat de OpenCV a byte[]
    public static byte[] convertirABytes(Mat descriptor) {

        if (descriptor == null
                || descriptor.empty()
                || descriptor.type() != CvType.CV_32FC1
                || descriptor.total() != DIMENSIONES) {

            throw new IllegalArgumentException(
                    "Descriptor facial invalido"
            );
        }

        float[] valores = new float[DIMENSIONES];
        descriptor.get(0, 0, valores);

        ByteBuffer buffer = ByteBuffer.allocate(
                DIMENSIONES * Float.BYTES
        );

        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (float valor : valores) {
            if (!Float.isFinite(valor)) {
                throw new IllegalArgumentException(
                        "Descriptor contiene valores invalidos"
                );
            }
            buffer.putFloat(valor);
        }

        return buffer.array();
    }

    // Convertir byte[] de MySQL a Mat de OpenCV
    public static Mat convertirAMat(byte[] datos) {

        if (datos == null
                || datos.length != DIMENSIONES * Float.BYTES) {

            throw new IllegalArgumentException(
                    "Datos faciales invalidos"
            );
        }

        ByteBuffer buffer = ByteBuffer.wrap(datos);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        float[] valores = new float[DIMENSIONES];

        for (int i = 0; i < DIMENSIONES; i++) {
            valores[i] = buffer.getFloat();

            if (!Float.isFinite(valores[i])) {
                throw new IllegalArgumentException(
                        "Datos faciales contienen valores invalidos"
                );
            }
        }

        Mat descriptor = new Mat(
                1, DIMENSIONES, CvType.CV_32FC1
        );

        descriptor.put(0, 0, valores);

        return descriptor;
    }


}
