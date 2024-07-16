(ns autorizador.adapter.transaction
  (:require [autorizador.model.account :as model.account]
            [autorizador.model.transaction :as model.transaction]
            [autorizador.wire.transaction :as wire.transaction]
            [schema.core :as s]))

(s/defn wire->internal :- model.transaction/Transaction
  [{{:keys [id amount merchant mcc]} :transaction} :- wire.transaction/Transaction
   account :- model.account/Account]
  #:transaction{:id       id
                :account  account
                :amount   amount
                :mcc      mcc
                :merchant merchant})
