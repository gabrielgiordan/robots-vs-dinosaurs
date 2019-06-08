(ns robots-vs-dinosaurs.service
  (:require
    (reitit
      [swagger :as swagger]
      [swagger-ui :as swagger-ui])
    (reitit.dev
      [pretty :as pretty])
    (reitit.http
      [coercion :as coercion])
    (reitit.http.interceptors
      [muuntaja :as interceptors.muuntaja])
    (reitit.coercion
      [spec :as coercion.spec])
    (reitit.interceptor
      [sieppari :as sieppari])
    (ring.util
      [response :as ring.response])
    (robots-vs-dinosaurs
      [controller :as controller]
      [spec :as spec]
      [adapter :as adapter]
      [interceptors :as interceptors])
    (muuntaja
      [core :as core.muuntaja])
    (clojure.spec
      [alpha :as s])))

(defn- get-prod-options
  "Gets the default options."
  []
  {:redirect-trailing-slash {:method :both}
   ;:exception              pretty/exception
   :conflicts               nil
   ;:validate               ring-spec/validate
   ;::reitit-spec/explain   expound/expound-str
   :executor                sieppari/executor
   :data                    {:coercion     coercion.spec/coercion
                             :muuntaja     core.muuntaja/instance
                             :interceptors [
                                            ;(parameters/parameters-interceptor)
                                            ;(multipart/multipart-interceptor)
                                            (interceptors/exception-interceptor)
                                            (interceptors.muuntaja/format-interceptor)
                                            (interceptors.muuntaja/format-negotiate-interceptor)
                                            (interceptors.muuntaja/format-response-interceptor)
                                            (interceptors.muuntaja/format-request-interceptor)
                                            (coercion/coerce-request-interceptor)
                                            ;(coercion/coerce-response-interceptor)
                                            ]}})

(defn- get-dev-options
  "Gets the development options merged with default."
  []
  (let [options (get-prod-options)
        dev-options {:redirect-trailing-slash {:method :both}
                     :exception               pretty/exception}
        dev-interceptors [swagger/swagger-feature]]
    (update-in
      (merge dev-options options)
      [:data :interceptor]
      #(vec (concat %2 %1))
      dev-interceptors)))

(defn get-options
  "Get the option according to the given environment `prod` or `dev`."
  [env]
  (if (= env :prod)
    (get-prod-options)
    (get-dev-options)))

;; Simulations
(defn get-simulations-response-handler
  "Creates a 200 response with all the simulations."
  [{{:keys [storage]} :components}]
  (some->
    (controller/get-simulations storage)
    (ring.response/response)))

(defn create-simulation-response-handler!
  "Creates a 201 response with the location header for the created Simulation.
  The default size when not give, is 50 x 50."
  [{uri                                                     :uri
    {:keys [storage]}                                       :components
    {:keys [title size], :or {size {:width 50 :height 50}}} :body-params}]
  (when-some
    [simulation (controller/create-simulation! storage title size)]
    (some->
      uri
      (str "/" (controller/get-id simulation))
      (ring.response/created simulation))))

(defn get-simulation-response-handler
  "Creates a 200 response with the requested Simulation."
  [{{:keys [storage]}       :components
    {:keys [simulation-id]} :path-params}]
  (some->>
    (adapter/string->int simulation-id)
    (controller/get-simulation storage)
    (ring.response/response)))

(defn delete-simulation-response-handler!
  "Creates a 200 response with `success` payload for the deleted Simulation."
  [{{:keys [storage]}       :components
    {:keys [simulation-id]} :path-params}]
  (when
    (some->>
      (adapter/string->int simulation-id)
      (controller/delete-simulation! storage))
    (ring.response/response {:success true})))

;; Robots

(s/def ::orientation string?)
(s/def ::post-robot
  (s/keys :req-un [::spec/point]
          :opt-un [::orientation]))

(defn create-robot-response-handler!
  "Creates a 201 response with the location header for the created Robot."
  [{uri                         :uri
    {:keys [storage]}           :components
    {:keys [simulation-id]}     :path-params
    {:keys [point orientation]} :body-params}]
  (when-some
    [robot
     (some->
       (adapter/string->int simulation-id)
       (as->
         $
         (some->>
           (adapter/string->orientation orientation)
           (controller/create-robot! storage $ point))))]
    (some->
      uri
      (str "/" (controller/get-id robot))
      (ring.response/created robot))))

(defn get-robot-response-handler
  "Gets a response of a robot."
  [{{:keys [storage]}                :components
    {:keys [simulation-id robot-id]} :path-params}]
  (some->
    (adapter/string->int simulation-id)
    (as->
      $
      (some->>
        (adapter/string->int robot-id)
        (controller/get-robot storage $)
        (ring.response/response)))))

(defn get-robots-response-handler
  "Creates a 200 response with the requested Robot."
  [{{:keys [storage]}       :components
    {:keys [simulation-id]} :path-params}]
  (some->>
    (adapter/string->int simulation-id)
    (controller/get-robots storage)
    (ring.response/response)))

