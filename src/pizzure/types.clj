(ns pizzure.types
  (:refer-clojure :exclude [atom doseq let fn defn ref
                            dotimes defprotocol loop for])
  (:require [clojure.core.typed :refer :all]))

(defalias Request (Map Kw Any))
(defalias Response (HMap :mandatory {:status Number
                                     :headers Vec
                                     :body String}))
(defalias RequestHandler [Request -> Response])
(defalias ValidatedRequestHandler (IFn [Request -> Response]
                                       [Request (Map Kw Any) -> Response]))
(defalias HiccupForm (HVec [Kw Any *]))
(defalias ServiceMap (Map Kw Any))

(ann ^:no-check ring.util.response/response [String -> Response])
(ann ^:no-check ring.util.response/redirect [String -> Response])

(defalias Database (HMap))
(defalias DatabaseOptions (HMap))
(defalias DatabaseSpecification (HMap))
(defalias Entity (HMap))
(defalias Query (HMap))
(defalias EntityValue (Map Kw Any))

(ann ^:no-check korma.db/create-db [DatabaseSpecification -> Database])
(ann ^:no-check korma.db/default-connection [Database -> nil])
(ann ^:no-check korma.db/postgres [DatabaseOptions -> DatabaseSpecification])
(ann ^:no-check korma.core/create-entity [String -> Entity])
(ann ^:no-check korma.core/select* [Entity -> Query])
(ann ^:no-check korma.core/insert* [Entity -> Query])
(ann ^:no-check korma.core/values [Query (U EntityValue (Vec EntityValue)) -> Query])
(ann ^:no-check korma.core/exec (All [[a :< EntityValue]] [Query -> (Seq a)]))
(ann ^:no-check korma.core/transform
     (All [[a :< EntityValue]] [Entity [a -> a] -> Entity]))

(ann ^:no-check io.pedestal.http.route.definition/expand-routes [Any -> Any])

(ann ^:no-check hiccup.compiler/render-html [Any -> String])
(ann ^:no-check hiccup.compiler/render-attr-map [(Map Any Any) -> String])
(ann ^:no-check hiccup.page/doctype [Kw -> String])
(ann ^:no-check hiccup.util/*html-mode* Keyword)
(ann ^:no-check hiccup.form/label (IFn [Kw -> HiccupForm]
                                       [Kw String -> HiccupForm]))
(ann ^:no-check hiccup.form/text-field (IFn [Kw -> HiccupForm]
                                            [Kw Any -> HiccupForm]))
(ann ^:no-check hiccup.form/submit-button [String -> HiccupForm])

(ann ^:no-check schema.utils/error? [Any -> Bool])
