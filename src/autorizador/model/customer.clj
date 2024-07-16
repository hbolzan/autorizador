(ns autorizador.model.customer
  (:require [schema.core :as s]))

(s/defschema Customer
  #:customer{:id s/Uuid})
