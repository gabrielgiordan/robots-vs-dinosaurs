(ns robots-vs-dinosaurs.interceptors
  (:require [reitit.http.interceptors.exception :as exception]))

;; TODO:
;; Create a pull request at `metosin/reitit` repository
;; to support `pedestal` for redirect trailing slash handler.
(defn redirect-trailing-slash-handler
  "301/308 redirects trailing slashes, with support to
  custom router for Pedestal handler. Alternative to
  `reitit.ring/redirect-trailing-slash-handler`."
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