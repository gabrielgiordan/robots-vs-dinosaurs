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
   :conflicts               nil
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

;;
;; Simulations
;;
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
    [simulation (controller/new-simulation! storage title size)]
    (some->
      uri
      (str "/" (:id simulation))
      (ring.response/created simulation))))

(defn get-simulation-response-handler
  "Creates a 200 response with the requested Simulation."
  [{{:keys [storage]}       :components
    {:keys [simulation-id]} :path-params}]
  (some->>
    (adapter/string->int simulation-id)
    (controller/get-simulation storage)
    (ring.response/response)))

(defn get-simulation-as-string-matrix-response-handler
  "Creates a 200 response with the requested Simulation string matrix."
  [{{:keys [storage]}       :components
    {:keys [simulation-id]} :path-params}]
  (some->>
    (adapter/string->int simulation-id)
    (controller/get-simulation-as-game storage)
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

;;
;; Robots
;;
(s/def ::orientation string?)
(s/def ::post-robot
  (s/keys :req-un [::spec/point]
          :opt-un [::orientation]))

(s/def ::post-dinosaur
  (s/keys :req-un [::spec/point]))

(defn- robot-response-handler!
  "Creates a 200 response with `requested-handler` function."
  [{{:keys [storage]}                :components
    {:keys [simulation-id robot-id]} :path-params}
   request-handler]
  (some->
    (adapter/string->int simulation-id)
    (as->
      $
      (some->>
        (adapter/string->int robot-id)
        (request-handler storage $)
        (ring.response/response)))))

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
           (controller/new-robot! storage $ (adapter/map->point point)))))]
    (->
      uri
      (str "/" (:id robot))
      (ring.response/created robot))))

(defn get-robots-response-handler
  "Creates a 200 response with the requested Robots."
  [{{:keys [storage]}       :components
    {:keys [simulation-id]} :path-params}]
  (some->>
    (adapter/string->int simulation-id)
    (controller/get-robots storage)
    (ring.response/response)))

(defn get-robot-response-handler
  "Gets a 200 response of a Robot."
  [request]
  (robot-response-handler! request controller/get-robot))

(defn turn-robot-left-response-handler!
  "Creates a 200 response with the requested Robot rotated to the left."
  [request]
  (robot-response-handler! request controller/turn-robot-left!))

(defn turn-robot-right-response-handler!
  "Creates a 200 response with the requested Robot rotated to the right."
  [request]
  (robot-response-handler! request controller/turn-robot-right!))

(defn move-robot-forward-response-handler!
  "Creates a 200 response with the requested Robot moved forward."
  [request]
  (robot-response-handler! request controller/move-robot-forward!))

(defn move-robot-backward-response-handler!
  "Creates a 200 response with the requested Robot moved backward."
  [request]
  (robot-response-handler! request controller/move-robot-backward!))

(defn robot-attack-response-handler!
  "Creates a 200 response with the Robot attacked Dinosaurs."
  [request]
  (robot-response-handler! request controller/robot-attack!))

;;
;; Dinosaurs
;;
(defn create-dinosaur-response-handler!
  "Creates a 201 response with the location header for the created Dinosaur."
  [{uri                     :uri
    {:keys [storage]}       :components
    {:keys [simulation-id]} :path-params
    {:keys [point]}         :body-params}]
  (when-some
    [dinosaur
     (some->
       (adapter/string->int simulation-id)
       (as->
         $
         (some->>
           (controller/new-dinosaur! storage $ (adapter/map->point point)))))]
    (->
      uri
      (str "/" (:id dinosaur))
      (ring.response/created dinosaur))))

(defn get-dinosaurs-response-handler
  "Creates a 200 response with the requested Dinosaurs."
  [{{:keys [storage]}       :components
    {:keys [simulation-id]} :path-params}]
  (some->>
    (adapter/string->int simulation-id)
    (controller/get-dinosaurs storage)
    (ring.response/response)))

(defn get-dinosaur-response-handler
  "Creates a 200 response with the requested Dinosaur"
  [{{:keys [storage]}                   :components
    {:keys [simulation-id dinosaur-id]} :path-params}]
  (some->
    (adapter/string->int simulation-id)
    (as->
      $
      (some->>
        (adapter/string->int dinosaur-id)
        (controller/get-dinosaur storage $)
        (ring.response/response)))))

