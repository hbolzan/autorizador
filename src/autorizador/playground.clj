(ns autorizador.playground
  (:require [autorizador.controller.account :as controller.account]
            [autorizador.controller.merchant :as controller.merchant]
            [autorizador.controller.transaction :as controller.transaction]
            [autorizador.diplomat.db :as diplomat.db]
            [autorizador.wire.account :as wire.account]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [schema.core :as s]))

(defn init-db! []
  (diplomat.db/init-db! :accounts))

(defn save-db! []
  (diplomat.db/save-db! :accounts))

(defn load-db! []
  (diplomat.db/load-db! :accounts))

(defn accounts-list []
  (->> diplomat.db/databases deref :accounts deref (map first)))

(s/defn new-account :- wire.account/Account
  []
  {:id           (random-uuid)
   :customer-id  (random-uuid)
   :transactions []
   :balances     {:food 0.00M
                  :meal 0.00M
                  :cash 0.00M}})

(defn transact [account-id mcc amount merchant]
  (let [t-db #:transaction{:id          (random-uuid)
                           :account-id  account-id
                           :amount      amount
                           :mcc         mcc
                           :merchant-id merchant}]
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

(comment
  :http

  (defonce server (atom nil))
  (reset! server (autorizador.server/start-server 8839))
  (autorizador.server/stop-server @server)

  @(http/get "http://localhost:8839/api/v1/accounts")
  @(http/get "http://localhost:8839/api/v1/merchants")

  @(http/get "http://localhost:8839/api/v1/accounts/09a06fdb-20e4-4c72-9a69-68b7c97df23b")
  @(http/post "http://localhost:8839/api/v1/transaction"
              {:body (json/generate-string
                      {:transaction {:id         #uuid "bc71f950-ef3f-46ff-a71d-838a3ec85012"
                                     :account-id #uuid "0b35718a-50ca-481a-b221-2fe20b3dec4a"
                                     :amount     10.00M
                                     :merchant   "PAG*JoseDaSilva          RIO DE JANEI BR"
                                     :mcc        :5811}})})

  ;;
  )

(comment
  :merchnts

  (diplomat.db/init-db! :merchants)

  ;; add merchants
  (diplomat.db/update-record! :merchants {:id "UBER TRIP                   SAO PAULO BR" :mcc :9999})
  (diplomat.db/update-record! :merchants {:id "UBER EATS                   SAO PAULO BR" :mcc :5812})
  (diplomat.db/update-record! :merchants {:id "PAG*JoseDaSilva          RIO DE JANEI BR" :mcc :5411})
  (diplomat.db/save-db! :merchants)

  (diplomat.db/load-db! :merchants)
  (controller.merchant/one! "UBER EATS                   SAO PAULO BR")
  ;;
  )

(comment

;; create some accounts
  (init-db!)
  (create-account! 200.00M 300.00M 500.00M)
  (create-account! 0.00M 100.00M 50.00M)
  (create-account! 1000.00M 0.00M 0.00M)
  (create-account! 100.00M 200.00M 300.00M)

  (save-db!)
  (load-db!)

  ;; list account id's
  (accounts-list)

  ;; get account detail
  (controller.account/one! #uuid "c99be8af-5e11-4130-98d3-f7bfce521a07")

  ;; add some transactions
  (let [account-id #uuid "c99be8af-5e11-4130-98d3-f7bfce521a07"]
    (transact account-id :5411 140.00M "PADARIA DO ZE               SAO PAULO BR")
    (transact account-id :5811 250.00M "PADARIA DO ZE               SAO PAULO BR")
    (transact account-id :9999 480.00M "PADARIA DO ZE               SAO PAULO BR"))

  ;; look at the account
  (controller.account/one! #uuid "c99be8af-5e11-4130-98d3-f7bfce521a07")

  ;; the next transaction wikk be denied
  (transact #uuid "c99be8af-5e11-4130-98d3-f7bfce521a07" :5411 70.00M "PADARIA DO ZE               SAO PAULO BR")

  (defn rejection-code [e]
    (let [cause (.getCause e)]
      (if cause
        (read-string (.getMessage cause))
        {:code :07})))

  (try
    (throw (Exception. "Insufficient funds" (Exception. "{:code :51}")))
    (catch Exception e
      (rejection-code e)))

  (try
    (throw (Exception. "Other error"))
    (catch Exception e
      (rejection-code e)))

;;
  )
