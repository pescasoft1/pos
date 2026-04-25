(ns pos.handlers.pos.model
  (:require [clojure.java.jdbc :as jdbc]
            [pos.hooks.movimientos :as mov-hooks]
            [pos.models.crud :refer [db Query Insert]]))

(defn get-productos
  "Fetch all products with their current inventory stock level."
  []
  (Query db [(str "SELECT p.id,p.numero_producto, p.nombre, p.precio, p.categoria, p.imagen,"
                  " COALESCE(i.cantidad, 0) as stock"
                  " FROM productos p"
                  " LEFT JOIN inventario i ON i.producto_id = p.id"
                  " ORDER BY p.nombre")]))

(defn search-productos
  "Search products by name or category."
  [term]
  (let [like-term (str "%" term "%")]
    (Query db [(str "SELECT p.id,p.numero_producto,p.nombre, p.precio, p.categoria, p.imagen,"
                    " COALESCE(i.cantidad, 0) as stock"
                    " FROM productos p"
                    " LEFT JOIN inventario i ON i.producto_id = p.id"
                    " WHERE p.nombre LIKE ? OR p.categoria LIKE ? OR p.numero_producto LIKE ?"
                    " ORDER BY p.nombre")
               like-term like-term])))

(defn register-sale-tx!
  "Registers a complete sale inside a single database transaction.
   Inserts the sale header, one detail row per item, and one movimiento
   per item (which triggers the inventory hook).
   Returns the new venta id."
  [venta-header items]
  (jdbc/with-db-transaction [tx db]
    (let [venta-result (first (Insert tx :ventas venta-header))
          venta-id     (or (:generated_key venta-result)
                           ((keyword "last_insert_rowid()") venta-result)
                           (:last_insert_rowid venta-result)
                           (:id venta-result)
                           (first (vals venta-result)))]
      (when (nil? venta-id)
        (throw (ex-info "Could not determine venta id after insert" {:result venta-result})))
      (doseq [item items]
        ;; Insert the detail line
        (Insert tx :ventas_detalle
                {:venta_id        venta-id
                 :producto_id     (:producto_id item)
                 :cantidad        (:cantidad item)
                 :precio_unitario (:precio item)
                 :subtotal        (* (:cantidad item) (:precio item))})
        ;; Insert a movimiento (type = venta) and fire the hook to reduce stock
        (let [mov {:producto_id     (:producto_id item)
                   :tipo_movimiento "venta"
                   :cantidad        (:cantidad item)}
              result (Insert tx :movimientos mov)]
          (mov-hooks/after-save tx mov result)))
      venta-id)))
