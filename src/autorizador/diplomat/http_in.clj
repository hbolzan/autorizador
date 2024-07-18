(ns autorizador.diplomat.http-in
  (:require [autorizador.adapter.transaction :as adapter.transaction]
            [autorizador.controller.account :as controller.account]
            [autorizador.controller.merchant :as controller.merchant]
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

(defn accounts []
  (response (controller.account/all!)))

(defn one-account [id]
  (response (controller.account/one! id)))

(defn merchants []
  (response (controller.merchant/all!)))

(defn one-merchant [name]
  (response (controller.merchant/one! name)))
