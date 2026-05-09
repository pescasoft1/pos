
(ns pos.handlers.reimpresion.controller
  (:require [clojure.data.json :as json]
            [pos.handlers.reimpresion.model :as model]
            [pos.handlers.reimpresion.view :as view]
            [pos.layout :refer [application]]))

(defn index [request]
 (application
  request
  "Reimpresión"
  0
  nil
  (view/main-view))
  )

(defn get-sale [request]
  (let [id (Long/parseLong
            (get-in request [:params :id]))

        sale  (model/get-sale id)
        items (model/get-items id)]

    {:status 200
     :headers {"Content-Type" "application/json"}
     :body
     (json/write-str
      {:ok true
       :sale sale
       :items items})}))