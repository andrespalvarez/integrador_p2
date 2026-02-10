package Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

// Datos básicos de conexión a la base de datos


    private static final String URL = "jdbc:mariadb://localhost:3306/dbProdCodBarras"; //direccion mariadb
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
           
// Carga única del driver JDBC al iniciar la clase
            
            
            Class.forName("org.mariadb.jdbc.Driver"); //driver mariadb
            
        } catch (ClassNotFoundException e) {

// Lanza excepción si el driver JDBC no está disponible


            throw new RuntimeException("Error: No se encontró el driver JDBC.", e);
        }
    }


//Devuelve una conexión activa a la base de datos.
//@return Connection establecida correctamente.
//@throws SQLException si ocurre un error al conectar.
 

    public static Connection getConnection() throws SQLException {

// Verifica que la configuración de conexión sea válida antes de abrir la conexión


        if (URL == null || URL.isEmpty() || USER == null || USER.isEmpty() || PASSWORD == null ) {
            throw new SQLException("Configuración de la base de datos incompleta o inválida.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}