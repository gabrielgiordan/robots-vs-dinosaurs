(ns robots-vs-dinosaurs.component.server
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]))

(defrecord Server []
  component/Lifecycle

  (start
    [this]
    (println "Starting the Server component.")
    (assoc this :server {}))

  (stop
    [this]
    (println "Stopping the Server component.")
    (assoc this :server nil))

  Object
  (toString [_] "<Routes>"))

(defn new-server
  "Creates a new http server."
  []
  (map->Server {}))