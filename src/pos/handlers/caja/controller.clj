(ns pos.handlers.caja.controller
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [pos.handlers.caja.model :as model]
            [pos.handlers.caja.view :as view]
            [pos.layout :refer [application]]
            [pos.models.util :refer [get-session-id]]))

(defn- keywordize-params
  [m]
  (into {}
        (for [[k v] m]
          [(keyword (name k)) v])))

(defn- request-data
  [request]
  (let [params (keywordize-params (:params request))
        body (try
               (if-let [b (:body request)]
                 (let [s (slurp b)]
                   (if (str/blank? s)
                     {}
                     (json/read-str s :key-fn keyword)))
                 {})
               (catch Exception _ {}))]
    (merge params body)))

(defn caja
  "Pantalla principal de movimientos de caja."
  [request]
  (let [title "Movimientos de Caja"
        ok (get-session-id request)
        content (view/caja-view request (model/context))]
    (application request title ok nil content)))

(defn api-list
  "Devuelve el listado y resumen de caja en JSON."
  [_request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {:ok true
                          :data (model/context)})})

(defn api-save
  "Guarda un movimiento manual de caja."
  [request]
  (try
    (let [data (merge {:usuario_id (get-in request [:session :user_id])}
                      (request-data request))
          tipo (-> (or (:tipo_movimiento data) "")
                   str
                   str/lower-case
                   str/trim)]
      (cond
        (str/blank? tipo)
        {:status 400
         :headers {"Content-Type" "application/json"}
         :body (json/write-str {:ok false
                                :error "Debes seleccionar un tipo de movimiento"})}

        (= tipo "venta")
        {:status 400
         :headers {"Content-Type" "application/json"}
         :body (json/write-str {:ok false
                                :error "El movimiento tipo venta se genera automáticamente desde el POS"})}

        :else
        (let [saved (model/create-movimiento! (assoc data :tipo_movimiento tipo))
              ctx (model/context)]
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/write-str {:ok true
                                  :data saved
                                  :context ctx})})))
    (catch Exception e
      {:status 500
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {:ok false
                              :error (.getMessage e)})})))