package Dao;

import java.sql.Connection;
import java.util.List;

// Interfaz base para DAOs genéricos. Define operaciones CRUD comunes.


public interface GenericDAO<T> {
    

// Inserta la entidad en la base de datos (sin transacción externa).

    void insertar(T entidad) throws Exception;
    
// Inserta la entidad usando la transacción/conexión existente.

    
    void insertTx(T entidad, Connection conn) throws Exception;

// Actualiza los datos de la entidad en la base de datos.
    
    void actualizar(T entidad)throws Exception;

// Elimina la entidad por ID.
   
    void eliminar(int id)throws Exception;

 // Obtiene una entidad por su ID.
   
    T getById(int id)throws Exception;

// Devuelve todas las entidades de la tabla.

    List<T> getAll()throws Exception;

}
