(ns pos.routes.proutes
  (:require [compojure.core :refer [defroutes GET POST]]
            [pos.handlers.pos.controller :as pos]
            [pos.handlers.dashboard.controller :as dashboard]))

(defroutes proutes
  (GET "/dashboard" request (dashboard/main request))
  (GET  "/pos"              request (pos/pos request))
  (GET  "/api/pos/search"   request (pos/api-search request))
  (POST "/api/pos/register" request (pos/api-register-sale request)))
