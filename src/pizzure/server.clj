(ns pizzure.server
  (:gen-class) ; for -main method in uberjar
  (:require [io.pedestal.http :as server]
            [io.pedestal.http.route :as route]
            [ns-tracker.core :refer [ns-tracker]]
            [pedestal-namespace-reloading.core :refer :all]
            [pizzure.service :as service]))

;; This is an adapted service map, that can be started and stopped
;; From the REPL you can call server/start and server/stop on this service
(defonce runnable-service (server/create-server service/service))

;;; the problem here is with passing in service/routes
;; (defn add-namespace-reloading
;;   [service-map namespaces routes]
;;   (let [modified-namespaces (ns-tracker namespaces)]
;;     (update-in service-map [:io.pedestal.http/interceptors]
;;                (partial map #(if (= (:name %) :io.pedestal.http.route/router)
;;                          (route/router (fn []
;;                                          (doseq [ns-sym (modified-namespaces)]
;;                                            (println "7")
;;                                            (require ns-sym :reload))
;;                                          service/routes))
;;                          %)))))

(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nCreating your [DEV] server...")
  (-> service/service ;; start with production configuration
      (merge {:env :dev
              ;; do not block thread that starts web server
              ::server/join? false
              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ::server/routes #(deref #'service/routes)
              ;; all origins are allowed in dev mode
              ::server/allowed-origins {:creds true :allowed-origins (constantly true)}})
      ;; Wire up interceptor chains
      server/default-interceptors
      server/dev-interceptors
      (add-namespace-reloading ["src"])
      server/create-server
      server/start))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server...")
  (server/start runnable-service))
