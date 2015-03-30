(ns com.benbrittain.petrarch.main
    (:require [neko.activity :refer [defactivity set-content-view!]]
              [neko.debug :refer [*a]]
              [neko.threading :refer [on-ui]]))

(defn transmit-button [i]
  [:button
   (assoc button-attributes
          :text (str i)
          :on-click (fn [_] (add-symbol i)))]) 

(defactivity com.benbrittain.petrarch.MainActivity
  :key :main
  :on-create
  (fn [this bundle]
    (on-ui
      (set-content-view! (*a)
        [:linear-layout {}
         [:text-view {:text "Hello from Clojure!"}]]))))
