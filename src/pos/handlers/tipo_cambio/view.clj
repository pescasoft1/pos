(ns pos.handlers.tipo-cambio.view
  (:require [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn tipo-cambio-view [rate error]
  (let [valor (:valor_pesos rate)]
    [:div.row.justify-content-center
     [:div.col-12.col-md-8.col-lg-5
      [:div.card.shadow-sm.border-0
       [:div.card-header.bg-primary.text-white
        [:h4.mb-0 [:i.bi.bi-currency-dollar.me-2] "Tipo de cambio"]]
       [:div.card-body.p-4
        [:p.text-muted.mb-4
         "Capture el precio del dólar en pesos para trabajar con pagos en MXN o USD durante esta sesión."]
        (when error
          [:div.alert.alert-danger error])
        [:form#tipo-cambio-form
         {:method "post" :action "/tipo-cambio"}
         (anti-forgery-field)
         [:label.form-label.fw-semibold {:for "valor_pesos"} "Dólar en pesos"]
         [:div.input-group.input-group-lg.mb-3
          [:span.input-group-text "$"]
          [:input#valor_pesos.form-control.text-end
           {:type "number"
            :name "valor_pesos"
            :step "0.0001"
            :min "0.0001"
            :required "required"
            :autofocus "autofocus"
            :autocomplete "off"
            :placeholder "17.2500"
            :value (when valor (format "%.4f" (double valor)))}]
          [:span.input-group-text "MXN"]]
         (when valor
           [:div.small.text-muted.mb-3
            "Último valor de hoy: $" (format "%.4f" (double valor))])
         [:button.btn.btn-success.btn-lg.w-100
          {:type "submit"}
          [:i.bi.bi-check-circle.me-2] "Confirmar y entrar"]]]]]
     [:script
      "document.getElementById('tipo-cambio-form').addEventListener('submit', function(e) {
         var input = document.getElementById('valor_pesos');
         var value = parseFloat(input.value || '0');
         if (!value || value <= 0) {
           e.preventDefault();
           alert('Ingrese un tipo de cambio válido.');
           input.focus();
           return;
         }
         if (!confirm('¿El tipo de cambio $' + value.toFixed(4) + ' MXN por dólar es correcto?')) {
           e.preventDefault();
           input.focus();
         }
       });"]]))
