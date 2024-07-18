(ns autorizador.logic.transaction
  (:require [autorizador.model.transaction :as model.transaction]
            [autorizador.wire.account :as wire.account]
            [autorizador.wire.merchant :as wire.merchant]
            [schema.core :as s]))

(def mcc->category {:5411 :food
                    :5412 :food
                    :5811 :meal
                    :5812 :meal
                    :else :cash})

(s/defn  transaction->benefit-category :- wire.account/BenefitCategory
  [{transaction-mcc :transaction/mcc merchant :transaction/merchant} :- model.transaction/Transaction]
  (let [mcc (or (:mcc merchant) transaction-mcc)]
    (or (get mcc->category mcc) (:else mcc->category))))
