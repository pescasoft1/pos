CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  lastname TEXT,
  firstname TEXT,
  username TEXT UNIQUE,
  password TEXT,
  dob TEXT,
  cell TEXT,
  phone TEXT,
  fax TEXT,
  email TEXT,
  level TEXT,
  active TEXT,
  imagen TEXT,
  last_login TEXT DEFAULT (datetime('now'))
);


CREATE OR REPLACE VIEW users_view AS
SELECT 
    id,
    lastname,
    firstname,
    username,
    dob,
    cell,
    phone,
    fax,
    email,
    level,
    active,
    imagen,
    last_login,
    DATE_FORMAT(dob, '%d/%m/%Y') AS dob_formatted,
    CASE
        WHEN level = 'U' THEN 'Usuario'
        WHEN level = 'A' THEN 'Administrador'
        ELSE 'Sistema'
    END AS level_formatted,
    CASE
        WHEN active = 'T' THEN 'Activo'
        ELSE 'Inactivo'
    END AS active_formatted
FROM users
ORDER BY lastname, firstname;


CREATE TABLE IF NOT EXISTS audit_log (
  id        INTEGER PRIMARY KEY AUTOINCREMENT,
  entity    TEXT        NOT NULL,
  operation TEXT        NOT NULL,
  data      TEXT,
  user_id   INTEGER,
  timestamp TEXT
);


CREATE TABLE IF NOT EXISTS productos (
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


CREATE TABLE IF NOT EXISTS provedores (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre   VARCHAR(255),
    email    VARCHAR(255),
    telefono VARCHAR(255)
);


CREATE TABLE IF NOT EXISTS inventario (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_id          INT,
    cantidad             INT DEFAULT 0,
    provedor_id          INT,
    ultima_actualizacion DATE DEFAULT (date('now')), costo integer,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (provedor_id) REFERENCES provedores(id) ON DELETE SET NULL
);
CREATE INDEX idx_inventario_producto_id ON inventario(producto_id);
CREATE INDEX idx_inventario_provedor_id ON inventario(provedor_id);


CREATE TABLE movimientos (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_id      INT,
    tipo_movimiento  VARCHAR(10) NOT NULL CHECK (tipo_movimiento IN ('venta', 'compra')),
    fecha_movimiento DATE DEFAULT (date('now')),
    cantidad         INT NOT NULL,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE
);
CREATE INDEX idx_movimientos_producto_id ON movimientos(producto_id);


CREATE TABLE if NOT EXISTS ventas (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha     DATETIME DEFAULT CURRENT_TIMESTAMP,
    total     DECIMAL(12,2) NOT NULL DEFAULT 0,
    pago      DECIMAL(12,2) NOT NULL DEFAULT 0,
    cambio    DECIMAL(12,2) NOT NULL DEFAULT 0,
    usuario_id INT,
    estado    VARCHAR(20) NOT NULL DEFAULT 'completada'
              CHECK (estado IN ('completada', 'cancelada')), cotizacion_id INT, tipo_pago TEXT, descuento TEXT, moneda TEXT DEFAULT 'MXN'
CHECK (moneda IN ('MXN', 'USD')),
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE SET NULL
);
CREATE INDEX idx_ventas_usuario_id ON ventas(usuario_id);
CREATE INDEX idx_ventas_cotizacion_id ON ventas(cotizacion_id);



CREATE TABLE if NOT EXISTS ventas_detalle (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    venta_id INT NOT NULL, producto_id INT, 
    nombre VARCHAR(200) NOT NULL DEFAULT '', 
    cantidad INT NOT NULL, 
    precio_unitario DECIMAL(10,2) NOT NULL, 
    subtotal DECIMAL(12,2) NOT NULL, 
    categoria VARCHAR(20) NOT NULL DEFAULT '', 
    FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE, 
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE);
CREATE INDEX idx_ventas_detalle_venta_id ON ventas_detalle(venta_id);
CREATE INDEX idx_ventas_detalle_producto_id ON ventas_detalle(producto_id);


CREATE TABLE IF NOT EXISTS  clientes (
    id integer primary key autoincrement,
    nombre text,
    telefono text,
    email text,
    direccion text,
    rfc text,
    fecha_registro text default (datetime('now')),
    activo integer default 1
);


CREATE TABLE IF NOT EXISTS cotizaciones (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha       DATETIME DEFAULT CURRENT_TIMESTAMP,
    cliente_nombre VARCHAR(100),
    cliente_telefono VARCHAR(20),
    total       DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado      VARCHAR(20) NOT NULL DEFAULT 'borrador'
                CHECK (estado IN ('borrador', 'enviada', 'aceptada', 'rechazada', 'cancelada')),
    notas       TEXT,
    usuario_id  INT, venta_id INT,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE SET NULL
);
CREATE INDEX idx_cotizaciones_fecha ON cotizaciones(fecha);
CREATE INDEX idx_cotizaciones_estado ON cotizaciones(estado);
CREATE INDEX idx_cotizaciones_cliente_nombre ON cotizaciones(cliente_nombre);
CREATE INDEX idx_cotizaciones_cliente_telefono ON cotizaciones(cliente_telefono);
CREATE INDEX idx_cotizaciones_venta_id ON cotizaciones(venta_id);


CREATE TABLE IF NOT EXISTS cotizaciones_detalle (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    cotizacion_id   INT NOT NULL,
    producto_id     INT,
    nombre          VARCHAR(200) NOT NULL,
    cantidad        INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(12,2) NOT NULL,
    tipo            VARCHAR(20) NOT NULL DEFAULT 'producto'
                CHECK (tipo IN ('servicio', 'producto', 'misc')),
    FOREIGN KEY (cotizacion_id) REFERENCES cotizaciones(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE SET NULL
);
CREATE INDEX idx_cotizaciones_detalle_cotizacion_id ON cotizaciones_detalle(cotizacion_id);
CREATE INDEX idx_cotizaciones_detalle_producto_id ON cotizaciones_detalle(producto_id);


CREATE TABLE IF NOT EXISTS caja_movimientos (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  fecha DATETIME NOT NULL,
  tipo_movimiento TEXT NOT NULL,   -- apertura, venta, retiro, ingreso_extra, cierre, ajuste
  monto NUMERIC(10,2) NOT NULL,
  venta_id INTEGER,
  descripcion TEXT,
  usuario_id INTEGER  
);


CREATE TABLE tipo_cambio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha DATE NOT NULL,
    valor_pesos DECIMAL(12,4) NOT NULL,
    usuario_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE SET NULL
);
CREATE UNIQUE INDEX idx_tipo_cambio_fecha ON tipo_cambio(fecha);