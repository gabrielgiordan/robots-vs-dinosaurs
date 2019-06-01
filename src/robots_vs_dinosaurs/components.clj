(ns robots-vs-dinosaurs.components
  "Components such as service maps and interceptors for router."
  (:require [io.pedestal.http :as server]
            [reitit.http :as http]))

(defonce service-maps
         {:prod {:env :prod
                 ::server/type :jetty
                 ::server/port 8080
                 ::http/resource-path "/public"
                 ::server/routes []}
          :dev (server/dev-interceptors
                 {:env :dev
                  ::server/join? false
                  ::server/allowed-origins {:creds true :allowed-origins (constantly true)}
                  ;; Below is to allow Swagger on `dev` mode,
                  ;; CSP violations due to inline styles:
                  ;; https://github.com/swagger-api/swagger-ui/issues/3370
                  ::server/secure-headers {:content-security-policy-settings
                                           {:default-src "'self'"
                                            :style-src "'self' 'unsafe-inline'"
                                            :script-src "'self' 'unsafe-inline'"}}})})
