(ns autorizador.adapter.transaction
  (:require [autorizador.model.transaction :as model.transaction]
            [autorizador.wire.transaction :as wire.transaction]
            [schema.core :as s]))

(s/defn wire->internal :- model.transaction/Transaction
  [{{:keys [id amount account-id merchant mcc]} :transaction} :- wire.transaction/Transaction]
  #:transaction{:id          id
                :account-id  account-id
                :amount      amount
                :mcc         mcc
                :merchant-id merchant})
