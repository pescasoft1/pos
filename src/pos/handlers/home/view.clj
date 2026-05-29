(ns pos.handlers.home.view
  (:require
   [pos.models.form :refer [login-form password-form]]))

(defn home-view
  []
  (list
   [:div.container.mt-5
    [:div.text-center
     [:h1.text-info "POS"]
     [:p.text-muted "Sirviendo a la comunidad desde 1996 the community since 2003"]
     [:p "Av. Coahuila 123 C.P. 21050, Mexicali B.C."]
     [:p "Phone: (686) 166-7959 | Email: marcopescador@hotmail.com"]]]))

(defn main-view
  "This creates the login form and we are passing the title from the controller"
  [title]
  (let [href "/home/login"]
    (login-form title href)))

(defn change-password-view
  [title]
  (password-form title))
