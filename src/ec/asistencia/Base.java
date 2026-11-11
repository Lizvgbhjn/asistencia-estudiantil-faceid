package ec.asistencia;

import java.sql.*;
import java.util.*;

final class Base implements AutoCloseable {
    private final Connection conexion;

    Base(Properties p) throws SQLException {
        conexion = DriverManager.getConnection(p.getProperty("bd.url"), p.getProperty("bd.usuario"), p.getProperty("bd.clave"));
    }

    void iniciar() throws SQLException { conexion.setAutoCommit(false); }
    void confirmar() throws SQLException { conexion.commit(); }
    void revertir() throws SQLException { conexion.rollback(); }

    private PreparedStatement preparar(String sql, Object... valores) throws SQLException {
        PreparedStatement p = conexion.prepareStatement(sql);
        try {
            for (int i = 0; i < valores.length; i++) p.setObject(i + 1, valores[i]);
            return p;
        } catch (SQLException e) {
            p.close();
            throw e;
        }
    }

    List<String[]> consultar(String sql, Object... valores) throws SQLException {
        try (var p = preparar(sql, valores); var r = p.executeQuery()) {
            List<String[]> filas = new ArrayList<>();
            int columnas = r.getMetaData().getColumnCount();
            while (r.next()) {
                String[] fila = new String[columnas];
                for (int i = 0; i < columnas; i++) fila[i] = Objects.toString(r.getString(i + 1), "");
                filas.add(fila);
            }
            return filas;
        }
    }

    String[] uno(String sql, Object... valores) throws SQLException {
        var filas = consultar(sql, valores);
        return filas.isEmpty() ? null : filas.get(0);
    }

    int cambiar(String sql, Object... valores) throws SQLException {
        try (var p = preparar(sql, valores)) { return p.executeUpdate(); }
    }

    byte[] foto(long clase, long estudiante) throws SQLException {
        return imagen("SELECT foto FROM asistencia_registro WHERE clase_id=? AND estudiante_id=?", clase, estudiante);
    }

    byte[] fotoMatricula(long materia, long estudiante) throws SQLException {
        return imagen("SELECT foto_matricula FROM matricula WHERE materia_id=? AND estudiante_id=?", materia, estudiante);
    }

    private byte[] imagen(String sql, long registro, long estudiante) throws SQLException {
        try (var p = preparar(sql, registro, estudiante);
                var r = p.executeQuery()) {
            return r.next() ? r.getBytes(1) : null;
        }
    }

    int guardarPerfilFacial(long estudianteId, byte[] descriptor, String versionModelo) throws SQLException {
        if (descriptor == null || descriptor.length != 512) {
            throw new IllegalArgumentException("El descriptor facial debe tener 512 bytes.");
        }
        byte[] cifrado;
        try { cifrado = CifradoFacial.cifrar(descriptor); }
        catch (Exception e) { throw new IllegalStateException("No se pudo cifrar el perfil facial.", e); }
        return cambiar("""
                INSERT INTO perfil_facial(estudiante_id,descriptor,version_modelo,consentimiento_en)
                VALUES(?,?,?,CURRENT_TIMESTAMP)
                """, estudianteId, cifrado, versionModelo);
    }

    byte[] obtenerPerfilFacial(long estudianteId) throws SQLException {
        try (var p = preparar("SELECT descriptor FROM perfil_facial WHERE estudiante_id=?", estudianteId);
                var r = p.executeQuery()) {
            if (!r.next()) return null;
            try { return CifradoFacial.descifrar(r.getBytes(1)); }
            catch (Exception e) { throw new IllegalStateException("No se pudo descifrar el perfil facial.", e); }
        }
    }

    @Override public void close() throws SQLException { conexion.close(); }
}
