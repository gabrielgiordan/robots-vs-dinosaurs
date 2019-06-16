(ns robots-vs-dinosaurs.interceptors
  (:require [reitit.http.interceptors.exception :as exception]
            [reitit.coercion :as coercion]
            [io.pedestal.log :as log]
            [io.pedestal.interceptor.helpers :as interceptor]
            [muuntaja.core :as muuntaja]))

;;
;; Error handler
;;
(defn- error-response
  "Gets an response response with
  `status` and `body`."
  [status body]
  {:status status :body body})

(defn- safe-format
  "Checks if a format is supported by encoder/decoder,
  else returns the default format"
  [format]
  (if-let [_ (get (:formats muuntaja/default-options) format)]
    format
    (:default-format muuntaja/default-options)))

(defn- error-body
  "Gets the encoded response body."
  [status code message format]
  (some->>
    {:error {:status status, :code code, :message message}}
    (muuntaja/encode (safe-format format))
    (slurp)))

(defn- error-response-encoded
  "Gets an error response encoded."
  [status code message format]
  (error-response status (error-body status code message format)))

(defn- ex-handler
  "Handles an exception."
  ([status code message]
   (fn [ex {{accept "accept"} :headers}]
     (log/error status (ex-message ex))
     {:status  status
      :headers {"Content-type" accept}
      :body    (error-body status code message accept)})))

(defn exception-interceptor
  "Intercept exceptions with custom messages."
  []
  (exception/exception-interceptor
    {;; Simulation exceptions.
     :out-of-bounds                         (ex-handler 403 10 "Invalid operation. Out of bounds.")
     :not-a-robot                           (ex-handler 403 11 "Invalid operation. Not a Robot.")
     :not-a-dinosaur                        (ex-handler 403 12 "Invalid operation. Not a Dinosaur.")
     :occupied                              (ex-handler 403 13 "Invalid operation. Position is occupied.")
     :attack-missed                         (ex-handler 400 14 "Invalid operation. No units to attack.")

     ;; Spec exceptions.
     ::coercion/request-coercion            (ex-handler 400 1 "Invalid request.")
     ::coercion/response-coercion           (ex-handler 500 2 "Response validation failed.")

     ;; Malformed exceptions.
     :muuntaja/decode                       (ex-handler 400 3 "Malformed format when decoding.")
     :muuntaja/encode                       (ex-handler 400 4 "Malformed format when encoding.")
     :muuntaja/request-charset-negotiation  (ex-handler 400 15 "Could not negotiate a charset for the request.")
     :muuntaja/response-charset-negotiation (ex-handler 400 16 "Could not negotiate a charset for the response.")
     :muuntaja/response-format-negotiation  (ex-handler 400 17 "Could not negotiate a format for the response.")

     ;; Generic Exceptions.
     ::exception/exception                  (ex-handler 500 5 "Internal server error.")
     ::exception/default                    (ex-handler 500 6 "Internal server error.")}))

(def not-found-interceptor
  "An interceptor that returns a 404 when routing failed to resolve a route."
  (interceptor/after
    ::not-found
    (fn [context]
      (let [resp (:response context)]
        (if-not (and (map? resp) (integer? (:status resp)))
          (do (log/error 404 (:request context))
              (let [{{{accept "accept"} :headers} :request} context]
                (assoc context :response (error-response-encoded 404 0 "Could not find the requested resource." accept))))
          context)))))

;;
;; Redirect trailing slash `/`
;;
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