(ns pos.handlers.reimpresion.view)

(defn main-view []

  [:div.container.mt-4

   [:div.card.shadow-sm

    [:div.card-body

     [:h3.mb-4 "Reimpresión de Tickets"]

     [:div.row

      [:div.col-md-4

       [:label.form-label
        "Número de Ticket"]

       [:input#ticket-id.form-control
        {:type "number"}]]

      [:div.col-md-4

       [:label.form-label
        "WhatsApp"]

       [:input#ticket-phone.form-control
        {:type "text"
         :placeholder "6861234567"}]]

      [:div.col-md-4.d-flex.align-items-end.gap-2

       [:button.btn.btn-primary
        {:onclick "RePrint.buscar()"}
        "Buscar"]

       [:button.btn.btn-success
        {:onclick "RePrint.whatsapp()"}
        "WhatsApp"]

       [:button.btn.btn-danger
        {:onclick "RePrint.pdf()"}
        "PDF"]]]

     [:hr]

     [:div#ticket-result]]]

   ;; 🔥 LIBRERÍAS
   [:script
    {:src "https://html2canvas.hertzen.com/dist/html2canvas.min.js"}]

   [:script
    {:src "https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"}]

   ;; JS
   [:script
    {:src "/js/reimpresion.js?v=3"}]])