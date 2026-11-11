/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.asistencia;
import org.opencv.core.Core;
import org.opencv.core.Mat;
/**
 *
 * @author user
 */
public class PruebaOpenCV {
    
    public static void main(String[] args) {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            System.out.println("OpenCV cargado correctamente");
            System.out.println("Version: " + Core.VERSION);

            Mat imagen = Mat.eye(3, 3, 0);
            System.out.println(imagen.dump());
            imagen.release();

        } catch (UnsatisfiedLinkError e) {
            System.err.println("Error al cargar OpenCV:");
            e.printStackTrace();
        }
    }

}
