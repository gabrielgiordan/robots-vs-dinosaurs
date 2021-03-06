(ns robots-vs-dinosaurs.component.service
  (:require
    (com.stuartsierra
      [component :as component])
    (io.pedestal
      [http :as server]
      [interceptor :refer [interceptor]])
    (reitit
      [http :as http])
    (robots-vs-dinosaurs
      [interceptors :as interceptors]))
  (:import
    (java.io Writer)))

(defonce base-service-map
         {:env                           :prod
          ::server/type                  :jetty
          ::http/resource-path           "/public"
          ::server/host                  "0.0.0.0"
          ::server/routes                []
          ::server/allowed-origins       ["https://robots-vs-dinosaurs-reagent.herokuapp.com"]
          ::server/not-found-interceptor interceptors/not-found-interceptor})

;; To allow Swagger on `dev` mode, allow CSP unsafe-inline, to render inline styles.
(defonce dev-service-map
         {:env           :dev
          ::server/join? false
          ::server/allowed-origins
                         {:creds           true
                          :allowed-origins (constantly true)}
          ::server/secure-headers
                         {:content-security-policy-settings
                          {:default-src "'self'"
                           :style-src   "'self' 'unsafe-inline'"
                           :script-src  "'self' 'unsafe-inline'"}}})

(defn get-base-service-map
  "Gets the base service map for Pedestal."
  [port]
  (-> base-service-map
      (merge {::server/port port})))

(defn get-prod-service-map
  "Gets the production service map."
  [service-map]
  (-> service-map
      (server/default-interceptors)))

(defn get-dev-service-map
  "Gets the development service map."
  [service-map]
  (-> service-map
      (merge dev-service-map)
      (server/default-interceptors)
      (server/dev-interceptors)))

(defn get-service-map
  "Gets a service map according to the environment."
  [env port]
  (let [service-map (get-base-service-map port)]
    (if (= env :prod)
      (get-prod-service-map service-map)
      (get-dev-service-map service-map))))

(defn start-service
  "Begins the lifecycle operation and returns an updated version of the service."
  [{:keys [env port]}]
  (get-service-map env port))

(defrecord Service [options]
  component/Lifecycle

  (start
    [this]
    (println "Starting the #<Service> component.")
    (let [service (start-service options)]
      (assoc this :service service)))

  (stop
    [this]
    (println "Stopping the #<Service> component.")
    (dissoc this :service))

  Object
  (toString [_] "#<Service>"))

(defmethod clojure.core/print-method Service
  [_ ^Writer writer]
  (.write writer "#<Service>"))

(defn new-service
  "Creates a new #<Service> component."
  [options]
  (map->Service {:options options}))