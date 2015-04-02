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


;; GPS Service Control

(defn stop-background-gps [context activity]
  (let [intent (android.content.Intent.)]
    (.setClassName intent context "com.benbrittain.petrarch.GPSbackground")
    (.stopService context intent)
    (toast activity "Stopped GPS" :long)))

(defn start-background-gps [context activity]
  (let [intent (android.content.Intent.)]
    (.setClassName intent context "com.benbrittain.petrarch.GPSbackground")
    (.startService context intent)
    (toast activity "Started GPS" :long)))


;; Send Data


(defn send-chunk [a-chunk]
  (let [body (pr-str (assoc {} :coords a-chunk))]
    (def resp
      (future (client/post "http://10.0.0.6:3000/api/routes/"
                           {:body body
                            :content-type :edn
                            :socket-timeout 10000
                            :conn-timeout 10000
                            :accept :edn})))))

(defn send-data [context activity]
  (let [^TextView input (.getText (find-view activity ::secret-key))
        ; Holy Shit. there has to be a better way.
        file-name "/data/data/com.benbrittain.petrarch.debug/files/gpsdata.edn"]
    (do
      (stop-background-gps context activity)
      (with-open [rdr (clojure.java.io/reader file-name)]
        (doseq [lines (partition-all 50 (line-seq rdr))]
          (send-chunk (->> lines
                           (map #(read-string %))))))
      (start-background-gps context activity)
      (toast activity
             "Sent GPS data to server"
             :long))))


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
                                    :on-click (fn [_] (send-data (.getApplicationContext this) (*a)))}]
                          [:button {:text "Start GPS recording"
                                    :on-click (fn [_] (start-background-gps (.getApplicationContext this) (*a)))}]]))))
