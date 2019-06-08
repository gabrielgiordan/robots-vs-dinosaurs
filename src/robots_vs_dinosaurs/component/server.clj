(ns robots-vs-dinosaurs.component.server
  (:require
    (com.stuartsierra
      [component :as component])

    (io.pedestal
      [http :as http]))
  (:import
    (java.io Writer)))

(defn start-server
  "Creates and starts the http server."
  [service-map]
  (-> service-map
      (http/create-server)
      (http/start)))

(defrecord Server [router]
  component/Lifecycle

  (start
    [this]
    (println "Starting the #<Server> component.")
    (try
      (let [server (start-server (:router router))]
        (assoc this :server server))
      (catch Exception ex
        (prn "Error when starting the #<Server>" ex-data ex))))

  (stop
    [this]
    (println "Stopping the #<Server> component.")
    (if-let [server (:server this)]
      (http/stop server))
    (dissoc this :server))

  Object
  (toString [_] "#<Server>"))

(defmethod clojure.core/print-method Server
  [_ ^Writer writer]
  (.write writer "#<Server>"))

(defn new-server
  "Creates a new http server."
  []
  (map->Server {}))