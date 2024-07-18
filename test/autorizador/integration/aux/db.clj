(ns autorizador.integration.aux.db
  (:require [autorizador.controller.account :as controller.account]
            [autorizador.controller.transaction :as controller.transaction]
            [autorizador.diplomat.db :as diplomat.db]))

(defn new-account
  []
  {:id           (random-uuid)
   :customer-id  (random-uuid)
   :transactions []
   :balances     {:food 0.00M
                  :meal 0.00M
                  :cash 0.00M}})

(defn transact [account-id mcc amount merchant]
  (let [t-db #:transaction{:id         (random-uuid)
                           :account-id account-id
                           :amount     amount
                           :mcc        mcc
                           :merchant   merchant}]
    (controller.transaction/authorize! t-db)))

(defn create-account!
  [food meal cash]
  (let [account    (new-account)
        account-id (:id account)]
    (diplomat.db/update-record! :accounts account)
    (when (> food 0.00M) (transact account-id :5411 (- food) "CREDITO FOOD"))
    (when (> meal 0.00M) (transact account-id :5811 (- meal) "CREDITO MEAL"))
    (when (> cash 0.00M) (transact account-id :9999 (- cash) "CREDITO CASH"))
    (controller.account/one! account-id)))

(defn init-accounts-db!
  [food meal cash]
  (diplomat.db/init-db! :accounts)
  (create-account! food meal cash))
