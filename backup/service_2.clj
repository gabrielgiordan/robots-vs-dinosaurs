(ns robots-vs-dinosaurs.service-2
  (:require [io.pedestal.http :as server]
            [reitit.dev.pretty :as pretty]
            [reitit.pedestal :as pedestal]
            [reitit.ring :as ring]
            [reitit.http :as http]
            [reitit.http.coercion :as coercion]
            [reitit.coercion.spec :as coercion-spec]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.http.interceptors.parameters :as parameters]
            [reitit.http.interceptors.muuntaja :as muuntaja]
            [reitit.http.interceptors.exception :as exception]
            [reitit.http.interceptors.multipart :as multipart]
            [ring.util.response :as ring-resp]
            [robots-vs-dinosaurs.controller :as controller]
            [robots-vs-dinosaurs.spec :as spec]
            [clojure.spec.alpha :as s]))

;(defn get-scoreboard
;  "Scoreboard handler."
;  [_]
;  (ring-resp/response
;    (controller/get-scoreboard)))
;
;(defn get-board
;  "Board handler."
;  [_]
;  (ring-resp/response
;    (controller/get-board)))
;
;
;(defn new-board
;  "New board handler."
;  [{{{{:keys [width height]} :size} :body} :parameters}]
;  (ring-resp/response
;    (controller/new-board width height)))

(def routes
  [["/v1"
    ;["/scoreboard" {:swagger {:tags ["Scoreboard"]}
    ;                :get {:summary "Get the current simulation scoreboard."
    ;                      :responses {200 {:body ::spec/scoreboard}}
    ;                      :handler get-scoreboard}}]
    ;["/board" {:swagger {:tags ["Board"]}
    ;           :get {:summary "Get the current simulation board."
    ;                 :responses {200 {:body ::spec/board}}
    ;                 :handler get-board}
    ;           :post {:summary "Creates a new board with a given the size."
    ;                  :parameters {:body ::spec/size}
    ;                  :responses {200 {:body ::spec/board}}
    ;                  :handler new-board}}]]
    ]
   ["" {:no-doc true}
    ["/swagger.json" {:get {:swagger {:info
                                      {:title "Robots vs Dinosaurs"
                                       :description
                                              "Service that provides support to run
                                              simulations on remote-controlled robots
                                              that fight dinosaurs."}}
                            :handler (swagger/create-swagger-handler)}}]
    ["/api-docs/*" {:get (swagger-ui/create-swagger-ui-handler
                           {:config {:validatorUrl nil
                                     :docExpansion "list"}})}]]])

(defonce data
  {:exception pretty/exception
   :data {:coercion coercion-spec/coercion
          :muuntaja muuntaja.core/instance
          :interceptors [swagger/swagger-feature
                         (parameters/parameters-interceptor)
                         (muuntaja/format-negotiate-interceptor)
                         (muuntaja/format-response-interceptor)
                         (exception/exception-interceptor)
                         (muuntaja/format-request-interceptor)
                         (coercion/coerce-response-interceptor)
                         (coercion/coerce-request-interceptor)
                         (multipart/multipart-interceptor)]}})