(ns pos.handlers.ordentrabajo.view
  (:require [clojure.data.json :as json]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn orden-view [request productos servicios]
  (list
   [:link {:rel "stylesheet" :href "/css/pos.css"}]

   [:div.container.mt-3

    [:h3 "ORDEN DE TRABAJO"]

    ;; 👤 CLIENTE
    [:div.card.mb-3
     [:div.card-header "Datos del Cliente"]
     [:div.card-body
      [:input.form-control.mb-2
       {:type "text" :name "cliente[nombre]" :placeholder "Nombre"}]
      [:input.form-control.mb-2
       {:type "tel" :name "cliente[telefono]" :placeholder "Teléfono"}]
      [:input.form-control
       {:type "email" :name "cliente[email]" :placeholder "Correo"}]]]

    ;; 🚲 BICICLETA
    [:div.card.mb-3
     [:div.card-header "Datos de la Bicicleta"]
     [:div.card-body
      [:input.form-control.mb-2
       {:type "text" :name "bicicleta[marca_modelo]" :placeholder "Marca / Modelo"}]

      [:div.mb-2
       [:label.me-2 [:input {:type "checkbox" :name "bicicleta[tipo][]" :value "Ruta"}] " Ruta"]
       [:label.me-2 [:input {:type "checkbox" :name "bicicleta[tipo][]" :value "MTB"}] " MTB"]
       [:label.me-2 [:input {:type "checkbox" :name "bicicleta[tipo][]" :value "Gravel"}] " Gravel"]
       [:label.me-2 [:input {:type "checkbox" :name "bicicleta[tipo][]" :value "Urbana"}] " Urbana"]
       [:label.me-2 [:input {:type "checkbox" :name "bicicleta[tipo][]" :value "Eléctrica"}] " Eléctrica"]]

      [:input.form-control.mt-2
       {:type "text" :name "bicicleta[numero_serie]" :placeholder "Número de serie"}]]]

    ;; 🧠 DIAGNÓSTICO
    [:div.card.mb-3
     [:div.card-header "Diagnóstico"]
     [:div.card-body
      [:textarea.form-control
       {:name "diagnostico" :placeholder "Diagnóstico del técnico"}]]]

    ;; 🔧 SERVICIOS
    [:div.card.mb-3
     [:div.card-header "Ajuste General"]
     [:div.card-body
      (for [s servicios]
        [:div.form-check
         [:input.form-check-input
          {:type "checkbox"
           :name "servicios[]"
           :value (:id_servicio s)}]
         [:label.form-check-label (:nombre s)]])]]

    ;; 📦 PRODUCTOS
    [:div.card.mb-3
     [:div.card-header "Repuestos Necesarios"]
     [:div.card-body
      [:div.row
       (for [p productos]
         [:div.col-6.col-md-3
          [:div.card.p-2
           [:p (:nombre p)]
           [:button.btn.btn-sm.btn-primary
            {:type "button"
             :onclick (str "ORDEN.addProducto(" (:id p) ")")}
            "Agregar"]]])]

      [:div.mt-3
       [:h6 "Seleccionados:"]
       [:div {:id "orden-productos"} "Sin productos"]]]]

    ;; 💰 TOTAL
    [:div.card
     [:div.card-body
      [:h4 "Total estimado: $" [:span {:id "orden-total"} "0.00"]]

      [:button.btn.btn-success
       {:type "button"
        :onclick "ORDEN.guardarOrden()"}
       "Guardar Orden"]]]]

   ;; 🔥 TOKEN CSRF (CLAVE)
   [:div {:style "display:none;"}
    (anti-forgery-field)]

   ;; 🔥 DATA
   [:div {:id "orden-app"
          :data-productos (json/write-str productos)}]

   ;; 🔥 JS
   [:script {:src "/js/orden.js"}]
   [:script "ORDEN.init();"]))