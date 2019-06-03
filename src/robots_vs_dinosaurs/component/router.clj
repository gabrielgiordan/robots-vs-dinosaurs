(ns robots-vs-dinosaurs.component.router
  "Router component for alternative router Reitit.
  https://metosin.github.io/reitit/performance.html"
  (:require [com.stuartsierra.component :as component]
            [reitit.http :as http]
            [reitit.pedestal :as pedestal]
            [reitit.ring :as ring]
            [robots-vs-dinosaurs.interceptors :refer [redirect-trailing-slash-handler]])
  (:import (java.io Writer)
           (clojure.lang ExceptionInfo)))

(defn no-routes-match-handlers
  "Add handlers when no routes match."
  [options router]
  (ring/routes
    (redirect-trailing-slash-handler
      {:method (-> options :redirect-trailing-slash :method)
       :router router})
    (ring/create-resource-handler)
    (ring/create-default-handler)))

(defn request-component-interceptor
  "Attaches the components to the Reitit request."
  [component]
  {:name        ::components
   :description "Components interceptor for Reitit."
   :enter       (fn [context]
                  (merge-with merge context {:request {:components component}}))})

(defn merge-components-interceptors
  "Add the components to the data to get intercepted."
  [data components]
  (let [interceptor (request-component-interceptor components)]
    (update-in data [:data :interceptors] #(vec (concat %1 %2)) [interceptor])))

(defn start-router
  "Starts the Reitit router."
  [component options service routes]
  (let [data (merge-components-interceptors
               (:options options)
               {:storage (:storage component)})
        router (http/router routes data)]
    (pedestal/replace-last-interceptor
      service
      (pedestal/routing-interceptor
        router
        (no-routes-match-handlers options router)))))

(defrecord Router [options service routes storage]
  component/Lifecycle

  (start
    [this]
    (println "Starting the #<Router> component.")
    (try
      (let [router (start-router
                     this
                     options
                     (:service service)
                     (:routes routes))]
        (assoc this :router router))
      (catch ExceptionInfo ex
        (prn "Error when starting the #<Service>" (ex-data ex)))))

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
  [options]
  (map->Router {:options options}))

