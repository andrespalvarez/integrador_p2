drop schema dbProdCodBarras;
CREATE SCHEMA if not exists dbProdCodBarras ;
use dbProdCodBarras;
-- --------------------------------------------------------
-- 						CREATE DE TABLAS
-- --------------------------------------------------------
-- crea tabla CodigoBarras (primero esta por la relacion q tiene con la otra tabla)
create table if not exists CodigoBarras (
 id INT AUTO_INCREMENT PRIMARY KEY,
 eliminado bool,
 tipo enum ('EAN13','EAN8','UPC') NOT NULL,
 valor varchar(20) NOT NULL UNIQUE,
 fechaAsignacion date,
 observaciones varchar(255) 
 );
-- crea tabla Producto
create table if not exists Producto (
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
