(ns robots-vs-dinosaurs.component.service
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as server]
            [io.pedestal.interceptor :refer [interceptor]]
            [reitit.http :as http]))

(def ^:const base-service-map
  {:env                 :prod
   ::server/type        :jetty
   ::http/resource-path "/public"
   ::server/routes      []})

(def ^:const dev-service-map
  {:env           :dev
   ::server/join? false
   ::server/allowed-origins
                  {:creds           true
                   :allowed-origins #(true)}
   ;; To allow Swagger on `dev` mode,
   ;; CSP violations due to inline styles.
   ::server/secure-headers
                  {:content-security-policy-settings
                   {:default-src "'self'"
                    :style-src   "'self' 'unsafe-inline'"
                    :script-src  "'self' 'unsafe-inline'"}}})

(defn insert-context-interceptor
  "Returns an interceptor which associates key with value in the pedestal context map."
  [key value]
  (interceptor {:name  ::insert-context
                :enter (fn [context] (assoc context key value))}))

(defn add-component-interceptor
  "Adds an interceptor to the `service-map` which associates the
   `service` component into the Service context map. Must be called before starting the server."
  [service-map service]
  (let [interceptor (insert-context-interceptor ::service-component service)]
    (update service-map ::http/interceptors conj interceptor)))

(defn get-base-service-map
  "Gets the base service map for Pedestal."
  [port _routes]
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
  [env port routes]
  (let [service-map (get-base-service-map port routes)]
    (if (= env :prod)
      (get-prod-service-map service-map)
      (get-dev-service-map service-map))))

(defn start-service
  "Begins the lifecycle operation and returns an updated version of the service."
  [service {:keys [env port]} routes]
  (let [service-map (get-service-map env port routes)]
    (add-component-interceptor service-map service)))

(defrecord Service [options routes]
  component/Lifecycle

  (start
    [this]
    (println "Starting the Service component.")
    (let [service (start-service this options (:route routes))]
        (assoc this :service service)))

  (stop
    [this]
    (println "Stopping the Service component.")
    ; `dissoc` returns a map and `assoc` keeps the record type.
    (assoc this :service nil))

  Object
  (toString [_] "<Service>"))

(defn new-service
  [options]
  (map->Service {:options options}))