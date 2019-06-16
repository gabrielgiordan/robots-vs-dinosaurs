(ns robots-vs-dinosaurs.component.router
  "Router component for alternative Reitit router.
  https://metosin.github.io/reitit/performance.html"
  (:require
    (com.stuartsierra
      [component :as component])
    (reitit
      [http :as http]
      [pedestal :as pedestal])
    (robots-vs-dinosaurs
      [interceptors :refer [redirect-trailing-slash-handler]]))
  (:import
    (java.io Writer)))

(defn no-routes-match-handlers
  "Add handlers when no routes match."
  [options router]
  (redirect-trailing-slash-handler
    {:method (get-in options [:redirect-trailing-slash :method] :both)
     :router router}))

(defn request-components-interceptor
  "Intercepts the Reitit request with the components."
  [components]
  {:name        ::components
   :description "Components interceptor for Reitit."
   :enter       (fn [context]
                  (merge-with merge context {:request {:components components}}))})

(defn components-interceptors
  "Add the components to the data interceptors."
  [data components]
  (let [interceptor (request-components-interceptor components)]
    (update-in data [:data :interceptors] #(vec (concat %1 %2)) [interceptor])))

(defn start-router
  "Starts the Reitit router."
  [component]
  (let [options           (get-in component [:options :options])
        routes            (:routes component)
        service-map       (get-in component [:service :service])
        storage-component (:storage component)
        data              (components-interceptors options {:storage storage-component})
        router            (http/router routes data)]

    (pedestal/replace-last-interceptor
      service-map
      (pedestal/routing-interceptor
        router
        (no-routes-match-handlers options router)))))

(defrecord Router [options routes service storage]
  component/Lifecycle

  (start
    [this]
    (println "Starting the #<Router> component.")
    (let [router (start-router this)]
      (assoc this :router router)))

  (stop
    [this]
    (println "Stopping the #<Router> component.")
    (dissoc this :router))

  Object
  (toString [_] "#<Router>"))

(defmethod clojure.core/print-method Router
  [_ ^Writer writer]
  (.write writer "#<Router>"))

(defn new-router
  "Creates a new Router component."
  [options routes]
  (map->Router
    {:options options
     :routes routes}))

