(ns pos.handlers.tipo-cambio.model
  (:require [pos.models.crud :refer [Insert Query Update]]))

(defn today-rate []
  (first
   (Query ["SELECT id, fecha, valor_pesos, usuario_id, created_at
            FROM tipo_cambio
            WHERE fecha = DATE('now', 'localtime')
            ORDER BY created_at DESC, id DESC
            LIMIT 1"])))

(defn latest-rate []
  (first
   (Query ["SELECT id, fecha, valor_pesos, usuario_id, created_at
            FROM tipo_cambio
            ORDER BY fecha DESC, created_at DESC, id DESC
            LIMIT 1"])))

(defn save-today-rate! [valor-pesos usuario-id]
  (if-let [current (today-rate)]
    (do
      (Update :tipo_cambio
              {:valor_pesos valor-pesos
               :usuario_id usuario-id
               :created_at (java.time.LocalDateTime/now)}
              ["id = ?" (:id current)])
      (assoc current :valor_pesos valor-pesos :usuario_id usuario-id))
    (let [result (first
                  (Insert :tipo_cambio
                          {:fecha (str (java.time.LocalDate/now))
                           :valor_pesos valor-pesos
                           :usuario_id usuario-id}))
          id (or (:generated_key result)
                 ((keyword "last_insert_rowid()") result)
                 (:last_insert_rowid result)
                 (:id result)
                 (first (vals result)))]
      {:id id
       :fecha (str (java.time.LocalDate/now))
       :valor_pesos valor-pesos
       :usuario_id usuario-id})))
