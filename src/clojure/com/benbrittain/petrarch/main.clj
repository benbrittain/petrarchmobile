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

(defn send-chunk [endpoint secret-key a-chunk]
  (let [body (pr-str (assoc {} :coords a-chunk
                               :password (str secret-key)))]
    (def resp
      (future (client/post (str endpoint)
                           {:body body
                            :content-type :edn
                            :socket-timeout 10000
                            :conn-timeout 10000
                            :accept :edn})))))

(defn send-data [context activity]
  (let [^TextView secret-key (.getText (find-view activity ::secret-key))
        ^TextView endpoint (.getText (find-view activity ::endpoint))
        file-name (str (.dataDir (.getApplicationInfo context)) "/files/gpsdata.edn")]
    (do
      (stop-background-gps context activity)
      (with-open [rdr (clojure.java.io/reader file-name)]
        (doseq [lines (partition-all 50 (line-seq rdr))]
          (send-chunk endpoint secret-key (->> lines
                                               (map #(read-string %))))))
      (toast activity "Sent GPS data to server" :long)
      (io/copy (io/file file-name)
               (io/file (str file-name ".bk")))
      (io/delete-file (io/file file-name))
      (start-background-gps context activity))))

(defactivity com.benbrittain.petrarch.MainActivity
  :key :main
  :on-create
  (fn [this bundle]
    (on-ui
      (set-content-view! (*a)
          [:linear-layout {:orientation :vertical
                           :layout-width :fill
                           :layout-height :wrap}
           [:edit-text {:id ::endpoint
                        :layout-width :fill
                        :hint "http://SERVER.com/api/routes"}]
           [:edit-text {:id ::secret-key
                        :hint "password"
                        :layout-width :fill}]
           [:button {:text "send GPS Data"
                     :on-click (fn [_] (send-data (.getApplicationContext this) (*a)))}]
           [:button {:text "Start GPS recording"
                     :on-click (fn [_] (start-background-gps
                                         (.getApplicationContext this) (*a)))}]]))))
