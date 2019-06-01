(defproject robots-vs-dinosaurs "0.1.0-SNAPSHOT"
  :description "Robots vs Dinosaurs exercise."
  :url "http://robots-vs-dinosaurs.herokuapp.com/"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [io.pedestal/pedestal.service "0.5.5"]
                 [io.pedestal/pedestal.jetty "0.5.5"]
                 [metosin/reitit "0.3.7"]
                 [metosin/reitit-pedestal "0.3.7"]
                 [com.stuartsierra/component "0.4.0"]
                 [ch.qos.logback/logback-classic "1.2.3" exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.26"]
                 [org.slf4j/jcl-over-slf4j "1.7.26"]
                 [org.slf4j/log4j-over-slf4j "1.7.26"]
                 [nano-id "0.9.3"]]
  :min-lein-version "2.0.0"
  :test-paths ["test"]
  :target-path "target/%s"
  :resource-paths ["config", "resources"]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "robots-vs-dinosaurs.server/run-dev"]}
                   :dependencies [[org.clojure/test.check "0.9.0"]
                                  [io.pedestal/pedestal.service-tools "0.5.5"]]}
             :uberjar {:aot [robots-vs-dinosaurs.server]}}
  :main ^:skip-aot robots-vs-dinosaurs.server)