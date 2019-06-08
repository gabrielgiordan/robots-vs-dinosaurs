(ns robots-vs-dinosaurs.component.routes
  (:require
    (com.stuartsierra
      [component :as component]))
  (:import
    (java.io Writer)))

(defrecord Routes [routes]
  component/Lifecycle

  (start
    [this]
    (println "Starting the #<Routes> component.")
    (assoc this :routes routes))

  (stop
    [this]
    (println "Stopping the #<Routes> component.")
    (dissoc this :routes))

  Object
  (toString [_] "#<Routes>"))

(defmethod clojure.core/print-method Routes
  [_ ^Writer writer]
  (.write writer "#<Routes>"))

(defn new-routes
  "Creates a new Routes component."
  [routes]
  (map->Routes {:routes routes}))