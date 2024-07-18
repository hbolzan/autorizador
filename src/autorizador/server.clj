(ns autorizador.server
  (:require [autorizador.controller.account :as controller.account]
            [autorizador.controller.merchant :as controller.merchant]
            [autorizador.diplomat.http-in :as diplomat.http-in]
            [autorizador.middleware.input :as middleware.input]
            [autorizador.wire.transaction :as wire.transaction]
            [compojure.core :refer [defroutes GET POST routes]]
            [org.httpkit.server :as httpkit.server]
            [ring.middleware.json :as middleware]))

(defn with-middleware [handler middleware-list]
  (reduce (fn [h m] (m h)) handler (reverse middleware-list)))

(defn log-request [handler]
  (fn [request]
    (println "******** log-request")
    (println request)
    (println "********")
    (handler request)))

(defroutes transaction
  (POST "/api/v1/transaction" request (diplomat.http-in/handle-transaction (:data request))))
(defroutes info
  (GET "/api/v1/accounts" [] (diplomat.http-in/accounts))
  (GET "/api/v1/accounts/:id" [id] (diplomat.http-in/one-account (parse-uuid id)))
  (GET "/api/v1/merchants" [] (diplomat.http-in/merchants))
  (GET "/api/v1/merchants/:name" [name] (diplomat.http-in/one-merchant name)))

(def service
  (-> (routes
       info
       (-> transaction
           ;; log-request
           (middleware.input/wrap-schema-validation wire.transaction/Transaction)
           middleware/wrap-json-body
           middleware.input/parse-body
           middleware.input/wrap-exception-handler))
      middleware/wrap-json-response))

(defn start-server [port]
  (controller.account/start-up!)
  (controller.merchant/start-up!)
  (httpkit.server/run-server service {:port port}))

(defn stop-server [server]
  (server))

(defn -main
  [& args]
  (let [port 8838]
    (println (format "Servidor escutando na porta %s" port))
    (start-server port)))
