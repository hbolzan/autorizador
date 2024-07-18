(ns autorizador.logic.transaction-test
  (:require [clojure.test :refer [deftest is are testing use-fixtures]]
            [autorizador.logic.transaction :as logic.transaction]
            [schema.test :as st]))

(use-fixtures :once st/validate-schemas)

(def merchant-5411 {:id "PAG*JoseDaSilva          RIO DE JANEI BR" :mcc :5411})
(def merchant-5812 {:id "UBER EATS                   SAO PAULO BR" :mcc :5812})
(def merchant-9999 {:id "UBER TRIP                   SAO PAULO BR" :mcc :9999})

(defn transaction [mcc merchant]
  #:transaction {:id         #uuid "bc71f950-ef3f-46ff-a71d-838a3ec85012"
                 :account-id #uuid "09a06fdb-20e4-4c72-9a69-68b7c97df23b"
                 :amount     123.45M
                 :mcc        mcc
                 :merchant   (or (:id merchant) "PADARIA DO ZE               SAO PAULO BR")})

(deftest transaction->benefit-category
  (testing "Maps transaction mcc to benefit category or cash")
  (are [?mcc ?merchant ?category]
       (= ?category (logic.transaction/transaction->benefit-category (transaction ?mcc ?merchant) ?merchant))
    :5411 nil           :food
    :5412 nil           :food
    :5811 nil           :meal
    :5812 nil           :meal
    :1234 nil           :cash
    :9999 nil           :cash
    :9999 merchant-5812 :meal
    :5411 merchant-5812 :meal
    :5412 merchant-9999 :cash
    :5811 merchant-5411 :food))
