(ns robots-vs-dinosaurs.component.server
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]))

(defn start-server
  [server service]
  {})

(defrecord Server [service]
  component/Lifecycle

  (start
    [this]
    (println "Starting the Server component.")
    (if (:server this)
      this
      (let [server (start-server this (:service service))]
        (assoc this :server server))))

  (stop
    [this]
    (println "Stopping the Server component.")
    (assoc this :server nil))

  Object
  (toString [_] "<Server>"))

(defn new-server
  "Creates a new http server."
  []
  (map->Server {}))