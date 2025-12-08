SELECT p.id, p.nombre, p.marca, p.categoria, p.precio, p.peso, p.codigoBarras , 
cb.id AS id, cb.tipo, cb.valor  
FROM producto p LEFT JOIN codigobarras cb ON p.codigobarras = cb.id 
WHERE p.eliminado = FALSE;