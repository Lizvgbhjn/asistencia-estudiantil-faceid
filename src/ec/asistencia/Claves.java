package ec.asistencia;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class Claves {
    private static final SecureRandom AZAR = new SecureRandom();

    static String token() {
        byte[] bytes = new byte[32];
        AZAR.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String crear(String clave) {
        if (clave.length() < 8 || clave.length() > 128) {
            throw new IllegalArgumentException("La clave debe tener entre 8 y 128 caracteres.");
        }
        byte[] sal = new byte[16];
        AZAR.nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal) + ":" + Base64.getEncoder().encodeToString(derivar(clave, sal));
    }

    static boolean verificar(String clave, String guardada) {
        if (clave.length() > 128) return false;
        String[] partes = guardada.split(":");
        var base64 = Base64.getDecoder();
        return MessageDigest.isEqual(base64.decode(partes[1]), derivar(clave, base64.decode(partes[0])));
    }

    private static byte[] derivar(String clave, byte[] sal) {
        var datos = new PBEKeySpec(clave.toCharArray(), sal, 600_000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(datos).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        } finally {
            datos.clearPassword();
        }
    }
}
