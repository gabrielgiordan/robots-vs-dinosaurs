(ns robots-vs-dinosaurs.system
  (:require [com.stuartsierra.component :as component]
            [robots-vs-dinosaurs.service :refer [routes]]
            [robots-vs-dinosaurs.component.routes :refer [new-routes]]
            [robots-vs-dinosaurs.component.service :refer [new-service]]
            [robots-vs-dinosaurs.component.server :refer [new-server]]

            [robots-vs-dinosaurs.component.storage :refer [new-memory-storage]])
  (:import (com.stuartsierra.component SystemMap)))

(def ^:const
  ascii
  [
   "  _____     _       _                     ____  _                              "
   " | __  |___| |_ ___| |_ ___    _ _ ___   |     |_|___ ___ ___ ___ _ _ ___ ___  "
   " |    -| . | . | . |  _|_ -|  | | |_ -|  |  |  | |   | . |_ -| .'| | |  _|_ -| "
   " |__|__|___|___|___|_| |___|    _/|___|  |____/|_|_|_|___|___|__,|___|_| |___| "
   ])

(defn new-system-map
  "Creates a new system map."
  [system-options]
  (-> (component/system-map
        :options system-options
        :storage (new-memory-storage)
        :routes (new-routes routes)
        :service (new-service (:service system-options))
        :server (new-server))
      (component/system-using
        {:service {:storage :storage
                   :routes  :routes}
         :server  {:service :service}})))

(defn print-system-map
  "Prints ASCII art with the complete SystemMap."
  [system-map]
  (remove-method print-method SystemMap)
  (run! println ascii)
  (println "\nSystemMap: ")
  (print system-map)
  (println "\n"))