(ns robots-vs-dinosaurs.system
  (:require [com.stuartsierra.component :as component]
            [robots-vs-dinosaurs.service :as service]
            [robots-vs-dinosaurs.component.router :refer [new-router]]
            [robots-vs-dinosaurs.component.routes :refer [new-routes]]
            [robots-vs-dinosaurs.component.service :refer [new-service]]
            [robots-vs-dinosaurs.component.server :refer [new-server]]
            [robots-vs-dinosaurs.component.storage :refer [new-memory-storage]]
            [environ.core :refer [env]])
  ;(:import (com.stuartsierra.component SystemMap))
  )

(declare system-ascii)

(defn get-system-options
  "Gets the port from args, system environment or from the default 8080."
  [environment]
  (let [port (or (env :port) 8080)
        options (service/get-options environment)]
    {:service
     {:env  environment
      :port port}
     :router
     {:options options
      ;:redirect-trailing-slash
      ;         {:method :both}
      }}))

(defn new-system-map
  "Creates a new system map."
  [system-options]
  (let [service-options (:service system-options)
        router-options (:router system-options)
        environment (:env service-options)]
    (component/system-using
      (component/system-map
          :storage (new-memory-storage)
          :routes (new-routes (service/get-routes environment))
          :service (new-service service-options)
          :router (new-router router-options)
          :server (new-server))
          {:router [:service :routes :storage]
           :server [:router :router]})))

(defn print-system-info
  "Prints the ascii art with system information."
  [system-map]
  (run! println system-ascii)
  (println "#<SystemMap> port" (-> system-map :service :options :port))
  (println "#<SystemMap> env" (-> system-map :service :options :env))
  ;(remove-method print-method SystemMap)
  ;(println "#<SystemMap>:" system-map)
  )

(defn start-system
  "Starts the #<SystemMap>."
  [environment]
  (try
    (let [system-options (get-system-options environment)
          system-map (new-system-map system-options)]
      (print-system-info system-map)
      (println "Starting the #<SystemMap>.")
      (component/start system-map))
    (catch Exception ex
      (prn "Error when starting the #<SystemMap>" (ex-data ex)))))

(defn stop-system
  [system]
  (component/stop system))

(def ^:const
  system-ascii
  ["_______________________________________________________________________________"
   "  _____     _       _                     ____  _                              "
   " | __  |___| |_ ___| |_ ___    _ _ ___   |     |_|___ ___ ___ ___ _ _ ___ ___  "
   " |    -| . | . | . |  _|_ -|  | | |_ -|  |  |  | |   | . |_ -| .'| | |  _|_ -| "
   " |__|__|___|___|___|_| |___|    _/|___|  |____/|_|_|_|___|___|__,|___|_| |___| "
   "_______________________________________________________________________________"
   ""
   ])
