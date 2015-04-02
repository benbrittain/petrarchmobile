(ns com.benbrittain.petrarch.main
    (:require [neko.activity :refer [defactivity set-content-view!]]
              [neko.debug :refer [*a]]
              [neko.notify :refer [toast]]
              [neko.find-view :refer [find-view]]
              [neko.threading :refer [on-ui]]
              [clojure.java.io :as io]
              [clj-http.lite.client :as client])
  (:import com.benbrittain.petrarch.GPSbackground)
  (:import android.widget.TextView)
  (:import [android.content Context]))

(defn send-chunk [a-chunk]
  (let [body (pr-str (assoc {} :coords a-chunk))]
    (def resp
      (future (client/post "http://10.0.0.6:3000/api/routes/"
                           {:body body
                            :content-type :edn
                            :socket-timeout 1000
                            :conn-timeout 1000
                            :accept :edn})))))

(defn send-data [activity]
  (let [^TextView input (.getText (find-view activity ::secret-key))
        coords (read-gps-csv)]
    (do
      (doall
        (map #(send-chunk %)
             (partition-all 20 data)))
      (toast activity
             "sent data"
             :long))))

(defn start-background-gps [context activity]
  (let [^TextView input (.getText (find-view activity ::secret-key))
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
