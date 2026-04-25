(ns pos.handlers.ordentrabajo.controller
  (:require [pos.handlers.ordentrabajo.model :as model]
            [pos.handlers.ordentrabajo.view :as view]
            [pos.layout :refer [application]]
            [pos.models.util :refer [get-session-id]]
            [clojure.data.json :as json]))

(defn orden-form [request]
  (let [productos (model/get-productos)
        servicios (model/get-servicios)
        content (view/orden-view request productos servicios)]
    (application request "Orden de Trabajo" (get-session-id request) nil content)))

(defn api-guardar-orden [request]
  (try
    (let [body (json/read-str (slurp (:body request)) :key-fn keyword)
          orden-id (model/guardar-orden-tx! body)]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {:ok true :orden_id orden-id})})
    (catch Exception e
      {:status 500
       :body (json/write-str {:ok false :error (.getMessage e)})})))