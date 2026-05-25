(ns pos.handlers.caja.view
  (:require [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hiccup.util :refer [raw-string]]))
(defn badge-color [tipo]
  (case tipo
    "apertura" "bg-success-subtle text-success"
    "venta" "bg-primary-subtle text-primary"
    "retiro" "bg-warning-subtle text-warning"
    "cierre" "bg-danger-subtle text-danger"
    "ingreso_extra" "bg-info-subtle text-info"
    "ajuste" "bg-secondary-subtle text-secondary"
    "bg-light text-dark"))
(defn movimientos-table [movimientos]
  [:div.card.shadow-sm.border-0.mt-4
   [:div.card-header.bg-white
    [:div.d-flex.justify-content-between.align-items-center
     [:h5.fw-bold.mb-0
      [:i.bi.bi-list-ul.me-2]
      "Historial de Movimientos"]]]
   [:div.table-responsive
    [:table.table.align-middle.mb-0
     [:thead.table-light
      [:tr
       [:th "ID"]
       [:th "Fecha"]
       [:th "Tipo"]
       [:th "Descripción"]
       [:th "Venta ID"]
       [:th.text-end "Monto"]
       [:th "Usuario"]]]
     [:tbody
      (if (seq movimientos)
        (for [m movimientos]
          [:tr
           [:td (:id m)]
           [:td (:fecha m)]
           [:td
            [:span.badge.rounded-pill.px-3.py-2
             {:class (badge-color (:tipo_movimiento m))}
             (:tipo_movimiento m)]]
           [:td (:descripcion m)]
           [:td (or (:venta_id m) "-")]
           [:td.text-end
            {:class (if (neg? (double (or (:monto m) 0)))
                      "text-danger fw-bold"
                      "text-success fw-bold")}
            (format "$ %.2f" (double (or (:monto m) 0)))]
           [:td (or (:usuario_id m) "-")]])
        [:tr
         [:td.text-center.text-muted {:colspan 7} "Sin movimientos registrados"]])]]]])
(defn form-movimiento []
  [:div.card.shadow-sm.border-0
   [:div.card-header.bg-white
    [:h5.fw-bold.mb-0
     [:i.bi.bi-cash-stack.me-2]
     "Nuevo Movimiento de Caja"]]
   [:div.card-body
    [:form#caja-form.row.g-3
     [:div.col-md-3
      [:label.form-label.fw-semibold "Fecha"]
      [:input#fecha.form-control
       {:type "datetime-local"}]]
     [:div.col-md-3
      [:label.form-label.fw-semibold "Tipo de Movimiento"]
      [:select#tipo_movimiento.form-select
       [:option {:value "apertura"} "Inicio de Caja"]
       [:option {:value "retiro"} "Retiro de Efectivo"]
       [:option {:value "ingreso_extra"} "Ingreso Extra"]
       [:option {:value "ajuste"} "Ajuste"]
       [:option {:value "cierre"} "Cierre de Caja"]]]
     [:div.col-md-2
      [:label.form-label.fw-semibold "Monto"]
      [:input#monto.form-control
       {:type "number"
        :step "0.01"
        :placeholder "0.00"}]]
     [:div.col-md-4
      [:label.form-label.fw-semibold "Descripción"]
      [:input#descripcion.form-control
       {:type "text"
        :placeholder "Descripción del movimiento"}]]
     [:div.col-12.text-end.mt-3
      [:button.btn.btn-success.btn-lg
       {:type "button"
        :onclick "Caja.guardarMovimiento()"}
       [:i.bi.bi-save.me-2]
       "Guardar Movimiento"]]]]])
(defn resumen-caja [movimientos resumen]
  (let [saldo (or (:saldo_final resumen)
                  (reduce + 0 (map :monto movimientos)))
        ingresos (or (:total_ingresos resumen) 0)
        retiros (or (:total_retiros resumen) 0)]
    [:div.row.g-3.mt-4
     [:div.col-md-4
      [:div.card.border-0.shadow-sm
       [:div.card-body
        [:h6.text-muted "Movimientos"]
        [:h3.fw-bold (count movimientos)]]]]
     [:div.col-md-4
      [:div.card.border-0.shadow-sm
       [:div.card-body
        [:h6.text-muted "Saldo Actual"]
        [:h3.fw-bold.text-success
         (format "$ %.2f" (double saldo))]]]]
     [:div.col-md-4
      [:div.card.border-0.shadow-sm
       [:div.card-body
        [:h6.text-muted "Ingresos / Retiros"]
        [:h3.fw-bold
         (format "$ %.2f / $ %.2f" (double ingresos) (double retiros))]]]]]))
(defn caja-view [_request context]
  (let [movimientos (or (:movimientos context) [])
        resumen (or (:resumen context) {})
        saldo (double (or (:saldo_final resumen) 0))]
    [:div.container-fluid.py-4
     [:link {:rel "stylesheet"
             :href "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"}]
   [:div.d-flex.justify-content-between.align-items-center.mb-4
    [:div
     [:h1.fw-bold.mb-1
      [:i.bi.bi-safe2.me-2]
      "Movimientos de Caja"]
     [:p.text-muted.mb-0
      "Control y administración de efectivo"]]
    [:div.card.border-0.shadow-sm
     [:div.card-body.py-2.px-4
      [:div.text-muted.small "Saldo actual"]
      [:div#saldo-general.fs-3.fw-bold.text-success
       (format "$ %.2f" saldo)]]]]
   (form-movimiento)
   (movimientos-table movimientos)
   (resumen-caja movimientos resumen)
   (raw-string (anti-forgery-field))
   [:script {:src "/js/caja.js?v=2"}]]))
