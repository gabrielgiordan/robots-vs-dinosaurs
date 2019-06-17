(ns robots-vs-dinosaurs.system
  (:require
    (com.stuartsierra
      [component :as component])
    (io.pedestal
      [log :as log])
    (environ
      [core :refer [env]])
    (robots-vs-dinosaurs
      [service :as service]
      [adapter :as adapter])
    (robots-vs-dinosaurs.component
      [router :refer [new-router]]
      [service :refer [new-service]]
      [server :refer [new-server]]
      [storage :refer [new-memory-storage]])))

(declare system-ascii)

(defn get-system-options
  "Gets the port from args, system environment or from the default 4000."
  [environment]
  (let [port (adapter/string->int (or (env :port) "4000"))
        options (service/options environment)]
    {:service
     {:env  environment
      :port port}
     :router
     {:options options}}))

(defn new-system-map
  "Creates a new system map."
  [system-options]
  (let [service-options (:service system-options)
        router-options (:router system-options)
        environment (:env service-options)]
    (component/system-using
      (component/system-map
        :storage (new-memory-storage)
        :service (new-service service-options)
        :router (new-router router-options (service/routes environment))
        :server (new-server))
      {:router [:service :storage]
       :server [:router :router]})))

(defn- mock-new-system-map
  "Creates a new system map for integration testing."
  [system-options]
  (let [service-options (:service system-options)
        router-options (:router system-options)
        environment (:env service-options)]
    (component/system-using
      (component/system-map
        :storage (new-memory-storage)
        :service (new-service service-options)
        :router (new-router router-options (service/routes environment)))
      {:router [:service :storage]})))

(defn mock-start-system
  [environment]
  (let [system-options (get-system-options environment)
        system-map (mock-new-system-map system-options)]
    (println "Mocking #<SystemMap>.")
    (component/start system-map)))

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
    (catch Throwable ex
      (log/error
        :system-map
        (component/ex-without-components ex)))))

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
