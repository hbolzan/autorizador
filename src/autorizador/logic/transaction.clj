(ns autorizador.logic.transaction
  (:require [schema.core :as s]
            [autorizador.wire.account :as wire.account]
            [autorizador.model.transaction :as model.transaction]))

(def mcc->category {:5411 :food
                    :5412 :food
                    :5811 :meal
                    :5812 :meal
                    :else :cash})

(s/defn  transaction->benefit-category :- wire.account/BenefitCategory
  [{mcc :transaction/mcc} :- model.transaction/Transaction]
  (or (get mcc->category mcc) (:else mcc->category)))
