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
            [reitit.ring.spec :as ring-spec]
            [reitit.spec :as reitit-spec]
            [expound.alpha :as expound]
            [ring.util.response :as ring-resp]
            [robots-vs-dinosaurs.controller :as controller]
            [robots-vs-dinosaurs.spec :as spec]))

(defn- get-prod-options
  "Gets the default options."
  []
  {:validate ring-spec/validate
   ::reitit-spec/explain expound/expound-str
   :executor sieppari/executor
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
        (update-in [:data :interceptors] #(vec (concat %2 %1)) dev-interceptors))))

(defn get-options
  "Get the option according to the current environment."
  [env]
  (if (= env :prod)
    (get-prod-options)
    (get-dev-options)))

(defn all-simulations-handler
  "Gets all simulations."
  [{:keys [components]}]
  (ring-resp/response
    (controller/simulations
      (:storage components))))

(defn reset-simulations-handler
  "Resets all the simulations."
  [{:keys [components]}]
  (ring-resp/response
    (controller/reset-simulations
      (:storage components))))

(defn new-simulation-handler
  "Resets all the simulations."
  [{:keys [body-params components]}]
  (ring-resp/response
    (controller/new-simulation
      (:storage components)
      (:title body-params)
      (:size body-params))))

(defn get-simulation-handler
  "Resets all the simulations."
  [{:keys [components query]}]
  (ring-resp/response
    {}))

(defn get-simulation-routes
  "Gets the Simulation routes."
  []
  ["/api"
   ["/simulations"
    {:swagger {:tags ["Simulations"]}
     :get     {:summary   "Lists all available simulations rooms."
               :responses {200 {:body ::spec/simulations}}
               :handler   all-simulations-handler}
     :post    {:summary    "Creates a new simulation room."
               :handler    new-simulation-handler
               :responses  {200 {:body ::spec/simulation}}
               :parameters {:body ::spec/post-simulation}}}]
   ["/simulation/:id"
    {:swagger {:tags ["Simulations"]}
     :get     {:summary   "Gets a simulation room by id."
               :responses {200 {:body ::spec/simulation}}
               :handler   get-simulation-handler}}]
   ["/simulations/reset"
    {:swagger {:tags ["Simulations"]}
     :get     {:summary   "Resets all simulations."
               :responses {200 {:body {}}}
               :handler   reset-simulations-handler}}]])

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