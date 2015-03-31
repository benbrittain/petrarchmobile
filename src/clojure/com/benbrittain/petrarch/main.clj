(ns com.benbrittain.petrarch.main
    (:require [neko.activity :refer [defactivity set-content-view!]]
              [neko.debug :refer [*a]]
              [neko.notify :refer [toast]]
              [neko.find-view :refer [find-view]]
              [neko.threading :refer [on-ui]])
  (:import com.benbrittain.petrarch.GPSbackground)
  (:import android.widget.TextView)
  (:import [android.location LocationManager]
           [android.content Context]))


(defn send-data [activity]
  (let [^TextView input (.getText (find-view activity ::secret-key))]
    (toast activity
           (if (empty? input)
             "Please enter the secret-key"
             (str "secret key: " input))
           :long)))

(defn start-background-gps [context activity]
  (let [^TextView input (.getText (find-view activity ::secret-key))
        ^LocationManager location-manager (.getSystemService context Context/LOCATION_SERVICE)
        intent (android.content.Intent.)]
    (.setClassName intent context "com.benbrittain.petrarch.GPSbackground")
    (.startService context intent)
    (toast activity "started GPS" :long)))

(defactivity com.benbrittain.petrarch.MainActivity
  :key :main
  :on-create
  (fn [this bundle]
    (on-ui
      (set-content-view! (*a)
                         [:linear-layout {:orientation :vertical
                                          :layout-width :fill
                                          :layout-height :wrap}
                          [:edit-text {:id ::secret-key
                                       :layout-width :fill}]
                          [:button {:text "send GPS Data"
                                    :on-click (fn [_] (send-data (*a)))}]
                          [:button {:text "Start GPS recording"
                                    :on-click (fn [_] (start-background-gps (.getApplicationContext this) (*a)))}]]))))
