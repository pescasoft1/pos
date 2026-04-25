(ns pos.handlers.ordentrabajo.model
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [pos.models.crud :refer [db Insert Query]]))

;; =========================
;; PRODUCTOS
;; =========================
(defn get-productos
  []
  (Query db
         [(str "SELECT p.id,p.numero_producto, p.nombre, p.precio, p.categoria, p.imagen,"
               " COALESCE(i.cantidad, 0) as stock"
               " FROM productos p"
               " LEFT JOIN inventario i ON i.producto_id = p.id"
               " ORDER BY p.nombre")]))

;; =========================
;; SERVICIOS
;; =========================
(defn get-servicios
  []
  (Query db ["SELECT id_servicio, nombre FROM servicios ORDER BY nombre"]))

;; =========================
;; GUARDAR ORDEN
;; =========================
(defn guardar-orden-tx!
  [{:keys [cliente bicicleta diagnostico servicios productos]}]

  (jdbc/with-db-transaction [tx db]

    ;; -------------------------
    ;; 1. CLIENTE
    ;; -------------------------
    (let [cliente-result (first (Insert tx :clientes
                                        {:nombre (:nombre cliente)
                                         :telefono (:telefono cliente)
                                         :correo (:email cliente)}))

          cliente-id (or (:generated_key cliente-result)
                         (:id cliente-result)
                         (first (vals cliente-result)))]

      ;; -------------------------
      ;; 2. BICICLETA
      ;; -------------------------
      (let [bicicleta-result (first (Insert tx :bicicletas
                                            {:id_cliente cliente-id
                                             :marca_modelo (:marca_modelo bicicleta)
                                             :tipo (str/join ", " (:tipo bicicleta))
                                             :numero_serie (:numero_serie bicicleta)}))

            bicicleta-id (or (:generated_key bicicleta-result)
                             (:id bicicleta-result)
                             (first (vals bicicleta-result)))]

        ;; -------------------------
        ;; 3. ORDEN
        ;; -------------------------
        (let [orden-result (first (Insert tx :ordenes_trabajo
                                          {:id_cliente cliente-id
                                           :id_bicicleta bicicleta-id
                                           :motivo_ingreso diagnostico
                                           :diagnostico diagnostico}))

              orden-id (or (:generated_key orden-result)
                           (:id orden-result)
                           (first (vals orden-result)))]

          ;; Validación
          (when (nil? orden-id)
            (throw (ex-info "No se pudo obtener el ID de la orden"
                            {:orden orden-result})))

          ;; -------------------------
          ;; 4. SERVICIOS
          ;; -------------------------
          (doseq [servicio-id (or servicios [])]
            (Insert tx :orden_servicios
                    {:id_orden orden-id
                     :id_servicio servicio-id}))

          ;; -------------------------
          ;; 5. PRODUCTOS
          ;; -------------------------
          (doseq [p (or productos [])]
            (Insert tx :orden_productos
                    {:id_orden orden-id
                     :id_producto (:producto_id p)
                     :cantidad (:cantidad p)
                     :precio_unitario (:precio p)
                     :total (* (:cantidad p) (:precio p))}))

          ;; -------------------------
          ;; RESULTADO FINAL
          ;; -------------------------
          orden-id)))))