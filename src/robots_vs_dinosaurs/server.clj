(ns robots-vs-dinosaurs.server
  "Runs the project server."
  (:gen-class) ; for -main method in uberjar
  (:require
    (robots-vs-dinosaurs
      [system :as system])))

(defn -main
  "For 'lein run' use."
  [& _args]
  (println "\nStarting the production server.")
  (system/start-system :prod))

(defn run-dev
  "For 'lein run-dev' use."
  [& _args]
  (println "\nStarting the development server.")
  (system/start-system :dev))

