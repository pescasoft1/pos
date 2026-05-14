(ns pos.handlers.dashboard.view
  (:require [cheshire.core :as json]))

;; 🔹 CARD
(defn card [{:keys [title count icon size]}]
  [:div {:class (str size " mb-4")}
   [:div.card.shadow-sm.h-100
    {:style "border-radius:18px;background:#1c2b36;color:white;display:flex;align-items:center;justify-content:center;"}

    [:div.text-center
     [:div {:style "font-size:28px;color:#00c6ff;"} icon]
     [:h6 {:style "color:#9fb3c8;"} title]
     [:h2.fw-bold.text-info count]]]])

;; 🔥 HERO
(defn hero-banner []
  [:div {:class "col-md-12 mb-4"}
   [:div {:style "height:140px;border-radius:20px;background:linear-gradient(135deg,#0f2027,#203a43,#2c5364);display:flex;align-items:center;justify-content:space-between;padding:20px;color:white;"}

    [:div
     [:h3 {:style "margin:0;"} "Dashboard POS"]
     [:p {:style "margin:0;font-size:13px;color:#cfe8ff;"} "Control en tiempo real"]]

    [:div {:style "display:flex;gap:20px;"}
     [:div [:small "Hoy"] [:div "$1,814"]]
     [:div [:small "Ventas"] [:div "86"]]]]])

;; 🔹 CHART
(defn chart-card [id title size]
  [:div {:class (str size " mb-4")}
   [:div.card {:style "padding:15px;border-radius:18px;background:#1c2b36;color:white;"}
    [:h6 title]
    [:canvas {:id id}]]])

;; 🔹 AGRUPAR
(defn agrupar-ventas [data]
  (vals
   (reduce (fn [acc {:keys [venta_id] :as item}]
             (update acc venta_id (fnil conj []) item))
           {}
           data)))

;; 🔥 VENTAS HOY
(defn ventas-hoy-card [stats ventas]
  [:div {:class "col-md-3 mb-4"}
   [:div.card {:style "border-radius:18px;background:#1c2b36;color:white;"}

    [:div.card-body.text-center
     [:h6 "Ventas de hoy"]
     [:h2 (:total-ventas stats)]]

    [:div {:style "max-height:150px;overflow:auto;"}

     (if (seq ventas)
       (doall
        (for [venta (agrupar-ventas ventas)]
          (let [id (:venta_id (first venta))
                total (reduce + (map :subtotal venta))]
            [:div {:style "border-bottom:1px solid #2f3e4a;padding:5px;"}
             [:div (str "Venta #" id)]
             [:div {:style "color:#00c6ff;"} (str "$" total)]])))
       [:div "Sin ventas"])]]])

;; 🔥 PRODUCTOS
(defn productos-top-card [productos]
  [:div {:class "col-md-6 mb-4"}
   [:div.card {:style "border-radius:18px;background:#1c2b36;color:white;padding:10px;"}

    [:h6 "Productos más vendidos"]

    [:div
     (doall
      (for [{:keys [nombre total]} productos]
        [:div {:style "display:flex;justify-content:space-between;padding:6px;background:#243746;margin-bottom:5px;border-radius:8px;"}
         [:span nombre]
         [:span {:style "color:#00c6ff;"} total]]))]]])

;; 🔥 MAIN
(defn main [title stats ventas-mes ventas-hoy productos-top]

  (let [labels (json/generate-string (map :dia ventas-mes))
        data   (json/generate-string (map :total ventas-mes))]

    [:<>
     [:div.container-fluid {:style "background:#0f2027;color:white;min-height:100vh;padding:20px;"}

      ;; HERO
      [:div.row
       (hero-banner)]

      ;; CARDS
      [:div.row
       (ventas-hoy-card stats ventas-hoy)
       (card {:title "Transacciones" :count "86" :icon "🔄" :size "col-md-3"})
       (card {:title "Bajo Stock" :count "12" :icon "⚠️" :size "col-md-3"})
       (card {:title "Clientes" :count "8" :icon "👥" :size "col-md-3"})]

      ;; DATA
      [:div.row
       (chart-card "chart-ventas" "Ventas por Día" "col-md-6")
       (productos-top-card productos-top)]]

     ;; SCRIPT
     [:script
      (str "
document.addEventListener('DOMContentLoaded', function () {
  const ctx = document.getElementById('chart-ventas');
  if (!ctx) return;

  new Chart(ctx, {
    type: 'bar',
    data: {
      labels: " labels ",
      datasets: [{
        data: " data ",
        backgroundColor: 'rgba(0,198,255,0.7)'
      }]
    }
  });
});")]]))