(defn turn-robot-left-response-handler!
  "Creates a 200 response with the requested Robot rotated to the left."
  [{{:keys [storage]}                :components
    {:keys [simulation-id robot-id]} :path-params}]
  (some->
    (adapter/string->int simulation-id)
    (as->
      $
      (some->>
        (adapter/string->int robot-id)
        (controller/turn-robot-left! storage $)
        (ring.response/response)))))


(defn get-simulation-routes
  "Gets the Simulation routes."
  []
  ["/api"

   ;; Simulations

   ["/simulations"
    {:swagger {:tags ["Simulations"]}

     :get     {:summary   "Lists all simulations."
               :responses {200 {:body ::spec/simulations}}
               :handler   get-simulations-response-handler}

     :post    {:summary    "Creates a simulation."
               :handler    create-simulation-response-handler!
               :responses  {201 {:body ::spec/simulation}
                            400 {:body ::spec/error-response}}
               :parameters {:body ::spec/post-simulation}}}]

   ["/simulations/:simulation-id"
    {:swagger {:tags ["Simulations"]}
     :get     {:summary    "Gets a simulation."
               :responses  {200 {:body ::spec/simulation}
                            404 {:body ::spec/error-response}
                            400 {:body ::spec/error-response}}
               :handler    get-simulation-response-handler
               :parameters {:path {:simulation-id ::spec/id}}}

     :delete  {:summary    "Deletes a simulation."
               :parameters {:path {:simulation-id ::spec/id}}
               :responses  {200 {:body ::spec/delete-response} ; Could return 204, but has content.
                            404 {:body ::spec/error-response}
                            400 {:body ::spec/error-response}}
               :handler    delete-simulation-response-handler!}}]

   ;; Robots

   ["/simulations/:simulation-id/robots"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "List all robots inside a simulation board."
               :responses  {200 {:body ::spec/robots}}
               :handler    get-robots-response-handler
               :parameters {:path {:simulation-id ::spec/id}}}

     :post    {:summary    "Creates a robot inside a simulation board."
               :handler    create-robot-response-handler!
               :responses  {201 {:body ::spec/robot}
                            400 {}}
               :parameters {:body ::post-robot
                            :path {:simulation-id ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Gets a robot by id."
               :responses  {200 {:body ::spec/robots}}
               :handler    get-robot-response-handler
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/turn-left"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Rotates a robot to the left."
               :responses  {200 {:body ::spec/robots}}
               :handler    turn-robot-left-response-handler!
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/turn-right"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Rotates a robot to the right."
               :responses  {200 {:body ::spec/robots}}
               :handler    get-simulations-response-handler
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/move-forward"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Moves a robot forward."
               :responses  {200 {:body ::spec/robots}}
               :handler    get-simulations-response-handler
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/move-backwards"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Moves a robot backwards."
               :responses  {200 {:body ::spec/robots}}
               :handler    get-simulations-response-handler
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/attack"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Makes a robot attack around it:
     in front, to the left, to the right and behind."
               :responses  {200 {:body ::spec/robots}}
               :handler    get-simulations-response-handler
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/dinosaurs"
    {:swagger {:tags ["Dinosaurs"]}
     :post    {:summary    "Creates a new dinosaur."
               :responses  {200 {:body ::spec/robots}}
               :handler    get-simulations-response-handler
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/dinosaurs/"
    {:swagger {:tags ["Dinosaurs"]}
     :get     {:summary    "Gets all available dinosaurs into a simulation room."
               :responses  {200 {:body ::spec/dinosaurs}}
               :handler    get-simulations-response-handler
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/dinosaurs/:dinosaur-id"
    {:swagger {:tags ["Dinosaurs"]}
     :get     {:summary    "Gets a dinosaurs by id."
               :responses  {200 {:body ::spec/dinosaurs}}
               :handler    get-simulations-response-handler
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]
   ])

(defn get-swagger-routes
  "Gets the Swagger routes."
  []
  [["" {:no-doc true}
    ["/swagger.json"
     {:get
      {:swagger
       {:info
        {:title       "Robots vs Dinosaurs"
         :description "Service that provides support to run simulations on remote-controlled robots that fight dinosaurs."}}
       :handler
       (swagger/create-swagger-handler)}}]
    ["/api-docs/*"
     {:get
      (swagger-ui/create-swagger-ui-handler
        {:config {:validatorUrl nil
                  :docExpansion "list"}})}]]])

(defn- get-prod-routes
  "Gets the production routes."
  []
  [(get-simulation-routes)])

(defn- get-dev-routes
  "Gets the development routes."
  []
  (let [prod-routes (get-prod-routes)
        dev-routes (get-swagger-routes)]
    (vec (concat prod-routes dev-routes))))

(defn get-routes
  "Get the routes according to the current environment."
  [env]
  (if (= env :prod)
    (get-prod-routes)
    (get-dev-routes)))