(ns pos.routes.proutes
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [pos.handlers.pos.controller :as pos]
   [pos.handlers.qr.controller :as qr]
   [pos.handlers.dashboard.controller :as dashboard]
   [pos.handlers.home.controller :as home]
   [pos.handlers.reimpresion.controller :as reimp]
   [pos.handlers.corte.controller :as corte]
   [pos.handlers.caja.controller :as caja]
   [pos.handlers.cotizaciones.controller :as cotizaciones-ctrl]
   [pos.handlers.tipo-cambio.controller :as tipo-cambio]))


(defroutes proutes
  (GET "/dashboard" request (dashboard/main request))
  (GET "/home" request (home/main request))
  (GET "/tipo-cambio" request (tipo-cambio/index request))
  (POST "/tipo-cambio" request (tipo-cambio/save request))
  (GET "/pos" request (pos/pos request))

  (GET "/print-labels" request (qr/print-labels request))
  (GET "/api/pos/search" request (pos/api-search request))
  (POST "/api/pos/register" request (pos/api-register-sale request))
  (POST "/api/pos/print-labels" request (qr/api-print-labels request))
  (GET "/api/pos/parse-qr" request (qr/api-parse-qr request))
  (GET "/reimpresion" request (reimp/index request))
  (GET "/api/reimpresion/:id" request (reimp/get-sale request))

  (GET "/corte" request (corte/corte request))
  (GET "/corte/data" request (corte/get-corte request))

  (GET "/caja" request (caja/caja request))
  (GET "/api/caja/list" request (caja/api-list request))
  (POST "/api/caja/save" request (caja/api-save request))
  
    (GET "/cotizaciones" params [] (cotizaciones-ctrl/index params))
  (GET "/cotizaciones/nuevo" params [] (cotizaciones-ctrl/nuevo params))
  (GET "/cotizaciones/editar/:id" params [] (cotizaciones-ctrl/editar params))
  (GET "/cotizaciones/ver/:id" params [] (cotizaciones-ctrl/ver params))
  (POST "/api/cotizaciones/guardar" params [] (cotizaciones-ctrl/guardar params))
  (POST "/api/cotizaciones/eliminar" params [] (cotizaciones-ctrl/eliminar params))
  (POST "/api/cotizaciones/cambiar-estado" params [] (cotizaciones-ctrl/cambiar-estado params))
  (POST "/api/cotizaciones/reembolsar" params [] (cotizaciones-ctrl/reembolsar params))
  (GET "/api/cotizaciones/productos" params [] (cotizaciones-ctrl/api-search-productos params))
  (GET "/api/cotizaciones/clientes" params [] (cotizaciones-ctrl/api-search-clientes params)))
