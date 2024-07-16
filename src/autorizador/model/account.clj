(ns autorizador.model.account
  (:require [autorizador.model.customer :as model.customer]
            [schema.core :as s])
  (:import [java.math BigDecimal]))

(def benefit-categories #{:food :meal :cash})
(def BenefitCategory (apply s/enum benefit-categories))

(s/defschema Transaction
  #:transaction{:id        s/Uuid
                :source-id s/Uuid
                :amount    BigDecimal
                :category  BenefitCategory})

(s/defschema Account
  #:account{:id           s/Uuid
            :customer     model.customer/Customer
            :transactions [Transaction]})
