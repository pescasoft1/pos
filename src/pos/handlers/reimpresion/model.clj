(ns pos.handlers.reimpresion.model
  (:require [pos.models.crud :refer [db Query]]))

(defn get-sale [venta-id]
  (first
   (Query db ["SELECT * FROM ventas WHERE id = ?" venta-id])))

(defn get-items [venta-id]
  (Query db
         ["SELECT * FROM ventas_detalle WHERE venta_id = ?"
          venta-id]))