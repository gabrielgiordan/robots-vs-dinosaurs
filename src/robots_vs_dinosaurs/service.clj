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
      [adapter :as adapter]
      [interceptors :as interceptors])
    (muuntaja
      [core :as core.muuntaja])))

;;
;; Service Options
;;
(defn- get-options
  "Gets the default options."
  []
  {:redirect-trailing-slash {:method :both}
   :executor                sieppari/executor
   :data                    {:coercion     coercion.spec/coercion
                             :muuntaja     core.muuntaja/instance
                             :interceptors [(interceptors/exception-interceptor)
                                            (interceptors.muuntaja/format-interceptor)
                                            (interceptors.muuntaja/format-negotiate-interceptor)
                                            (interceptors.muuntaja/format-response-interceptor)
                                            (interceptors.muuntaja/format-request-interceptor)
                                            (coercion/coerce-request-interceptor)
                                            (coercion/coerce-response-interceptor)]}})

(defn- dev-options
  "Gets the development options merged with default."
  []
  (let [options (get-options)
        dev-options {:redirect-trailing-slash {:method :both}
                     :exception               pretty/exception}
        dev-interceptors [swagger/swagger-feature]]
    (update-in
      (merge dev-options options)
      [:data :interceptor]
      #(vec (concat %2 %1))
      dev-interceptors)))

(defn options
  "Get the option according to the given environment `prod` or `dev`."
  [env]
  (if (= env :prod)
    (get-options)
    (dev-options)))

