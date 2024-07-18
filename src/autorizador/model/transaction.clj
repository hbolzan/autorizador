(ns autorizador.model.transaction
  (:require [autorizador.wire.merchant :as wire.merchant]
            [schema.core :as s])
  (:import [java.math BigDecimal]))

(s/defschema Transaction
  #:transaction {:id                                    s/Uuid
                 :account-id                            s/Uuid
                 :amount                                BigDecimal
                 :mcc                                   s/Keyword
                 :merchant-id                           s/Str
                 (s/optional-key :transaction/merchant) wire.merchant/Merchant})
