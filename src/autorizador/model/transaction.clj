(ns autorizador.model.transaction
  (:require [schema.core :as s])
  (:import [java.math BigDecimal]))

(s/defschema Transaction
  #:transaction {:id                       s/Uuid
                 :account-id               s/Uuid
                 :amount                   BigDecimal
                 :mcc                      s/Keyword
                 :merchant                 s/Str})
