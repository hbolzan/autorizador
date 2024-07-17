(ns autorizador.controller.transaction
  (:require [autorizador.model.transaction :as model.transaction]
            [autorizador.wire.transaction :as wire.transaction]
            [autorizador.logic.transaction :as logic.transaction]
            [schema.core :as s]))

(s/defn authorize! :- wire.transaction/Authorization
  [transaction :- model.transaction/Transaction]
  (let [benefit-category (logic.transaction/transaction->benefit-category transaction)]
    {:authorization {:code :00}}))
