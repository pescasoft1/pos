(ns pos.handlers.dashboard.model
  (:require
   [pos.models.crud :refer [Query]]))

;; 🔹 TOTAL VENTAS
(defn get-stats []
  {:total-ventas
   (->> (Query " SELECT 
  substr(fecha,1,10) as dia,
  SUM(total) as total
FROM ventas
WHERE substr(fecha,1,10) = date('now')
GROUP BY dia
ORDER BY dia")
        first
        :total)})

;; 🔹 VENTAS POR DÍA
(defn get-ventas-mes []
  (Query "
    SELECT 
      substr(fecha,1,10) as dia,
      SUM(total) as total
    FROM ventas
    GROUP BY dia
    ORDER BY dia
  "))

;; 🔹 VENTAS HOY
(defn get-ventas-hoy []
  (Query "
    SELECT 
      vd.venta_id,
      vd.nombre,
      vd.cantidad,
      vd.subtotal
    FROM ventas_detalle vd
    JOIN ventas v ON v.id = vd.venta_id
    WHERE date(v.fecha) = date('now')
    ORDER BY vd.venta_id
  "))

;; 🔹 PRODUCTOS TOP
(defn get-productos-top []
  (Query "
    SELECT 
      vd.nombre,
      SUM(vd.cantidad) as total,
      COALESCE(p.imagen,'no-image.png') as imagen
    FROM ventas_detalle vd
    LEFT JOIN productos p ON p.nombre = vd.nombre
    GROUP BY vd.nombre
    ORDER BY total DESC
    LIMIT 12
  "))