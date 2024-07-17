(ns autorizador.controller.account
  (:require [autorizador.diplomat.db :as diplomat.db]
            [autorizador.wire.account :as wire.account]
            [schema.core :as s]))

(diplomat.db/init-db! :accounts)

(s/defn new-account :- wire.account/Account
  [id food meal cash]
  {:id           id
   :customer-id  (random-uuid)
   :balances {:food food
              :meal meal
              :cash cash}})

(defn swap-fn [m k v]
  (assoc m k v))

(diplomat.db/update-record! :accounts (new-account (random-uuid) 100.0M 1000M 500M) swap-fn)
(diplmat.db/one-record :accounts #uuid "63fd4523-9dda-454f-9898-141b0a3ea704")

(defn set-account [db new-account]
  (swap! db swap-fn (-> new-account :id) new-account))

(set-account accounts
             (new-account (random-uuid) 100.0M 1000M 500M))

(map (fn [a] (vector (:id a) (-> a :balances :food) (-> a :balances :meal) (-> a :balances :cash)))
     (map second (into [] @accounts)))
(map (fn [a] a) @accounts)
(into [] @accounts)
;; {#uuid "dea277dc-97f3-494f-b920-d8bc650be6d2"}
(keyword (str #uuid "dea277dc-97f3-494f-b920-d8bc650be6d2"))
(random-uuid)

(swap! accounts )
