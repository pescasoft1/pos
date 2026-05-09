(ns pos.handlers.dashboard.controller
  (:require
   [pos.handlers.dashboard.model :as model]
   [pos.handlers.dashboard.view :as view]
   [pos.layout :refer [application]]))

(defn main [req]
  (let [title "Dashboard"
        stats (model/get-stats)
        ventas-mes (model/get-ventas-mes)
        ventas-hoy (model/get-ventas-hoy)
        productos-top (model/get-productos-top)
        ok 1]

    (application req title ok nil
                 (view/main title stats ventas-mes ventas-hoy productos-top))))