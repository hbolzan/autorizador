(ns autorizador.logic.transaction-test
  (:require [clojure.test :refer [deftest is are testing use-fixtures]]
            [autorizador.logic.transaction :as logic.transaction]
            [schema.test :as st]))

(use-fixtures :once st/validate-schemas)

(defn transaction [mcc]
  #:transaction {:id         #uuid "bc71f950-ef3f-46ff-a71d-838a3ec85012"
                 :account-id #uuid "09a06fdb-20e4-4c72-9a69-68b7c97df23b"
                 :amount     123.45M
                 :mcc        mcc
                 :merchant   "PADARIA DO ZE               SAO PAULO BR"})

(deftest transaction->benefit-category
  (testing "Maps transaction mcc to benefit category or cash")
  (are [?mcc ?category] (= ?category (logic.transaction/transaction->benefit-category (transaction ?mcc)))
    :5411 :food
    :5412 :food
    :5811 :meal
    :5812 :meal
    :1234 :cash
    :9999 :cash))
