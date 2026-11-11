
package ec.asistencia;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

public class PruebaGuardarPerfilFacial {

    public static void main(String[] args) {

        final long estudianteId = 3;
        final String version = "PRUEBA_NO_BIOMETRICA";

        try {
            Properties propiedades = new Properties();

            try (InputStream entrada = Files.newInputStream(
                    Path.of("servidor.properties"))) {

                propiedades.load(entrada);
            }

            try (Base bd = new Base(propiedades)) {

                System.out.println(
                    "===== PRUEBA PERFIL FACIAL ====="
                );

                // Comprobar que es una cuenta de estudiante
                String[] cuenta = bd.uno(
                    "SELECT rol FROM cuenta WHERE id = ?",
                    estudianteId
                );

                if (cuenta == null
                        || !"ESTUDIANTE".equals(cuenta[0])) {
                    throw new IllegalStateException(
                        "La cuenta no es de un estudiante"
                    );
                }

                // No sobrescribir perfiles existentes
                String[] existente = bd.uno(
                    "SELECT estudiante_id FROM perfil_facial "
                    + "WHERE estudiante_id = ?",
                    estudianteId
                );

                if (existente != null) {
                    System.out.println(
                        "El estudiante ya tiene un perfil. "
                        + "No se modificó."
                    );
                    return;
                }

                // Descriptor ficticio: NO contiene datos faciales
                byte[] original = new byte[512];

                for (int i = 0; i < original.length; i++) {
                    original[i] = (byte) (i % 251);
                }

                // Iniciar transacción para deshacer la prueba
                bd.iniciar();

                try {
                    bd.guardarPerfilFacial(
                        estudianteId,
                        original,
                        version
                    );

                    // Leer descriptor cifrado directamente
                    String[] almacenado = bd.uno(
                        "SELECT OCTET_LENGTH(descriptor) "
                        + "FROM perfil_facial "
                        + "WHERE estudiante_id = ?",
                        estudianteId
                    );

                    // Obtener descriptor descifrado
                    byte[] recuperado =
                            bd.obtenerPerfilFacial(estudianteId);

                    System.out.println(
                        "Estudiante ID: " + estudianteId
                    );

                    System.out.println(
                        "Bytes originales: " + original.length
                    );

                    System.out.println(
                        "Bytes almacenados cifrados: "
                        + almacenado[0]
                    );

                    System.out.println(
                        "Datos recuperados correctamente: "
                        + Arrays.equals(original, recuperado)
                    );

                } finally {
                    // Deshacer INSERT de prueba
                    bd.revertir();

                    System.out.println(
                        "Transaccion revertida: "
                        + "no se guardaron cambios"
                    );
                }

                System.out.println(
                    "===== FIN DE PRUEBA ====="
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
