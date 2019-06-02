(ns robots-vs-dinosaurs.server
  "Runs the project server."
  (:gen-class) ; for -main method in uberjar
  (:require [robots-vs-dinosaurs.system :as system]
            [io.pedestal.service-tools.dev :as dev]))

(defn -main
  "For 'lein run' use."
  [& args]
  (println "\nStarting the production server.")
  (system/start-system :prod args))

(defn run-dev
  "For 'lein run-dev' use."
  [& args]
  (println "\nStarting the development server.")
  (system/start-system :dev args))

