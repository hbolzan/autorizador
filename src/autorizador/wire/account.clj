(ns autorizador.wire.account
  (:require [schema.core :as s])
  (:import
   [java.math BigDecimal]))

(def benefit-categories #{:food :meal :cash})
(def BenefitCategory (apply s/enum benefit-categories))

(s/defschema Transaction
  {:id        s/Uuid
   :source-id s/Uuid
   :amount    BigDecimal
   :category  BenefitCategory})

(s/defschema Balances
  {:food BigDecimal
   :meal BigDecimal
   :cash BigDecimal})

(s/defschema Account
  {:id           s/Uuid
   :customer-id  s/Uuid
   :transactions [Transaction]
   :balances     Balances})
