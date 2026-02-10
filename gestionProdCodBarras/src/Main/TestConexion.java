
package Main;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Config.DatabaseConnection;

public class TestConexion {
    public static void main(String[] args) {


//Bloque try-with-resources que garantiza el cierre automático de la conexión.
//No requiere llamar manualmente a conn.close().
 


        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Conexión establecida con éxito.");
                
// Ejecuta la consulta SQL usando PreparedStatement para evitar inyecciones y reutilizar la sentencia


                String sql = "SELECT * FROM producto";
                try (PreparedStatement pstmt = conn.prepareStatement(sql); 
                        ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("📋 Listado de productos:");
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String nombre = rs.getString("nombre");
                        double precio = rs.getDouble("precio");
                        System.out.println("ID: " + id + ", Nombre: " + nombre + ", Precio: " + precio);
                    }
                }
            } else {
                System.out.println("❌ No se pudo establecer la conexión.");
            }
        } catch (SQLException e) {

// Manejo de errores SQL: muestra mensaje y permite depuración con stacktrace

            System.err.println("⚠️ Error al conectar a la base de datos: " + e.getMessage());
e.printStackTrace(); // Stacktrace para diagnosticar errores en tiempo de ejecución
        }
    }
}