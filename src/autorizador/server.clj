(ns autorizador.server
  (:require
   [autorizador.middleware.input :as middleware.input]
   [autorizador.wire.transaction :as wire.transaction]
   [compojure.core :refer [defroutes POST routes]]
   [org.httpkit.server :as httpkit.server]
   [ring.middleware.json :as middleware]
   [ring.util.response :refer [response]]))

(defn with-middleware [handler middleware-list]
  (reduce (fn [h m] (m h)) handler (reverse middleware-list)))

(defn log-request [handler]
  (fn [request]
    (println "******** log-request")
    (println request)
    (println "********")
    ;; (println (-> request :body slurp))
    (handler request)))

(defroutes transaction
  (POST "/api/v1/transaction" [] (response {:code :07})))

(def service
  (-> (routes
       (-> transaction
           log-request
           (middleware.input/wrap-schema-validation wire.transaction/Transaction)))
      middleware/wrap-json-body
      middleware.input/parse-body
      middleware.input/wrap-exception-handler
      middleware/wrap-json-response))

(defn -main
  [& args]
  (let [port 8838]
    (println (format "Servidor escutando na porta %s" port))
    (httpkit.server/run-server service {:port port})))
