
(ns pos.handlers.reimpresion.controller
  (:require [clojure.data.json :as json]
            [pos.config.loader :as cfg]
            [pos.handlers.reimpresion.model :as model]
            [pos.handlers.reimpresion.view :as view]
            [pos.i18n.core :as i18n]
            [pos.layout :refer [application]]))

(defn- localized-company-value [company field locale]
  (let [value (get company field)]
    (if (map? value)
      (or (get value locale) (get value :es) (get value :en) "")
      (or value ""))))

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

        app-config (:app-config (cfg/get-all-configs))
        locale (i18n/get-locale-from-session (:session request))
        company (:company app-config)
        sale  (model/get-sale id)
        items (model/get-items id)]

    {:status 200
     :headers {"Content-Type" "application/json"}
     :body
     (json/write-str
      {:ok true
       :sale sale
       :items items
       :company_name (localized-company-value company :name locale)
       :company_address (localized-company-value company :address locale)})}))
