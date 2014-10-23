(ns pizzure.service
  (:refer-clojure :exclude [atom doseq let fn defn ref dotimes defprotocol loop for])
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-resp]
            [io.pedestal.log :as log]
            [hiccup.core :refer [html]]
            [hiccup [page :refer :all] [form :as form]]
            [korma.db :refer :all]
            [korma.core :refer :all :as korma]

            [clojure.core.typed :refer :all]
            [pizzure.types :refer :all]
            [schema [core :as s] [coerce :as sc] [utils :as su]]))

;;; pedestal url helpers
(declare url-for form-action)

(ann ^:no-check NonEmptyString Any)
(def NonEmptyString (s/both String (s/pred (comp not empty?) 'empty?)))

(ann-record Bake [name :- String])
;; (ann ^:no-check map->Bake [(Map Kw Any) -> Bake]) ; coerce override example
(tc-ignore                              ;don't care to type check schema lib
 (s/defrecord Bake [name :- NonEmptyString]))

(ann bake-coercion-matcher [-> [Any -> Any]])
(defn bake-coercion-matcher []
  (fn [schema]
    (when (= schema Bake)
      map->Bake)))

(ann ^:no-check parse-bake [Any -> (U Bake (Map Kw (Map Kw Any)))])
(def parse-bake
  (sc/coercer Bake (bake-coercion-matcher)))

;;; convert from map to hmap, enabling assert refinement of type
(ann ^:no-check map->hmap [(Map Kw Any) -> (HMap)])
(def map->hmap identity)

(ann row->Bake [EntityValue -> Bake])
(defn row->Bake [m]
  (let [m (map->hmap m)]
    (assert (string? (:name m)))
    (map->Bake m)))

(defdb db (postgres {:db "food"
                     :user "postgres"}))

(defentity bakes
  (transform row->Bake))

(ann layout [HiccupForm * -> String])
(defn layout [& body]
  (html (html5 [:body body])))

(ann home-page RequestHandler)
(defn home-page
  [request]
  (let [response (layout [:body "Hello Pizza 🍕😻!"
                          [:p]
                          [:a {:href (url-for ::bakes-page)} "View Bakes"]])]
    (ring-resp/response response)))

(ann bakes-page RequestHandler)
(defn bakes-page
  [request]
  (let [bs (exec (select* bakes))
        bake-details (fn [{:keys [name]} :- Bake] [:li name])
        response (layout [:ul (map bake-details bs)]
                         [:a {:href (url-for ::new-bake-page)} "New Bake"])]
    (ring-resp/response response)))

(ann ^:no-check new-bake-page ValidatedRequestHandler)
(defn new-bake-page
  ([params] (new-bake-page params {}))
  ([{{:keys [name]} :params} errors]
     (-> [:form (form-action ::create-bake-page)
          (when-not (empty? errors)
            [:p (str errors)])
          (form/label :name "Name:")
          (form/text-field :name name)
          (form/submit-button "Create Bake")]
         layout
         ring-resp/response)))

(ann create-bake-page RequestHandler)
(defn create-bake-page
  [request]
  (let [bake (parse-bake (:params request))]
    (if (su/error? bake)
      (let [error (:error bake)]
        (assert error)
        (new-bake-page request error))
      (do
        ;; Need Ambrose for macro versions like (insert bakes (values bake))
        (-> (insert* bakes) (values bake) exec)
        (ring-resp/redirect (url-for ::bakes-page))))))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [middlewares/params
                     (body-params/body-params)
                     middlewares/keyword-params
                     bootstrap/html-body]
     ["/bakes" {:get bakes-page} {:post create-bake-page}
      ["/new" {:get new-bake-page}]]
     ]]])

(ann ^:no-check url-for [Kw -> String])
(def url-for (route/url-for-routes routes))
(ann ^:no-check form-action [Kw -> (HMap :mandatory {:action String :method String})])
(def form-action (route/form-action-for-routes routes))

;; Consumed by pizzure.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(ann service ServiceMap)
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
