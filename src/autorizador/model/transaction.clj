(ns autorizador.model.transaction
  (:require [autorizador.model.account :as model.account]
            [schema.core :as s])
  (:import [java.math BigDecimal]))

(s/defschema Transaction
  #:transaction {:id                       s/Uuid
                 :account-id               s/Uuid
                 (s/optional-key :account) model.account/Account
                 :amount                   BigDecimal
                 :mcc                      s/Keyword
                 :merchant                 s/Str})
