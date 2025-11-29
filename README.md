UTN - Tecnicatura Universitara en Programacion 2025

Trabajo Practico Integrador Programacion 2

Estudiantes: 
Andres Alvarez
Emiliano Alvarez

1.Descripcion del dominio: NO SE BIEN QUE SERIA????

Descripcin del sistema: ////////////COMPLETAR////////////

Producto (
 id INT AUTO_INCREMENT PRIMARY KEY,
 eliminado bool,
 nombre varchar(120) NOT NULL,
 marca varchar(80),
 categoria varchar(80),
 precio double(10,2) NOT NULL,
 peso double(10,3) CHECK(peso>0),
 codigoBarras int UNIQUE,
 foreign key (codigoBarras) references CodigoBarras(id) -- necesita que este creada la otra tabla
);

CodigoBarras (
 id INT AUTO_INCREMENT PRIMARY KEY,
 eliminado bool,
 tipo enum ('EAN13','EAN8','UPC') NOT NULL,
 valor varchar(20) NOT NULL UNIQUE,
 fechaImplantacion date,
 observaciones varchar(255) 
 );

////////////COMPLETAR////////////

2.Requisitos y creacion de Base de datos:

JAVA: ////////////COMPLETAR////////////
MySQL: ////////////COMPLETAR////////////
Driver: ////////////COMPLETAR////////////

3.Como compilar y ejecutar (credenciales de prueba y flujo de uso). 

*   Datos de conexión BD:
URL = "localhost:3306/dbProdCodBarras"
USER = "root"
PASSWORD = ""

*   Creación BD:
1. ejecutar 1_creacionDB.sql
2. ejecutar 2_carga_datos_prueba.sql

* (opcional) ejecutar main.TestConexion.java para prueba de conexion y lectura de datos con un PreparedStatement

*   correr main.main.java ////////////COMPLETAR////////////

4.Link al video Explicativo

////////////COMPLETAR////////////
