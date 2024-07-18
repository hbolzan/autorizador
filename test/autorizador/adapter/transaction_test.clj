(ns autorizador.adapter.transaction-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [autorizador.adapter.transaction :as adapter.transaction]
            [schema.test :as st]))

(use-fixtures :once st/validate-schemas)

(def customer #:customer {:id #uuid "42524b09-f172-48cc-a406-f80161c9cf5e"})
(def account #:account{:id           #uuid "09a06fdb-20e4-4c72-9a69-68b7c97df23b"
                       :customer-id  (:id customer)
                       :transactions []})

(def wire {:transaction {:id         #uuid "bc71f950-ef3f-46ff-a71d-838a3ec85012"
                         :account-id #uuid "09a06fdb-20e4-4c72-9a69-68b7c97df23b"
                         :amount     123.45M
                         :merchant   "PADARIA DO ZE               SAO PAULO BR"
                         :mcc        :5811}})

(def internal #:transaction{:id         #uuid "bc71f950-ef3f-46ff-a71d-838a3ec85012"
                            :account-id #uuid "09a06fdb-20e4-4c72-9a69-68b7c97df23b"
                            :amount     123.45M
                            :mcc        :5811
                            :merchant   "PADARIA DO ZE               SAO PAULO BR"})

(deftest wire->internal
  (testing "Adapts wire transaction to internal model"
    (is (= internal
           (adapter.transaction/wire->internal wire)))))
