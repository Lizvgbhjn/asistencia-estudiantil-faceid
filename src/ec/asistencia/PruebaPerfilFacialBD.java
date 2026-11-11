
package ec.asistencia;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PruebaPerfilFacialBD {

    public static void main(String[] args) {

        Path archivo = Path.of("servidor.properties");

        try {
            if (!Files.isRegularFile(archivo)) {
                throw new IllegalStateException(
                    "No se encuentra servidor.properties"
                );
            }

            Properties propiedades = new Properties();

            try (InputStream entrada =
                    Files.newInputStream(archivo)) {

                propiedades.load(entrada);
            }

            try (Base bd = new Base(propiedades)) {

                System.out.println(
                    "===== PRUEBA DE MYSQL ====="
                );

                String[] resultado = bd.uno(
                    "SELECT DATABASE(), VERSION()"
                );

                System.out.println(
                    "Base de datos: " + resultado[0]
                );

                System.out.println(
                    "Version MySQL/MariaDB: " + resultado[1]
                );

                String[] total = bd.uno(
                    "SELECT COUNT(*) FROM perfil_facial"
                );

                System.out.println(
                    "Perfiles faciales registrados: "
                    + total[0]
                );

                System.out.println(
                    "Conexion correcta con MySQL"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
