(ns autorizador.controller.transaction
  (:require [autorizador.diplomat.db :as diplomat.db]
            [autorizador.logic.account :as logic.account]
            [autorizador.model.transaction :as model.transaction]
            [autorizador.wire.account :as wire.account]
            [autorizador.controller.merchant :as controller.merchant]
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
                                 (:transaction/amount transaction))
                         (Exception. "{:code :51}"))))))

(s/defn maybe-with-merchant :- model.transaction/Transaction
  [{:transaction/keys [merchant-id] :as transaction}  :- model.transaction/Transaction]
  (let [merchant (controller.merchant/one! merchant-id)]
    (if (empty? merchant)
      transaction
      (assoc transaction :transaction/merchant merchant))))

(s/defn authorize!
  [transaction  :- model.transaction/Transaction]
  (diplomat.db/update-record! :accounts (maybe-with-merchant transaction) append-transaction :transaction/account-id)
  {:authorization {:code :00}})
