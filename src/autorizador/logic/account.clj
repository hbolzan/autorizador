(ns autorizador.logic.account
  (:require [autorizador.logic.transaction :as logic.transaction]
            [autorizador.model.transaction :as model.transaction]
            [autorizador.wire.account :as wire.account]
            [schema.core :as s]))

(s/defn sufficient-funds? :- s/Bool
  [account :- wire.account/Account
   {amount :transaction/amount :as transaction} :- model.transaction/Transaction]
  (let [category (logic.transaction/transaction->benefit-category transaction)
        balance  (+ (-> account :balances (get category)) (-> account :balances :cash))]
    (>= balance amount)))

(s/defn ^:private account-transaction
  [source-id amount category random-uuid-fn]
  {:id        (random-uuid-fn)
   :source-id source-id
   :amount    amount
   :category  category})

(defn conj-if [a conj? x]
  (if conj? (conj a x) a))

(s/defn with-new-transaction :- [wire.account/Transaction]
  [{:keys [transactions balances]} :- wire.account/Account
   {:transaction/keys [amount] source-id :transaction/id :as transaction} :- model.transaction/Transaction
   random-uuid-fn]
  (let [category (logic.transaction/transaction->benefit-category transaction)
        funded   (min (get balances category) amount)
        pending  (- amount funded)]
    (-> transactions
        (conj (account-transaction source-id funded category random-uuid-fn))
        (conj-if (> pending 0.00M) (account-transaction source-id pending :cash random-uuid-fn)))))

(s/defn with-updated-balance :- wire.account/Account
  [{:keys [balances] :as account} :- wire.account/Account
   {:transaction/keys [amount] :as transaction} :- model.transaction/Transaction]
  (let [category (logic.transaction/transaction->benefit-category transaction)
        balance  (-> (get balances category))
        cash     (:cash balances)
        funded   (min balance amount)
        pending  (- amount funded)]
    (-> account
        (assoc-in [:balances category] (- balance funded))
        (assoc-in [:balances :cash] (- cash pending)))))

(s/defn updated-account :- wire.account/Account
  [account :- wire.account/Account
   transaction :- model.transaction/Transaction
   random-uuid-fn]
  (-> account
      (assoc :transactions (with-new-transaction account transaction random-uuid-fn))
      (with-updated-balance transaction)))
