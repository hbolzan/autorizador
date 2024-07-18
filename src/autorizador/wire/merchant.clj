(ns autorizador.wire.merchant
  (:require [schema.core :as s]))

(s/defschema Merchant
  {:id  s/Str
   :mcc s/Keyword})
