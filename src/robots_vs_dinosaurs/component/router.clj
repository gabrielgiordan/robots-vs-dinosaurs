(ns robots-vs-dinosaurs.component.router
  (:require [com.stuartsierra.component :as component]
            [reitit.http :as http]
            [reitit.pedestal :as pedestal]
            [reitit.ring :as ring]
            [robots-vs-dinosaurs.interceptors :refer [redirect-trailing-slash-handler]])
  (:import (java.io Writer)))

(defn no-routes-match-handlers
  "Add handlers when no routes match."
  [options router]
  (ring/routes
    (redirect-trailing-slash-handler
      {:method (-> options :redirect-trailing-slash :method)
       :router router})
    (ring/create-resource-handler)
    (ring/create-default-handler)))

(defn start-router
  "Starts the Reitit router."
  [options service routes]
  (let [router (http/router routes (:options options))]
    (pedestal/replace-last-interceptor
      service
      (pedestal/routing-interceptor
        router
        (no-routes-match-handlers options router)))))

(defrecord Router [options service routes]
  component/Lifecycle

  (start
    [this]
    (println "Starting the #<Router> component.")
    (let [router (start-router options (:service service) (:routes routes))]
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
  [options]
  (map->Router {:options options}))

