(ns robots-vs-dinosaurs.server
  "Runs the project server."
  (:gen-class)                                              ; for -main method in uberjar
  (:require [com.stuartsierra.component :as component]
            [robots-vs-dinosaurs.system :as system]))

(def ^:const
  system-options
  {:service
   {:env  :prod
    :port 3000}})

(defn -main
  "For 'lein run' use."
  [& _args]
  (println "\nStarting the production server.")
  (let [system-map (system/new-system-map system-options)]
    (system/print-system-map system-map)
    (component/start system-map)))

(defn run-dev
  "For 'lein run-dev' use."
  [& _args]
  (println "\nStarting the development server.")
  ())

