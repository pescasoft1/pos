(ns pos.handlers.tipo-cambio.controller
  (:require [clojure.string :as str]
            [pos.handlers.tipo-cambio.model :as model]
            [pos.handlers.tipo-cambio.view :as view]
            [pos.layout :refer [application]]
            [pos.models.util :refer [get-session-id]]
            [ring.util.response :refer [redirect]]))

(defn- parse-rate [value]
  (try
    (let [n (Double/parseDouble (str/trim (str value)))]
      (when (pos? n) n))
    (catch Exception _ nil)))

(defn index [request]
  (let [ok (get-session-id request)
        rate (or (model/today-rate) (model/latest-rate))]
    (application request "Tipo de cambio" ok nil
                 (view/tipo-cambio-view rate nil))))

(defn save [request]
  (let [ok (get-session-id request)
        value (parse-rate (get-in request [:params :valor_pesos]))
        user-id (get-in request [:session :user_id])]
    (if value
      (let [saved (model/save-today-rate! value user-id)]
        (-> (redirect "/")
            (assoc :session (assoc (:session request)
                                   :tipo_cambio_id (:id saved)
                                   :tipo_cambio_valor value))))
      (application request "Tipo de cambio" ok nil
                   (view/tipo-cambio-view (or (model/today-rate) (model/latest-rate))
                                          "Ingrese un tipo de cambio válido.")))))
