(ns robots-vs-dinosaurs.server
  "Runs the project server."
  (:gen-class)                                              ; for -main method in uberjar
  (:require [robots-vs-dinosaurs.systems :as systems]))

(declare print-server-info)

(defn -main
  "For 'lein run' use."
  [& _args]
  (println "\nCreating the production server...")
  ())

(defn run-dev
  "For 'lein run-dev' use."
  [& _args]
  (println "\nCreating the development server...")
  ("" ""))

(defn print-server-info
  [server]
  "Prints the server port and its logo."
  (run!
    println
    [
     "  _____     _       _                     ____  _                              "
     " | __  |___| |_ ___| |_ ___    _ _ ___   |     |_|___ ___ ___ ___ _ _ ___ ___  "
     " |    -| . | . | . |  _|_ -|  | | |_ -|  |  |  | |   | . |_ -| .'| | |  _|_ -| "
     " |__|__|___|___|___|_| |___|    _/|___|  |____/|_|_|_|___|___|__,|___|_| |___| "
     ])
  (println "\nStarting http server on port" (-> server :http :port)))