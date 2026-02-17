UTN - Tecnicatura Universitaria en Programación 2025
Trabajo Práctico Integrador - Programación 2
Estudiantes:  Andres Alvarez y Emiliano Alvarez

1. Descripción del Dominio y del Sistema

Descripción del Sistema:
Este sistema es una solución robusta de gestión de inventario desarrollada en Java con persistencia en MariaDB/MySQL. Su objetivo principal es administrar el ciclo de vida de los productos y sus respectivos códigos de barras, garantizando la integridad referencial y la consistencia de los datos mediante una arquitectura profesional en capas (DAO, Service, Entity, Config).

Características Principales:
Relación 1:1: Cada producto posee un identificador único mediante un código de barras.
Gestión Transaccional: Implementación de un TransactionManager para asegurar que las operaciones complejas (como crear un código y un producto simultáneamente) sean atómicas (se hacen ambas o ninguna).
Baja Lógica (Soft Delete): Los registros no se eliminan físicamente, permitiendo mantener la trazabilidad histórica del inventario.

Estructura de tablas implementada:

CREATE TABLE CodigoBarras (
    id INT AUTO_INCREMENT PRIMARY KEY,
    eliminado BOOLEAN DEFAULT FALSE,
    tipo ENUM('EAN13','EAN8','UPC') NOT NULL,
    valor VARCHAR(20) NOT NULL UNIQUE,
    fechaImplantacion DATE,
    observaciones VARCHAR(255)
);

CREATE TABLE Producto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    eliminado BOOLEAN DEFAULT FALSE,
    nombre VARCHAR(120) NOT NULL,
    marca VARCHAR(80),
    categoria VARCHAR(80),
    precio DOUBLE(10,2) NOT NULL,
    peso DOUBLE(10,3) CHECK(peso > 0),
    codigoBarras_id INT UNIQUE,
    FOREIGN KEY (codigoBarras_id) REFERENCES CodigoBarras(id)
);


2. Requisitos y Tecnologías
Para poder ejecutar este proyecto, necesitas contar con el siguiente entorno:
Java: JDK 17.
Base de Datos: MariaDB 10.4 y  MySQL Workbench 8.0.
Driver JDBC: mariadb-java-client.
IDE Recomendado: Apache Net Beans para Java.

3. Cómo compilar y ejecutar
Configuración de la Base de Datos:
Asegúrate de tener tu servidor SQL corriendo (XAMPPl).
Las credenciales por defecto configuradas en la clase DatabaseConnection son:
URL: jdbc:mariadb://localhost:3306/dbProdCodBarras
USER: root
PASSWORD: (vacío)

Ejecuta el script 1_creacionDB.sql para crear la estructura.
Ejecuta el script 2_carga_datos_prueba.sql para tener datos iniciales para testear.

Ejecución:
Prueba de Conexión: Se recomienda ejecutar primero la clase main.TestConexion.java. Si la consola muestra los datos de la base, la configuración es correcta.
Sistema Principal: Ejecuta la clase main.Main.java.

Flujo de uso:
El sistema iniciará un menú interactivo por consola.
Podrás Listar todos los productos activos (filtrados por baja lógica).
Podrás Registrar nuevos productos (el sistema te guiará para crear un código de barras si el producto lo requiere).
Podrás Modificar datos o realizar la Baja de un producto.

4. Link al Video Explicativo
https://www.youtube.com/watch?v=8xrs_ZQSjLI&feature=youtu.be


