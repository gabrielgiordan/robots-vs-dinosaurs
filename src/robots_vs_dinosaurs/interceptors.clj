(ns robots-vs-dinosaurs.interceptors
  (:require [reitit.http.interceptors.exception :as exception]
            [reitit.coercion :as coercion]
            [io.pedestal.log :refer [log]]
            [muuntaja.core :as muuntaja]
            [io.pedestal.interceptor.helpers :as interceptor]
            [io.pedestal.log :as log]))

(defn response
  [status body]
  {:status status :body body})

(defn error-body
  [status code message]
  {:error {:status status, :code code, :message message}})

(defn error-body-encoded
  [status code message]
  (->>
    (error-body status code message)
    (muuntaja/encode "application/json")
    (slurp)))

(defn error-response-map
  [status code message]
  (response status (error-body status code message)))

(defn error-response-encoded
  [status code message]
  (response status (error-body-encoded status code message)))

(defn error-response
  [status code message log-data]
  (let [response (error-body status code message)]
    (when-not (nil? log-data)
      (log {:log-data log-data :response response}))
    response))

(defn error-response-400
  [message log-data]
  (error-response 400 8 message log-data))

(defn error-response-404
  [message log-data]
  (error-response 404 9 message log-data))

(defn exception-handler
  "Creates a new exception handler."
  ([status code message]
   (fn [_exception _request]
     {:status status
      :body   (error-body-encoded status code message)})))

(def not-found-interceptor
  "An interceptor that returns a 404 when routing failed to resolve a route."
  (interceptor/after
    ::not-found
    (fn [context]
      (let [resp (:response context)]
        (if-not (and (map? resp) (integer? (:status resp)))
          (do (log/meter ::not-found)
              (assoc context :response (error-response-encoded 404 0 "Could not find the requested resource.")))
          context)))))

; :muuntaja/decode, input can't be decoded with the negotiated format & charset.
; :muuntaja/request-charset-negotiation, request charset is illegal.
; :muuntaja/response-charset-negotiation, could not negotiate a charset for the response.
; :muuntaja/response-format-negotiation, could not negotiate a format for the response.


(defn exception-interceptor
  "Intercept exceptions with custom Json messages."
  []
  (exception/exception-interceptor
    {;; Simulation exceptions.
     :out-of-bounds               (exception-handler 403 10 "Invalid operation. Out of bounds.")
     :not-a-robot                 (exception-handler 403 11 "Invalid operation. Not a Robot.")
     :not-a-dinosaur              (exception-handler 403 12 "Invalid operation. Not a Dinosaur.")
     :occupied                    (exception-handler 403 13 "Invalid operation. Position is occupied.")
     :attack-missed               (exception-handler 400 14 "Invalid operation. No units to attack.")

     ;; Spec exceptions.
     ::coercion/request-coercion  (exception-handler 400 1 "Invalid request.")
     ::coercion/response-coercion (exception-handler 500 2 "Response validation failed.")

     ;; Malformed exceptions.
     :muuntaja/decode             (exception-handler 400 3 "Malformed format when decoding.")
     :muuntaja/encode             (exception-handler 400 4 "Malformed format when encoding.")

     ;; Generic Exceptions.
     ::exception/exception        (exception-handler 500 5 "Internal server error.")
     ::exception/default          (exception-handler 500 6 "Internal server error.")

     ;; Wrap stack-traces for all exceptions.
     ::exception/wrap             (fn [handler exception request]
                                    (log (ex-data exception))
                                    (handler exception request))}))

;; TODO: Create a pull request at `metosin/reitit` repository
(defn redirect-trailing-slash-handler
  "301/308 redirects trailing slashes, with support for custom router."
  ([{:keys [method router]}]
   (letfn [(maybe-redirect [request path]
             (if (and (seq path) (reitit.core/match-by-path router path))
               {:status  (if (= (:request-method request) :get) 301 308)
                :headers {"Location" path}
                :body    ""}))
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