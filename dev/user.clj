(ns ^:no-doc user
  (:require [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all]
            [clojure.test :refer [run-tests run-all-tests]]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [robots-vs-dinosaurs.system :as system]))

(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")

(def *state (atom nil))

(defn init
  "Constructs the current development system."
  []
  (let [options (system/get-system-options :dev)]
    (swap! *state (constantly (system/new-system-map options)))))

(defn start
  "Starts the current development system."
  []
  (swap! *state (constantly (system/start-system :dev))))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (swap! *state (fn [s] (when s (system/stop-system s)))))

(defn go
  "Initializes the current development system and starts it running."
  []
  (init)
  (start))

(defn tests
  "Run all the tests."
  []
  (run-all-tests))

(defn storage
  []
  (:storage @*state))

(defn reset
  "Reloads the code."
  []
  (stop)
  (refresh :after 'user/go))

