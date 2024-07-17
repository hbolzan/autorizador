(ns autorizador.wire.account
  (:require [autorizador.model.account :as model.account]
            [schema.core :as s])
  (:import [java.math BigDecimal]))

(s/defschema Balances
  {:food BigDecimal
   :meal BigDecimal
   :cash BigDecimal})

(s/defschema Account
  {:id          s/Uuid
   :customer-id s/Uuid
   :balances    Balances})