;;
;; Simulation Handlers
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
;; Robots Handlers
;;
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
           (controller/new-robot! storage $ (adapter/map->Point point)))))]
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
;; Dinosaurs Handlers
;;
(defn create-dinosaur-response-handler!
  "Creates a 201 response with the location header for the created Dinosaur."
  [{uri                     :uri
    {:keys [storage]}       :components
    {:keys [simulation-id]} :path-params
    {:keys [point subtype]}         :body-params}]
  (when-some
    [dinosaur
     (some->
       (adapter/string->int simulation-id)
       (as->
         $
         (some->>
           (adapter/string->dinosaur-subtype subtype)
           (controller/new-dinosaur!
             storage
             $
             (adapter/map->Point point)))))]
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

;;
;; Routes
;;
(def simulation-routes
  [["/simulations"
    {:swagger {:tags ["Simulations"]}

     :get     {:summary   "Gets all the simulation spaces."
               :responses {200 {:body :simulation/simulations}}
               :handler   get-simulations-response-handler}

     :post    {:summary    "Create an empty simulation space."
               :handler    create-simulation-response-handler!
               :responses  {201 {:body :simulation/simulation}
                            400 {:body :response/error}}
               :parameters {:body :request/simulation}}}]

   ["/simulations/:simulation-id"
    {:swagger {:tags ["Simulations"]}
     :get     {:summary    "Gets a simulation space."
               :responses  {200 {:body :simulation/simulation}
                            404 {:body :response/error}
                            400 {:body :response/error}}
               :handler    get-simulation-response-handler
               :parameters {:path {:simulation-id :simulation/id}}}

     :delete  {:summary    "Deletes a simulation space."
               :parameters {:path {:simulation-id :simulation/id}}
               :responses  {200 {:body :response/success}   ; Could return 204, but has content.
                            404 {:body :response/error}
                            400 {:body :response/error}}
               :handler    delete-simulation-response-handler!}}]

   ["/simulations/:simulation-id/as-game"
    {:swagger {:tags ["Simulations"]}
     :get     {:summary    "Gets a simulation space as a simple string game."
               :responses  {200 {:body string?}
                            404 {:body :response/error}
                            400 {:body :response/error}}
               :handler    get-simulation-as-string-matrix-response-handler
               :parameters {:path {:simulation-id :simulation/id}}}}]])

(def robots-routes
  [["/simulations/:simulation-id/robots"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Gets all robots."
               :responses  {200 {:body :unit/robots}}
               :handler    get-robots-response-handler
               :parameters {:path {:simulation-id :simulation/id}}}

     :post    {:summary    "Create a robot in a certain position and facing direction."
               :handler    create-robot-response-handler!
               :responses  {201 {:body :unit/robot}
                            400 {:body :response/error}
                            403 {:body :response/error}}
               :parameters {:body :request/robot
                            :path {:simulation-id :simulation/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Gets a robot."
               :responses  {200 {:body :unit/robot}
                            403 {:body :response/error}
                            404 {:body :response/error}}
               :handler    get-robot-response-handler
               :parameters {:path
                            {:simulation-id :simulation/id
                             :robot-id      :unit/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/turn-left"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Turns a robot to the left."
               :responses  {200 {:body :unit/robot}
                            403 {:body :response/error}
                            404 {:body :response/error}}
               :handler    turn-robot-left-response-handler!
               :parameters {:path
                            {:simulation-id :simulation/id
                             :robot-id      :unit/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/turn-right"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Turns a robot to the right."
               :responses  {200 {:body :unit/robot}
                            403 {:body :response/error}
                            404 {:body :response/error}}
               :handler    turn-robot-right-response-handler!
               :parameters {:path
                            {:simulation-id :simulation/id
                             :robot-id      :unit/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/move-forward"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Moves a robot forward."
               :responses  {200 {:body :unit/robot}
                            403 {:body :response/error}
                            404 {:body :response/error}}
               :handler    move-robot-forward-response-handler!
               :parameters {:path
                            {:simulation-id :simulation/id
                             :robot-id      :unit/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/move-backward"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Moves a robot backward."
               :responses  {200 {:body :unit/robot}
                            403 {:body :response/error}
                            404 {:body :response/error}}
               :handler    move-robot-backward-response-handler!
               :parameters {:path
                            {:simulation-id :simulation/id
                             :robot-id      :unit/id}}}}]

   ["/simulations/:simulation-id/robots/:robot-id/attack"
    {:swagger {:tags ["Robots"]}
     :get     {:summary    "Makes a robot attack around it:
     in front, to the left, to the right and behind."
               :responses  {200 {:body :unit/dinosaurs}
                            400 {:body :response/error}
                            404 {:body :response/error}}
               :handler    robot-attack-response-handler!
               :parameters {:path
                            {:simulation-id :simulation/id
                             :robot-id      :unit/id}}}}]])

(def dinosaurs-routes
  [["/simulations/:simulation-id/dinosaurs"
    {:swagger {:tags ["Dinosaurs"]}
     :get     {:summary    "Gets all dinosaurs."
               :responses  {200 {:body :unit/dinosaurs}}
               :handler    get-dinosaurs-response-handler
               :parameters {:path {:simulation-id :simulation/id}}}
     :post    {:summary    "Create a dinosaur in a certain position."
               :responses  {200 {:body :unit/dinosaur}
                            403 {:body :response/error}}
               :handler    create-dinosaur-response-handler!
               :parameters {:body :request/dinosaur
                            :path {:simulation-id :simulation/id}}}}]

   ["/simulations/:simulation-id/dinosaurs/:dinosaur-id"
    {:swagger {:tags ["Dinosaurs"]}
     :get     {:summary    "Gets a dinosaurs."
               :responses  {200 {:body :unit/dinosaur}
                            403 {:body :response/error}
                            404 {:body :response/error}}
               :handler    get-dinosaur-response-handler
               :parameters {:path
                            {:simulation-id :simulation/id
                             :dinosaur-id   :unit/id}}}}]])

(def swagger-routes
  ["" {:no-doc true}
    ["/open-api.json"
     {:get
      {:swagger
       {:info
        {:title       "Robots vs Dinosaurs"
         :description "Service that provides support to run simulations on remote-controlled robots that fight dinosaurs."}}
       :handler
       (swagger/create-swagger-handler)}}]
    ["/docs/*"
     {:get
      (swagger-ui/create-swagger-ui-handler
        {:url "/open-api.json"
         :config {:validatorUrl nil
                  :docExpansion "list"}})}]])

;;
;; Concat Routes
;;
(defn get-routes
  "Gets the Simulation routes."
  []
   (vec
    (concat
      ["/api"]
      simulation-routes
      robots-routes
      dinosaurs-routes)))

(defn- dev-routes
  "Gets the development routes."
  []
  [(get-routes)
   swagger-routes])

(defn routes
  "Get the routes according to the current environment."
  [env]
  (if (= env :prod) (get-routes) (dev-routes)))