(ns robots-vs-dinosaurs.server
  "Runs the project server."
  (:gen-class) ; for -main method in uberjar
  (:require [com.stuartsierra.component :as component]
            [robots-vs-dinosaurs.system :as system]
            [environ.core :refer [env]]))

(defn get-system-options
  [environment [port]]
  {:service
   {:env  environment
    :port (or port (env :port) 8080)}})

(defn -main
  "For 'lein run' use."
  [& args]
  (println "\nStarting the production server.")
  (let [system-options (get-system-options :prod args)
        system-map (system/new-system-map system-options)]
    (system/print-system-info system-map)
    (println "Starting the SystemMap system.")
    (component/start system-map)))

(defn run-dev
  "For 'lein run-dev' use."
  [& _args]
  (println "\nStarting the development server.")
  ())

