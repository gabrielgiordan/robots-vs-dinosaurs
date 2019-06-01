(ns robots-vs-dinosaurs.components.service
  "Pedestal and Reitit service component for interceptors."
  (:require [io.pedestal.http :as server]
            [reitit.pedestal :as pedestal]
            [reitit.http :as http]
            [reitit.ring :as ring]
            [robots-vs-dinosaurs.service :as service]))

(declare redirect-trailing-slash-handler)

(defn- when-no-routes-match-handlers
  "Adds Ring handlers, Swagger and custom redirect for trailing slash when no default routes matches."
  [router]
  (ring/routes
    (redirect-trailing-slash-handler
      {:method :both
       :router router})
    (ring/create-resource-handler)
    (ring/create-default-handler)))

(defn- reitit-router-support
  "Replaces the Pedestal router for the Reitit one."
  [service-map routes data]
  (let [router (http/router routes data)]
    (-> service-map
        (pedestal/replace-last-interceptor
          (pedestal/routing-interceptor
            router
            (when-no-routes-match-handlers router))))))

(defn- do-service-map-intercept
  "Intercepts the `service-map` with default and custom interceptors."
  [service-map routes data]
  (-> service-map
      (server/default-interceptors)
      (reitit-router-support routes data)))

(defn create-service-map
  "Creates a new `service-map` given `env`."
  [service-maps environment]
  (-> (service-maps environment)
      (do-service-map-intercept service/routes service/data)))

;; TODO:
;; Create a pull request at `metosin/reitit` repository to support `pedestal` for redirect trailing slash handler.
(defn- redirect-trailing-slash-handler
  "301/308 redirects trailing slashes, with support to custom router for Pedestal handler.
  Alternative to `reitit.ring/redirect-trailing-slash-handler`."
  ([{:keys [method router]}]
   (letfn [(maybe-redirect [request path]
             (if (and (seq path) (reitit.core/match-by-path router path))
               {:status (if (= (:request-method request) :get) 301 308)
                :headers {"Location" path}
                :body ""}))
           (redirect-handler [request]
             (let [uri (:uri request)]
               (if (clojure.string/ends-with? uri "/")
                 (if (not= method :add)
                   (maybe-redirect request (clojure.string/replace-first uri #"/+$" "")))
                 (if (not= method :strip)
                   (maybe-redirect request (str uri "/"))))))]
     (fn
       ([request]
        (redirect-handler request))
       ([request respond _]
        (respond (redirect-handler request)))))))