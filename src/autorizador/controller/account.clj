(ns autorizador.controller.account
  (:require [autorizador.diplomat.db :as diplomat.db]
            [autorizador.wire.account :as wire.account]
            [schema.core :as s]))

(diplomat.db/load-db! :accounts)

(s/defn one-account! :- (s/maybe wire.account/Account)
  [account-id :- s/Uuid]
  (diplomat.db/one-record :accounts account-id))
