
package ec.asistencia;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PruebaCifradoFacial {

    public static void main(String[] args) {

        try {
            System.out.println("===== PRUEBA DE CIFRADO =====");

            byte[] original = "Prueba de descriptor facial"
                    .getBytes(StandardCharsets.UTF_8);

            byte[] cifrado = CifradoFacial.cifrar(original);

            byte[] recuperado = CifradoFacial.descifrar(cifrado);

            System.out.println(
                    "Tamaño original: " + original.length
            );

            System.out.println(
                    "Tamaño cifrado: " + cifrado.length
            );

            System.out.println(
                    "Cifrado diferente del original: "
                    + !Arrays.equals(original, cifrado)
            );

            System.out.println(
                    "Datos recuperados correctamente: "
                    + Arrays.equals(original, recuperado)
            );

            System.out.println("===== FIN DE PRUEBA =====");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
