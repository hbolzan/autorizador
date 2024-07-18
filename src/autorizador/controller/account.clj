(ns autorizador.controller.account
  (:require [autorizador.diplomat.db :as diplomat.db]
            [autorizador.wire.account :as wire.account]
            [schema.core :as s]))

(defn start-up! []
  (diplomat.db/load-db! :accounts))

(defn all! []
  (->> diplomat.db/databases deref :accounts deref))

(s/defn one! :- (s/maybe wire.account/Account)
  [id :- s/Uuid]
  (diplomat.db/one-record :accounts id))
