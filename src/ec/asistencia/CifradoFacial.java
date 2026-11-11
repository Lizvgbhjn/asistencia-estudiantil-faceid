/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.asistencia;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 *
 * @author user
 */
public class CifradoFacial {
   
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private static SecretKeySpec obtenerClave() {
        String claveBase64 =
                System.getenv("ASISTENCIA_CLAVE_FACIAL");

        if (claveBase64 == null || claveBase64.isBlank()) {
            throw new IllegalStateException(
                    "Falta ASISTENCIA_CLAVE_FACIAL"
            );
        }

        byte[] clave = Base64.getDecoder().decode(
                claveBase64
        );

        if (clave.length != 32) {
            throw new IllegalArgumentException(
                    "La clave AES-256 debe tener 32 bytes"
            );
        }

        return new SecretKeySpec(clave, "AES");
    }

    static void validarClave() {
        obtenerClave();
    }

    public static byte[] cifrar(byte[] datos)
            throws Exception {

        byte[] iv = new byte[IV_BYTES];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(
                "AES/GCM/NoPadding"
        );

        cipher.init(
                Cipher.ENCRYPT_MODE,
                obtenerClave(),
                new GCMParameterSpec(TAG_BITS, iv)
        );

        byte[] cifrado = cipher.doFinal(datos);

        byte[] resultado = new byte[
                iv.length + cifrado.length
        ];

        System.arraycopy(
                iv, 0, resultado, 0, iv.length
        );

        System.arraycopy(
                cifrado, 0, resultado,
                iv.length, cifrado.length
        );

        return resultado;
    }

    public static byte[] descifrar(byte[] datos)
            throws Exception {

        if (datos == null
                || datos.length < IV_BYTES + 16) {
            throw new IllegalArgumentException(
                    "Datos cifrados invalidos"
            );
        }

        byte[] iv = Arrays.copyOfRange(
                datos, 0, IV_BYTES
        );

        byte[] cifrado = Arrays.copyOfRange(
                datos, IV_BYTES, datos.length
        );

        Cipher cipher = Cipher.getInstance(
                "AES/GCM/NoPadding"
        );

        cipher.init(
                Cipher.DECRYPT_MODE,
                obtenerClave(),
                new GCMParameterSpec(TAG_BITS, iv)
        );

        return cipher.doFinal(cifrado);
    }


}
