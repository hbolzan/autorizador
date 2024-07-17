(ns autorizador.diplomat.http-in
  (:require [autorizador.adapter.transaction :as adapter.transaction]
            [autorizador.controller.transaction :as controller.transaction]
            [autorizador.wire.transaction :as wire.transaction]
            [ring.util.response :refer [response]]
            [schema.core :as s]))

(s/defn handle-transaction :- wire.transaction/Authorization
  [transaction :- wire.transaction/Transaction]
  (-> transaction
      adapter.transaction/wire->internal
      controller.transaction/authorize!
      response))
