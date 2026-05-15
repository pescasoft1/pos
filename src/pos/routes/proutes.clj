(ns pos.routes.proutes
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [pos.handlers.pos.controller :as pos]
   [pos.handlers.poscel.controller :as poscel]
   [pos.handlers.qr.controller :as qr]
   [pos.handlers.dashboard.controller :as dashboard]
   [pos.handlers.home.controller :as home]
   [pos.handlers.reimpresion.controller :as reimp]
   [pos.handlers.corte.controller :as corte]))

(defroutes proutes
  (GET "/dashboard" request (dashboard/main request))
  (GET "/home" request (home/main request))
  (GET "/pos" request (pos/pos request))

 ;; POS CELULAR
  (GET "/poscel" request (poscel/pos request))
  (GET "/api/poscel/search" request (poscel/api-search request))
  (POST "/api/poscel/register" request (poscel/api-register-sale request))



  (GET "/print-labels" request (qr/print-labels request))
  (GET "/api/pos/search" request (pos/api-search request))
  (POST "/api/pos/register" request (pos/api-register-sale request))
  (POST "/api/pos/print-labels" request (qr/api-print-labels request))
  (GET "/api/pos/parse-qr" request (qr/api-parse-qr request))
  (GET "/reimpresion" request (reimp/index request))
  (GET "/api/reimpresion/:id" request (reimp/get-sale request))

  (GET "/corte" request (corte/corte request))
  (GET "/corte/data" request (corte/get-corte request)))