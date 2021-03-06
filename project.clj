(defproject robots-vs-dinosaurs "0.1.0-SNAPSHOT"
  :description "Robots vs Dinosaurs exercise."
  :url "http://robots-vs-dinosaurs.herokuapp.com/"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.stuartsierra/component "0.4.0"]
                 [io.pedestal/pedestal.service "0.5.5"]
                 [io.pedestal/pedestal.jetty "0.5.5"]
                 [metosin/reitit "0.3.7"]
                 [metosin/reitit-pedestal "0.3.7"]
                 [ch.qos.logback/logback-classic "1.2.3" exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.26"]
                 [org.slf4j/jcl-over-slf4j "1.7.26"]
                 [org.slf4j/log4j-over-slf4j "1.7.26"]
                 [environ "1.1.0"]]
  :min-lein-version "2.0.0"
  :plugins [[lein-environ "1.1.0" :hooks false] ; Leiningen hooks are deprecated.
            [lein-cloverage "1.1.2-SNAPSHOT"]
            [lein-auto "0.1.3"]]
  :test-paths ["test"]
  :target-path "target/%s"
  :resource-paths ["config", "resources"]
  :profiles {:dev     {:aliases      {"run-dev" ["trampoline" "run" "-m" "robots-vs-dinosaurs.server/run-dev"]}
                       :source-paths ["dev"]
                       :dependencies [[midje "1.9.8"]
                                      [org.clojure/test.check "0.9.0"]
                                      [org.clojure/tools.namespace "0.2.11"]
                                      [org.clojure/tools.nrepl "0.2.13"]
                                      [io.pedestal/pedestal.service-tools "0.5.5"]
                                      [reloaded.repl "0.2.4"]
                                      [org.clojure/java.classpath "0.2.0"]]}
             :uberjar {:aot [robots-vs-dinosaurs.server]}}
  :jar-name "robots-vs-dinosaurs.jar"
  :uberjar-name "robots-vs-dinosaurs-standalone.jar"
  :main ^:skip-aot robots-vs-dinosaurs.server)