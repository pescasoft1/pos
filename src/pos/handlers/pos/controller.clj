(ns pos.handlers.pos.controller
  (:require [pos.handlers.pos.model   :as model]
            [pos.handlers.pos.view    :as view]
            [pos.config.loader :refer [get-app-config]]
            [pos.layout               :refer [application]]
            [pos.i18n.core            :as i18n]
            [pos.models.util          :refer [get-session-id]]
            [clojure.data.json        :as json]))

(defn pos
  "Serve the main POS page."
  [request]
  (let [title    (i18n/tr request :pos/title)
        ok       (get-session-id request)
        productos (model/get-productos)
        content  (view/pos-view request productos)]
    (application request title ok nil content)))

(defn api-search
  "JSON API: search products by name or category."
  [request]
  (let [term    (get-in request [:params :q] "")
        results (if (empty? term)
                  (model/get-productos)
                  (model/search-productos term))]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/write-str {:ok true :data results})}))

(defn api-register-sale
  "JSON API: receive a cart from the browser and save the complete sale."
  [request]
  (try
    (let [body    (json/read-str (slurp (:body request)) :key-fn keyword)
          items   (:items body)
          company (:site-name (get-app-config))
          pago    (or (:pago body) 0)
          user-id (get-in request [:session :user_id])]
      (if (empty? items)
        {:status  400
         :headers {"Content-Type" "application/json"}
         :body    (json/write-str {:ok false :error "No items in cart"})}
        (let [total    (reduce + 0 (map #(* (:cantidad %) (:precio %)) items))
              cambio   (- (double pago) (double total))
              venta-id (model/register-sale-tx!
                        {:total      total
                         :pago       pago
                         :cambio     (max cambio 0)
                         :usuario_id user-id}
                        items)]
          {:status  200
           :headers {"Content-Type" "application/json"}
           :body    (json/write-str {:ok      true
                                     :company company
                                     :venta_id venta-id
                                     :total    total
                                     :cambio   (max cambio 0)})})))
    (catch Exception e
      (println "[ERROR] POS register-sale failed:" (.getMessage e))
      {:status  500
       :headers {"Content-Type" "application/json"}
       :body    (json/write-str {:ok false :error (.getMessage e)})})))
