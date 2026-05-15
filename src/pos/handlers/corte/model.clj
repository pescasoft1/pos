(ns pos.handlers.corte.model
  (:require [pos.models.crud :refer [db Query]]))

(defn get-corte [desde hasta]
  (let [ventas (Query db
                      ["SELECT id, fecha, total, pago, cambio, usuario_id, estado, cotizacion_id
                        FROM ventas
                        WHERE date(fecha) BETWEEN date(?) AND date(?)
                          AND estado = 'completada'
                        ORDER BY fecha ASC, id ASC"
                       desde hasta])]
    (mapv
     (fn [venta]
       (assoc venta
              :items
              (Query db
                     ["SELECT nombre, cantidad, precio_unitario, subtotal, categoria
                       FROM ventas_detalle
                       WHERE venta_id = ?
                       ORDER BY id ASC"
                      (:id venta)])))
     ventas)))

(defn get-corte-resumen [desde hasta]
  (first
   (Query db
          ["SELECT
              COUNT(*) AS cantidad_ventas,
              COALESCE(SUM(total), 0) AS total_vendido,
              COALESCE(SUM(pago), 0) AS total_pagado,
              COALESCE(SUM(cambio), 0) AS total_cambio
            FROM ventas
            WHERE date(fecha) BETWEEN date(?) AND date(?)
              AND estado = 'completada'"
           desde hasta])))