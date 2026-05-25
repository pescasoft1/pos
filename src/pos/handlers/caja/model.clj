(ns pos.handlers.caja.model
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [pos.models.crud :refer [db Query Insert]]))

(def ^:private allowed-manual-types
  #{"apertura" "retiro" "ingreso_extra" "ajuste" "cierre"})

(defn- now-ts
  []
  (.format (java.time.LocalDateTime/now)
           (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")))

(defn- parse-double
  [v]
  (try
    (cond
      (nil? v) 0.0
      (number? v) (double v)
      :else (Double/parseDouble (str/trim (str v))))
    (catch Exception _
      0.0)))

(defn- parse-long-safe
  [v]
  (try
    (let [s (str/trim (str v))]
      (when-not (str/blank? s)
        (Long/parseLong s)))
    (catch Exception _
      nil)))

(defn- normalize-monto
  [tipo monto]
  (let [m (Math/abs (double (parse-double monto)))]
    (case tipo
      "retiro" (- m)
      "cierre" 0.0
      "ajuste" (parse-double monto)
      "venta" m
      "apertura" m
      "ingreso_extra" m
      (parse-double monto))))

(defn- normalize-row
  [m]
  (let [tipo (-> (or (:tipo_movimiento m) "")
                 str
                 str/lower-case
                 str/trim)]
    {:fecha          (or (:fecha m) (now-ts))
     :tipo_movimiento tipo
     :monto          (normalize-monto tipo (:monto m))
     :venta_id       (parse-long-safe (:venta_id m))
     :descripcion    (or (:descripcion m) "")
     :usuario_id     (parse-long-safe (:usuario_id m))}))

(defn- decorate-with-balance
  [rows]
  (loop [acc []
         saldo 0.0
         xs rows]
    (if (empty? xs)
      acc
      (let [row (first xs)
            monto (double (or (:monto row) 0.0))
            saldo' (+ saldo monto)]
        (recur (conj acc (assoc row :saldo_despues saldo'))
               saldo'
               (rest xs))))))

(defn get-movimientos
  []
  (let [rows (Query db
                    ["SELECT id, fecha, tipo_movimiento, monto, venta_id, descripcion, usuario_id
                      FROM caja_movimientos
                      ORDER BY datetime(fecha) ASC, id ASC"])]
    (decorate-with-balance rows)))

(defn get-resumen
  ([] (get-resumen (get-movimientos)))
  ([rows]
   (let [vals (map #(double (or (:monto %) 0.0)) rows)
         ingresos (reduce + 0.0 (filter pos? vals))
         retiros (reduce + 0.0 (map #(Math/abs ^double %) (filter neg? vals)))
         saldo (reduce + 0.0 vals)]
     {:total_ingresos ingresos
      :total_retiros retiros
      :total_movimientos (count rows)
      :saldo_final saldo})))

(defn context
  []
  {:caja_actual "CAJA-001"
   :movimientos (get-movimientos)
   :resumen (get-resumen)})

(defn create-movimiento!
  [movement]
  (let [row (normalize-row movement)]
    (when-not (allowed-manual-types (:tipo_movimiento row))
      (throw (ex-info "Tipo de movimiento no permitido para captura manual."
                      {:tipo (:tipo_movimiento row)})))
    (jdbc/with-db-transaction [tx db]
      (Insert tx :caja_movimientos row)
      (first (Query tx
                    ["SELECT id, fecha, tipo_movimiento, monto, venta_id, descripcion, usuario_id
                      FROM caja_movimientos
                      ORDER BY id DESC
                      LIMIT 1"])))))

(defn registrar-venta-automatica!
  [{:keys [fecha monto venta_id usuario_id descripcion]}]
  (let [row {:fecha          (or fecha (now-ts))
             :tipo_movimiento "venta"
             :monto          (Math/abs (double (parse-double monto)))
             :venta_id       (parse-long-safe venta_id)
             :descripcion    (or descripcion "Venta en efectivo")
             :usuario_id     (parse-long-safe usuario_id)}]
    (jdbc/with-db-transaction [tx db]
      (Insert tx :caja_movimientos row)
      (first (Query tx
                    ["SELECT id, fecha, tipo_movimiento, monto, venta_id, descripcion, usuario_id
                      FROM caja_movimientos
                      ORDER BY id DESC
                      LIMIT 1"])))))