(ns autorizador.wire.transaction
  (:require [schema.core :as s])
  (:import [java.math BigDecimal]))

(s/defschema Transaction
  {:transaction {:id         s/Uuid
                 :account-id s/Uuid
                 :amount     BigDecimal
                 :merchant   s/Str
                 :mcc        s/Keyword}})

(s/defschema Authorization
  {:authorization {:code (s/enum :00 :51 :07)}})
