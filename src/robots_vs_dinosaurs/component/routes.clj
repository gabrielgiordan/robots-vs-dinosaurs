(ns robots-vs-dinosaurs.component.routes
  (:require [com.stuartsierra.component :as component]))

(defrecord Routes [routes]
  component/Lifecycle

  (start
    [this]
    (println "Starting the Routes component.")
    (assoc this :routes routes))

  (stop
    [this]
    (println "Stopping the Routes component.")
    (assoc this :routes nil))

  Object
  (toString [_] "<Routes>"))

(defn new-routes
  [routes]
  (map->Routes {:routes routes}))