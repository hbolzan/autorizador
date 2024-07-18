(ns autorizador.controller.merchant
  (:require [autorizador.diplomat.db :as diplomat.db]
            [autorizador.wire.merchant :as wire.merchant]
            [schema.core :as s]))

(defn start-up! []
  (diplomat.db/load-db! :merchants))

(defn all! []
  (->> diplomat.db/databases deref :merchants deref))

(s/defn one! :- (s/maybe wire.merchant/Merchant)
  [name :- s/Str]
  (diplomat.db/one-record :merchants name))
