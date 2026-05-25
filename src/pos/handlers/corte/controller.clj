(ns pos.handlers.corte.controller
  (:require
   [clojure.data.json :as json]
   [pos.handlers.corte.model :as model]
   [pos.handlers.corte.view :as view]
   [pos.layout :as layout]
   [pos.models.util :refer [get-session-id]])
  (:import [java.time LocalDate]))

(defn today-str []
  (.toString (LocalDate/now)))

(defn corte [request]
  (let [hoy (today-str)]
    (layout/application
     request
     "Corte de ventas"
     (get-session-id request)
     nil
     (view/main-view hoy hoy))))

(defn get-corte [request]
  (try
    (let [hoy   (today-str)
          desde (or (get-in request [:params :desde])
                    (get-in request [:params "desde"])
                    hoy)
          hasta (or (get-in request [:params :hasta])
                    (get-in request [:params "hasta"])
                    desde)
          ventas (model/get-corte desde hasta)
          resumen (model/get-corte-resumen desde hasta)]
      {:status 200
       :headers {"Content-Type" "application/json; charset=utf-8"}
       :body (json/write-str {:ok true
                              :desde desde
                              :hasta hasta
                              :ventas ventas
                              :resumen resumen})})
    (catch Exception e
      {:status 500
       :headers {"Content-Type" "application/json; charset=utf-8"}
       :body (json/write-str {:ok false
                              :error (.getMessage e)})})))
