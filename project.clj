(defproject pizzure "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [io.pedestal/pedestal.service "0.3.1"]

                 ;; Remove this line and uncomment the next line to
                 ;; use Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.3.1"]
                 ;; [io.pedestal/pedestal.tomcat "0.3.0"]

                 [ch.qos.logback/logback-classic "1.1.2" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.7"]
                 [org.slf4j/jcl-over-slf4j "1.7.7"]
                 [org.slf4j/log4j-over-slf4j "1.7.7"]

                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [korma "0.4.0"]
                 [hiccup "1.0.5"]
                 [org.clojure/core.typed "0.2.72"]
                 [prismatic/schema "0.3.1"]
                 [org.mintsource/pedestal-namespace-reloading "0.1.0"]
                 ]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "pizzure.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.3.0"]]}}
  :main ^{:skip-aot true} pizzure.server)