(defn get-simulation-routes
  "Gets the Simulation routes."
  []
  ["/api"

   ;; Simulations

   ["/simulations"
    {:swagger {:tags ["Simulations"]}

     :get     {:summary   "Gets all the simulation spaces."
               :responses {200 {:body ::spec/simulations}}
               :handler   get-simulations-response-handler}

     :post    {:summary    "Create an empty simulation space."
               :handler    create-simulation-response-handler!
               :responses  {201 {:body ::spec/simulation}
                            400 {:body ::spec/error-response}}
               :parameters {:body ::spec/post-simulation}}}]

   ["/simulations/:simulation-id"
    {:swagger {:tags ["Simulations"]}
     :get     {:summary    "Gets a simulation space."
               :responses  {200 {:body ::spec/simulation}
                            404 {:body ::spec/error-response}
                            400 {:body ::spec/error-response}}
               :handler    get-simulation-response-handler
               :parameters {:path {:simulation-id ::spec/id}}}

     :delete  {:summary    "Deletes a simulation space."
               :parameters {:path {:simulation-id ::spec/id}}
               :responses  {200 {:body ::spec/delete-response} ; Could return 204, but has content.
                            404 {:body ::spec/error-response}
                            400 {:body ::spec/error-response}}
               :handler    delete-simulation-response-handler!}}]

   ["/simulations/:simulation-id/as-game"
    {:swagger {:tags ["Simulations"]}
     :get     {:summary    "Gets a simulation space as a simple string game."
               :responses  {200 {:body ::spec/simulation}
                            404 {:body ::spec/error-response}
                            400 {:body ::spec/error-response}}
               :handler    get-simulation-as-string-matrix-response-handler
               :parameters {:path {:simulation-id ::spec/id}}}}]

   ;; Robots

   ["/simulations/:simulation-id/robots"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Gets all robots."
               :responses  {200 {:body ::spec/robots}}
               :handler    get-robots-response-handler
               :parameters {:path {:simulation-id ::spec/id}}}

     :post    {:summary    "Create a robot in a certain position and facing direction."
               :handler    create-robot-response-handler!
               :responses  {201 {:body ::spec/robot}
                            400 {}}
               :parameters {:body ::post-robot
                            :path {:simulation-id ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Gets a robot."
               :responses  {200 {:body ::spec/robots}}
               :handler    get-robot-response-handler
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/turn-left"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Turns a robot to the left."
               :responses  {200 {:body ::spec/robots}}
               :handler    turn-robot-left-response-handler!
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/turn-right"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Turns a robot to the right."
               :responses  {200 {:body ::spec/robots}}
               :handler    turn-robot-right-response-handler!
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/move-forward"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Moves a robot forward."
               :responses  {200 {:body ::spec/robots}}
               :handler    move-robot-forward-response-handler!
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/move-backward"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Moves a robot backward."
               :responses  {200 {:body ::spec/robots}}
               :handler    move-robot-backward-response-handler!
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/attack"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Makes a robot attack around it:
     in front, to the left, to the right and behind."
               :responses  {200 {:body ::spec/robots}}
               :handler    robot-attack-response-handler!
               :parameters {:path
                            {:simulation-id ::spec/id
                             :robot-id      ::spec/id}}}}]

   ["/simulations/:simulation-id/dinosaurs"
    {:swagger {:tags ["Dinosaurs"]}
     :get     {:summary    "Gets all dinosaurs."
               :responses  {200 {:body ::spec/dinosaurs}}
               :handler    get-dinosaurs-response-handler
               :parameters {:path {:simulation-id ::spec/id}}}
     :post    {:summary    "Create a dinosaur in a certain position."
               :responses  {200 {:body ::spec/dinosaurs}}
               :handler    create-dinosaur-response-handler!
               :parameters {:body ::post-dinosaur
                            :path {:simulation-id ::spec/id}}}}]

   ["/simulations/:simulation-id/dinosaurs/:dinosaur-id"
    {:swagger {:tags ["Dinosaurs"]}
     :get     {:summary    "Gets a dinosaurs."
               :responses  {200 {:body ::spec/dinosaurs}}
               :handler    get-dinosaur-response-handler
               :parameters {:path
                            {:simulation-id ::spec/id
                             :dinosaur-id   ::spec/id}}}}]
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