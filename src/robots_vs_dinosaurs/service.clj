(ns robots-vs-dinosaurs.service
  (:require [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.dev.pretty :as pretty]
            [reitit.http.coercion :as coercion]
            [reitit.coercion.spec :as coercion-spec]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.http.interceptors.parameters :as parameters]
            [reitit.http.interceptors.muuntaja :as muuntaja]
            [reitit.http.interceptors.exception :as exception]
            [reitit.http.interceptors.multipart :as multipart]
            [reitit.interceptor.sieppari :as sieppari]
            [ring.util.response :as ring-resp]))

(defn- get-prod-options
  "Gets the default options."
  []
  {:executor sieppari/executor
   :data     {:coercion     coercion-spec/coercion
              :muuntaja     muuntaja.core/instance
              :interceptors [(parameters/parameters-interceptor)
                             (muuntaja/format-negotiate-interceptor)
                             (muuntaja/format-response-interceptor)
                             (exception/exception-interceptor)
                             (muuntaja/format-request-interceptor)
                             (coercion/coerce-response-interceptor)
                             (coercion/coerce-request-interceptor)
                             (multipart/multipart-interceptor)]}})

(defn- get-dev-options
  "Gets the development options merged with default."
  []
  (let [options (get-prod-options)
        dev-options {:exception pretty/exception}
        dev-interceptors [swagger/swagger-feature]]
    (-> (merge dev-options options)
        (update-in [:data :interceptors] concat dev-interceptors))))

(defn get-options
  "Get the option according to the current environment."
  [env]
  (if (= env :prod)
    (get-prod-options)
    (get-dev-options)))

(defn get-simulation
  "Scoreboard handler."
  [_]
  (ring-resp/response
    {:hello "world2222"}))

(defn get-simulation-routes
  "Gets the Simulation routes."
  []
  ["/api"
   ["/simulations"
    {:swagger
     {:tags ["Simulation"]}
     :get
     {:summary   "Lists all available simulations rooms."
      :responses {200 {:body map?}}
      :handler   get-simulation}}]
   ;["/board" {:swagger {:tags ["Board"]}
   ;           :get {:summary "Get the current simulation board."
   ;                 :responses {200 {:body ::spec/board}}
   ;                 :handler get-board}
   ;           :post {:summary "Creates a new board with a given the size."
   ;                  :parameters {:body ::spec/size}
   ;                  :responses {200 {:body ::spec/board}}
   ;                  :handler new-board}}]]
   ])

(defn get-swagger-routes
  "Gets the Swagger routes."
  []
  ["" {:no-doc true}
   ["/swagger.json"
    {:get
     {:swagger
      {:info
       {:title       "Robots vs Dinosaurs"
        :description "Service that provides support to run
        simulations on remote-controlled
        robots that fight dinosaurs."}}
      :handler
      (swagger/create-swagger-handler)}}]
   ["/api-docs/*"
    {:get
     (swagger-ui/create-swagger-ui-handler
       {:config {:validatorUrl nil
                 :docExpansion "list"}})}]])

(defn- get-prod-routes
  "Gets the production routes."
  []
  [(get-simulation-routes)])

(defn- get-dev-routes
  "Gets the development routes."
  []
  (let [prod-routes (get-prod-routes)
        dev-routes [(get-swagger-routes)]]
    (into [] (concat dev-routes prod-routes))))

(defn get-routes
  "Get the routes according to the current environment."
  [env]
  (if (= env :prod)
    (get-prod-routes)
    (get-dev-routes)))