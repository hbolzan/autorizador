(ns autorizador.server
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [response]]
            [ring.middleware.json :as middleware]
            [org.httpkit.server :as httpkit.server]))

(defroutes routes
  (GET "/api/v1/hello" []
    (response {:code   "Hello world!"})))

(def service
  (-> routes
      middleware/wrap-json-body
      middleware/wrap-json-response))

(defn -main
  [& args]
  (let [port 8838]
    (println (format "Servidor escutando requisições HTTP na porta %s" port))
    (httpkit.server/run-server service {:port port})))
