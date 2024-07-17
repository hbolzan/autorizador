(ns autorizador.controller.account
  (:require [autorizador.diplomat.db :as diplomat.db]
            [autorizador.wire.account :as wire.account]
            [schema.core :as s]))

(defn start-up! []
  (diplomat.db/load-db! :accounts)
  (println (->> diplomat.db/databases deref :accounts deref (map first))))

(s/defn one-account! :- (s/maybe wire.account/Account)
  [account-id :- s/Uuid]
  (diplomat.db/one-record :accounts account-id))
