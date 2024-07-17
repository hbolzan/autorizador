(ns autorizador.logic.account
  (:require [autorizador.logic.transaction :as logic.transaction]
            [autorizador.model.transaction :as model.transaction]
            [autorizador.wire.account :as wire.account]
            [schema.core :as s]))

(s/defn sufficient-funds? :- s/Bool
  [account :- wire.account/Account
   {amount :transaction/amount :as transaction} :- model.transaction/Transaction]
  (let [category (logic.transaction/transaction->benefit-category transaction)
        balance  (-> account :balances (get category))]
    (>= balance amount)))

(s/defn with-new-transaction :- [wire.account/Transaction]
  [transactions :- [wire.account/Transaction]
   {:transaction/keys [amount] source-id :transaction/id :as transaction} :- model.transaction/Transaction
   random-uuid-fn]
  (conj transactions {:id        (random-uuid-fn)
                      :source-id source-id
                      :amount    amount
                      :category  (logic.transaction/transaction->benefit-category transaction)}))

(s/defn with-updated-balance :- wire.account/Account
  [account :- wire.account/Account
   {:transaction/keys [amount] source-id :transaction/id :as transaction} :- model.transaction/Transaction]
  (let [category (logic.transaction/transaction->benefit-category transaction)
        balance  (-> account :balances (get category))]
    (assoc-in account [:balances category] (- balance amount))))

(s/defn updated-account :- wire.account/Account
  [{:keys [transactions] :as account} :- wire.account/Account
   transaction :- model.transaction/Transaction
   random-uuid-fn]
  (-> account
      (assoc :transactions (with-new-transaction transactions transaction random-uuid-fn))
      (with-updated-balance transaction)))
