(ns autorizador.controller.transaction
  (:require [autorizador.diplomat.db :as diplomat.db]
            [autorizador.logic.account :as logic.account]
            [autorizador.model.transaction :as model.transaction]
            [autorizador.wire.account :as wire.account]
            [schema.core :as s]))

(s/defn append-transaction :- (s/maybe wire.account/Account)
  [accounts :- {s/Any s/Any}
   account-id :- s/Uuid
   transaction :- model.transaction/Transaction]
  (let [account (get accounts account-id)]
    (if (logic.account/sufficient-funds? account transaction)
      (assoc accounts account-id (logic.account/updated-account account transaction random-uuid))
      (throw (Exception. (format "Insufficient funds - account-id: %s, transaction-id %s, amount: %s"
                                 account-id
                                 (:transaction/id transaction)
                                 (:transaction/amount transaction)))))))

(s/defn authorize!
  [transaction  :- model.transaction/Transaction]
  (diplomat.db/update-record! :accounts transaction append-transaction :transaction/account-id)
  {:authorization {:code :00}})
