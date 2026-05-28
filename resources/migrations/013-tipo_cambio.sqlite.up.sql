CREATE TABLE IF NOT EXISTS tipo_cambio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha DATE NOT NULL,
    valor_pesos DECIMAL(12,4) NOT NULL,
    usuario_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_tipo_cambio_fecha
ON tipo_cambio(fecha);
