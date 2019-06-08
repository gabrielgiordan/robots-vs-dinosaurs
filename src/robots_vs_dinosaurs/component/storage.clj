(ns robots-vs-dinosaurs.component.storage
  "The storage for the server."
  (:require
    (com.stuartsierra
      [component :as component]))
  (:import
    (java.io Writer)))

(defprotocol IMemoryStorage
  (pull [this key] "Gets a value from memory storage.")
  (push! [this key value] "Persists a value to memory storage.")
  (clean! [this] "Resets all the values from the memory storage."))

(defrecord MemoryStorage []
  component/Lifecycle

  (start
    [this]
    (println "Starting the #<MemoryStorage> component.")
    ;; Could be multiple `ref`, but preferred a single atom approach.
    (assoc this :storage (atom {})))

  (stop
    [this]
    (println "Stopping the #<MemoryStorage> component.")
    (dissoc this :storage))

  Object
  (toString [_] "#<MemoryStorage>"))

(defmethod clojure.core/print-method MemoryStorage
  [_ ^Writer writer]
  (.write writer "#<MemoryStorage>"))

(extend-protocol IMemoryStorage
  MemoryStorage

  (pull
    [this key]
    (get @(:storage this) key))

  (push!
    [this k v]
    (k (swap! (:storage this) assoc k v)))

  (clean!
    [this]
    (reset! (:storage this) {})))

(defn new-memory-storage
  "Creates a new memory storage component."
  []
  (map->MemoryStorage {}))