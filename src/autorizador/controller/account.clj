(ns autorizador.controller.account
  (:require [autorizador.diplomat.db :as diplomat.db]
            [autorizador.logic.account :as logic.account]
            [autorizador.model.transaction :as model.transaction]
            [autorizador.wire.account :as wire.account]
            [schema.core :as s]))

(diplomat.db/load-db! :accounts)

(s/defn one-account! :- (s/maybe wire.account/Account)
  [account-id :- s/Uuid]
  (diplomat.db/one-record :accounts account-id))

(s/defn append-transaction :- (s/maybe wire.account/Account)
  [accounts :- {s/Any s/Any}
   account-id :- s/Uuid
   transaction :- model.transaction/Transaction]
  (println "************ append-transaction")
  (println account-id)
  (println transaction)
  (let [account (get accounts account-id)]
    (if (logic.account/sufficient-funds? account transaction)
      (assoc accounts account-id (logic.account/updated-account account transaction random-uuid))
      (throw (Exception. (format "Insufficient funds - account-id: %s, transaction-id %s, amount: %s"
                                 account-id
                                 (:transaction/id transaction)
                                 (:transaction/amount transaction)))))))

(s/defn append-transaction!
  [transaction  :- model.transaction/Transaction]
  (diplomat.db/update-record! :accounts transaction append-transaction :transaction/account-id))

(comment
  (s/defn new-account :- wire.account/Account
    [id food meal cash]
    {:id           id
     :customer-id  (random-uuid)
     :transactions []
     :balances     {:food food
                    :meal meal
                    :cash cash}})

  (diplomat.db/init-db! :accounts)
  (diplomat.db/update-record! :accounts (new-account (random-uuid) 0.00M 0.00M 0.00M))
  (diplomat.db/save-db! :accounts)
  (diplomat.db/one-record :accounts #uuid "2b3a0a0d-3a96-4b53-be4a-37886ac36d91")

  (let [t-cr #:transaction{:id         #uuid "bc71f950-ef3f-46ff-a71d-838a3ec85012"
                           :account-id #uuid  "2b3a0a0d-3a96-4b53-be4a-37886ac36d91"
                           :amount     -100.00M
                           :mcc        :5811
                           :merchant   "CREDITO MEAL"}]
    (append-transaction! t-cr))

  (defn transact [account-id mcc amount]
    (let [t-db #:transaction{:id         #uuid "bc71f950-ef3f-46ff-a71d-838a3ec85012"
                             :account-id account-id
                             :amount     amount
                             :mcc        mcc
                             :merchant   "PADARIA DO ZE               SAO PAULO BR"}]
      (append-transaction! t-db)))

  (transact #uuid  "2b3a0a0d-3a96-4b53-be4a-37886ac36d91" :5812 35.00M)
  (transact #uuid  "2b3a0a0d-3a96-4b53-be4a-37886ac36d91" :5812 65.00M)
  (transact #uuid  "2b3a0a0d-3a96-4b53-be4a-37886ac36d91" :5412 45.00M)
  ;;
  )
