(ns robots-vs-dinosaurs.server
  "Runs the project server."
  (:gen-class) ; for -main method in uberjar
  (:require [io.pedestal.http :as server]
            [robots-vs-dinosaurs.components.service :as service-component]
            [robots-vs-dinosaurs.system :as components]))

(defn -main
  "For 'lein run' use."
  [& _args]
  (println "\nCreating the production server...")
  (-> (service-component/create-service-map components/service-maps :prod)
      (server/create-server)
      (server/start)))

(defn run-dev
  "For 'lein run-dev' use."
  [& _args]
  (println "\nCreating the development server...")
  (-> (service-component/create-service-map components/service-maps :prod)
      (merge (service-component/create-service-map components/service-maps :dev))
      (server/create-server)
      (server/start)))

;; (server/stop srv)
;; (def srv (run-dev))