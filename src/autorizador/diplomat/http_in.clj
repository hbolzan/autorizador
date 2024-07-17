(ns autorizador.diplomat.http-in
  (:require [ring.util.response :refer [response]]))

(defn handle-transaction [request]
  (response {:authorization {:code :00}}))
