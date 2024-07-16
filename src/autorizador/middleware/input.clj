(ns autorizador.middleware.input
  (:require [schema.core :as s]
            [camel-snake-kebab.core :as csk]
            [cheshire.core :as json]
            [schema.coerce :as coerce]
            [ring.util.response :refer [response]]))

(defn parse-body [handler]
  (fn [request]
    (handler (assoc request
                    :data
                    (-> request :body slurp (json/parse-string (comp csk/->kebab-case-keyword keyword)))))))

(def safe-bigdec (coerce/safe bigdec))

(defn big-decimal-matcher [schema]
  (when (= java.math.BigDecimal schema)
    safe-bigdec))

(def wire-matcher (coerce/first-matcher [big-decimal-matcher coerce/json-coercion-matcher]))

(defn wrap-schema-validation [handler schema]
  (fn [request]
    (let [coercer (coerce/coercer schema wire-matcher)
          data (-> request :data coercer)]
      (->> data (s/validate schema) handler))))

(defn wrap-exception-handler [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (println e)
        (response {:authorization {:code :07}})))))
