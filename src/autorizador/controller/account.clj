(ns autorizador.controller.account
  (:require [autorizador.diplomat.db :as diplomat.db]
            [autorizador.wire.account :as wire.account]
            [schema.core :as s]))

(diplomat.db/load-db! :accounts)

(s/defn new-account :- wire.account/Account
  [id food meal cash]
  {:id           id
   :customer-id  (random-uuid)
   :transactions []
   :balances     {:food food
                  :meal meal
                  :cash cash}})

(defn swap-fn [m k v]
  (assoc m k v))

;; (s/defn append-transaction :- (s/maybe wire.account/Account)
;;   [accounts :- {s/Any s/Any}
;;    {:transaction/keys [id account-id amount mcc] :as transaction} :- model.transaction/Transaction]
;;   (let [account (get accounts account-id)]
;;     (when (sufficient-funds? account transaction)
;;       )))

(comment
  (diplomat.db/init-db! :accounts)
  (diplomat.db/update-record! :accounts (new-account (random-uuid) 0.00M 0.00M 0.00M))
  (diplomat.db/save-db! :accounts)
  (diplomat.db/one-record :accounts #uuid "2b3a0a0d-3a96-4b53-be4a-37886ac36d91"))
