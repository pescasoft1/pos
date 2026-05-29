CREATE TABLE productos (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	nombre VARCHAR,
	precio DECIMAL,
	categoria VARCHAR,
	imagen VARCHAR,
	codigo_qr TEXT,
	precio_compra DECIMAL
, codigobarra TEXT);

CREATE UNIQUE INDEX idx_productos_codigobarra
ON productos(codigobarra);