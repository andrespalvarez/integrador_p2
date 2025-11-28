use dbProdCodBarras;

INSERT INTO CodigoBarras (id,eliminado,tipo,valor,fechaImplantacion,observaciones) VALUES
('1', '0', 'EAN8', 'CB000001', '2020-04-05', 'mesa básica rectangular hierro blanco fabricado por Muebleria Argentina'),
('2', '0', 'EAN8', 'CB000002', '2020-02-22', 'mesa básica rectangular hierro blanco fabricado por Muebleria Argentina'),
('3', '0', 'EAN13', 'CB000003', '2025-12-07', 'mesa básica rectangular hierro blanco fabricado por Muebleria Argentina'),
('4', '0', 'EAN13', 'CB000004', '2025-03-31', 'mesa básica rectangular hierro blanco fabricado por Fabrica de Muebles'),
('5', '0', 'EAN13', 'CB000005', '2022-06-07', 'mesa básica rectangular hierro blanco fabricado por Fabrica de Muebles'),
('6', '0', 'EAN8', 'CB000006', '2022-06-03', 'mesa básica rectangular hierro blanco fabricado por Fabrica de Muebles'),
('7', '0', 'EAN13', 'CB000007', '2024-10-22', 'mesa básica rectangular hierro blanco fabricado por Muebles Especiales'),
('8', '0', 'UPC', 'CB000008', '2024-10-14', 'mesa básica rectangular hierro blanco fabricado por Muebles Especiales'),
('9', '0', 'EAN8', 'CB000009', '2023-07-06', 'mesa básica rectangular hierro blanco fabricado por Muebles Especiales'),
('10', '0', 'EAN13', 'CB000010', '2023-03-13', 'mesa básica rectangular hierro blanco fabricado por Carpinteria de Diseño');

INSERT INTO Producto (id,eliminado,nombre,marca,categoria,precio, peso, codigoBarras) VALUES
('1', '0', 'mesa básica rectangular hierro blanco', 'Muebleria Argentina', 'usado', '27812.88', '63.847', '1'),
('2', '0', 'mesa básica rectangular hierro ', 'Muebleria Argentina', 'nuevo', '29705.06', '57.399', '2'),
('3', '0', 'mesa básica rectangular ', 'Muebleria Argentina', 'reacondicionado', '10086.66', '95.356', '3'),
('4', '0', 'mesa básica ', 'Fabrica de Muebles', 'usado', '6318.11', '4.481', '4'),
('5', '0', 'mesa ', 'Fabrica de Muebles', 'nuevo', '46330.57', '36.237', '5'),
('6', '0', 'silla premium circular hierro rojo', 'Fabrica de Muebles', 'reacondicionado', '7698.54', '67.645', '6'),
('7', '0', 'silla premium circular madera', 'Muebles Especiales', 'usado', '44500.98', '29.411', '7'),
('8', '0', 'silla premium circular', 'Muebles Especiales', 'nuevo', '44409.27', '44.021', '8'),
('9', '0', 'silla premium', 'Muebles Especiales', 'reacondicionado', '33543.40', '31.773', '9'),
('10', '0', 'silla', 'Carpinteria de Diseño', 'usado', '29489.23', '26.702', '10');