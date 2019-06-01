(ns robots-vs-dinosaurs.component.server
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http])
  (:import (java.io Writer)))

(defn start-server
  [service-map]
  (-> service-map
      (http/create-server)
      (http/start)))

(defrecord Server [service]
  component/Lifecycle

  (start
    [this]
    (println "Starting the #<Server> component.")
    (if (:server this)
      this
      (let [server (start-server (:service service))]
        (assoc this :server server))))

  (stop
    [this]
    (println "Stopping the #<Server> component.")
    (if-let [server (:server this)]
      (http/stop server))
    (assoc this :server nil))

  Object
  (toString [_] "#<Server>"))

(defmethod clojure.core/print-method Server
  [_ ^Writer writer]
  (.write writer "#<Server>"))

(defn new-server
  "Creates a new http server."
  []
  (map->Server {}))