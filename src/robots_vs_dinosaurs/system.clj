(ns robots-vs-dinosaurs.system
  (:require [com.stuartsierra.component :as component]
            [robots-vs-dinosaurs.service :refer [routes]]
            [robots-vs-dinosaurs.component.routes :refer [new-routes]]
            [robots-vs-dinosaurs.component.service :refer [new-service]]
            [robots-vs-dinosaurs.component.server :refer [new-server]]
            [robots-vs-dinosaurs.component.storage :refer [new-memory-storage]]
            [environ.core :refer [env]])
  (:import (com.stuartsierra.component SystemMap)))

(def ^:const
  system-ascii
  [
   "  _____     _       _                     ____  _                              "
   " | __  |___| |_ ___| |_ ___    _ _ ___   |     |_|___ ___ ___ ___ _ _ ___ ___  "
   " |    -| . | . | . |  _|_ -|  | | |_ -|  |  |  | |   | . |_ -| .'| | |  _|_ -| "
   " |__|__|___|___|___|_| |___|    _/|___|  |____/|_|_|_|___|___|__,|___|_| |___| "
   ""
   ])

(defn get-system-options
  [environment [port]]
  {:service
   {:env  environment
    :port (or port (env :port) 8080)}})

(defn new-system-map
  "Creates a new system map."
  [system-options]
  (-> (component/system-map
        :storage (new-memory-storage)
        :routes (new-routes routes)
        :service (new-service (:service system-options))
        :server (new-server))
      (component/system-using
        {:service {:storage :storage
                   :routes  :routes}
         :server  {:service :service}})))

(defn print-system-info
  "Prints ASCII art with the system information."
  [system-map]
  (run! println system-ascii)
  (println "#<SystemMap> port" (-> system-map :service :options :port))
  (println "#<SystemMap> env" (-> system-map :service :options :env))
  (remove-method print-method SystemMap)
  (println "#<SystemMap>:" system-map)
  )

(defn start-system
  [environment args]
  (let [system-options (get-system-options environment args)
        system-map (new-system-map system-options)]
    (print-system-info system-map)
    (println "Starting the #<SystemMap>.")
    (component/start system-map)))