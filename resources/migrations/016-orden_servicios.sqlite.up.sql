CREATE TABLE orden_servicios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_orden INT,
    id_servicio INT,
    FOREIGN KEY (id_orden) REFERENCES ordenes_trabajo(id_orden),
    FOREIGN KEY (id_servicio) REFERENCES servicios(id_servicio)
);