(ns autorizador.logic.account-test
  (:require [clojure.test :refer [deftest is are testing use-fixtures]]
            [autorizador.logic.account :as logic.account]
            [schema.test :as st]))

(use-fixtures :once st/validate-schemas)

(defn initial-transactions [food meal cash]
  (let [source-id (random-uuid)
        balances  [{:category :food :amount food} {:category :meal :amount meal} {:category :cash :amount cash}]]
    (reduce (fn [transactions {:keys [category amount]}]
              (if (= 0.00M amount)
                transactions
                (conj transactions
                      {:id        (random-uuid)
                       :source-id source-id
                       :amount    (- amount)
                       :category  category})))
            []
            balances)))

(defn new-account
  [food meal cash]
  {:id           (random-uuid)
   :customer-id  (random-uuid)
   :transactions (initial-transactions food meal cash)
   :balances     {:food food
                  :meal meal
                  :cash cash}})

(defn transaction [mcc amount]
  #:transaction{:id         (random-uuid)
                :account-id (random-uuid)
                :amount     amount
                :mcc        mcc
                :merchant   "PADARIA DO ZE               SAO PAULO BR"})

(deftest sufficient-funds?
  (testing "Checks if there are funds enough in the account for the MCC mapped by the transaction"
    (are [?account ?transaction ?enough] (= ?enough (logic.account/sufficient-funds? ?account ?transaction))
      (new-account 10.00M  0.00M  0.00M) (transaction :5411 10.00M) true
      (new-account 10.00M  0.00M  0.00M) (transaction :5412  8.50M) true
      (new-account  0.00M 10.00M  0.00M) (transaction :5811 10.00M) true
      (new-account  0.00M 10.00M  0.00M) (transaction :5812 10.00M) true
      (new-account  0.00M  0.00M 10.00M) (transaction :9999 10.00M) true
      (new-account  0.00M  0.00M  0.00M) (transaction :5411 10.00M) false
      (new-account 10.00M 10.00M  0.00M) (transaction :9999 10.00M) false
      (new-account 10.00M  0.00M  0.00M) (transaction :5411 10.01M) false
      (new-account 10.00M 10.00M  0.00M) (transaction :5411 15.00M) false
      (new-account 10.00M 10.00M 10.00M) (transaction :5411 15.00M) false)))

(deftest sufficient-funds?-with-falback
  (testing "Try to fund with the category mapped by the transaction with fallback to cash"
    (are [?account ?transaction ?enough] (= ?enough (logic.account/sufficient-funds? ?account ?transaction))
      (new-account 10.00M 20.00M  0.00M) (transaction :5411 20.00M) false
      (new-account 10.00M  0.00M 10.00M) (transaction :5411 20.00M) true
      (new-account  0.00M  0.00M 20.00M) (transaction :5411 20.00M) true
      (new-account  0.00M  0.00M 19.00M) (transaction :5411 20.00M) false
      (new-account  0.00M 10.00M 10.00M) (transaction :5411 20.00M) false)))

(def acc {:id          #uuid "09693c78-9c1d-483b-8306-fb1e430964b3"
          :customer-id #uuid "d740f20a-1074-4db0-896d-fa2418e8cf34"
          :transactions
          [{:id        #uuid "4dae55f1-f425-4784-b887-b5429700fb95"
            :source-id #uuid "1252c9cb-cb9a-45a5-9cf0-f60f2803e1c1"
            :amount    -10.00M
            :category  :food}]
          :balances    {:food 10.00M :meal 0.00M :cash 0.00M}})

(def fb-acc {:id          #uuid "273ac537-fa18-4beb-9265-c55676ea6e4c"
             :customer-id #uuid "32f17b00-8c65-407f-ae67-81f297ad7a14"
             :transactions
             [{:id        #uuid "4b483ca5-36f5-4e1d-aba8-90a8968bee7d"
               :source-id #uuid "27fbde88-5c33-408f-869b-7a12571de541"
               :amount    -10.00M
               :category  :food}
              {:id        #uuid "57d6f160-55bc-4c03-8370-d9f609755baa"
               :source-id #uuid "27fbde88-5c33-408f-869b-7a12571de541"
               :amount    -10.00M
               :category  :cash}]
             :balances    {:food 10.00M :meal 0.00M :cash 10.00M}})

(def tr #:transaction{:id         #uuid "fad45ef5-8035-4302-9f42-fa8eb06e7467"
                      :account-id #uuid "b05f3600-167d-42a9-9812-c64db1ec0d80"
                      :amount     8.50M
                      :mcc        :5412
                      :merchant   "PADARIA DO ZE               SAO PAULO BR"})

(def fb-tr #:transaction{:id         #uuid "24b91168-bb2d-498f-8b45-7bea2e738d43"
                         :account-id #uuid "ea282dc9-c11c-4cff-93af-2b74f505555a"
                         :amount     15.00M
                         :mcc        :5411
                         :merchant   "PADARIA DO ZE               SAO PAULO BR"})

(def acc+tr {:id          #uuid "09693c78-9c1d-483b-8306-fb1e430964b3"
             :customer-id #uuid "d740f20a-1074-4db0-896d-fa2418e8cf34"
             :transactions
             [{:id        #uuid "4dae55f1-f425-4784-b887-b5429700fb95"
               :source-id #uuid "1252c9cb-cb9a-45a5-9cf0-f60f2803e1c1"
               :amount    -10.00M
               :category  :food}
              {:id        #uuid "de622c76-e119-4682-b5ff-5a6fa6eb353b"
               :source-id #uuid "fad45ef5-8035-4302-9f42-fa8eb06e7467"
               :amount    8.50M
               :category  :food}]
             :balances    {:food 1.50M :meal 0.00M :cash 0.00M}})

(def fb-acc+tr {:id          #uuid "273ac537-fa18-4beb-9265-c55676ea6e4c"
                :customer-id #uuid "32f17b00-8c65-407f-ae67-81f297ad7a14"
                :transactions
                [{:id        #uuid "4b483ca5-36f5-4e1d-aba8-90a8968bee7d"
                  :source-id #uuid "27fbde88-5c33-408f-869b-7a12571de541"
                  :amount    -10.00M
                  :category  :food}
                 {:id        #uuid "57d6f160-55bc-4c03-8370-d9f609755baa"
                  :source-id #uuid "27fbde88-5c33-408f-869b-7a12571de541"
                  :amount    -10.00M
                  :category  :cash}
                 {:id        #uuid "de622c76-e119-4682-b5ff-5a6fa6eb353b"
                  :source-id #uuid "24b91168-bb2d-498f-8b45-7bea2e738d43"
                  :amount    10.00M
                  :category  :food}
                 {:id        #uuid "de622c76-e119-4682-b5ff-5a6fa6eb353b"
                  :source-id #uuid "24b91168-bb2d-498f-8b45-7bea2e738d43"
                  :amount    5.00M
                  :category  :cash}]
                :balances    {:food 0.00M :meal 0.00M :cash 5.00M}})

(def mocked-random-uuid (constantly #uuid "de622c76-e119-4682-b5ff-5a6fa6eb353b"))

(deftest with-new-transaction
  (testing "Append a transaction account transaction list with the category mapped by the mcc"
    (is (= (:transactions acc+tr)
           (logic.account/with-new-transaction acc tr mocked-random-uuid))))
  (testing "If funding falls back to cash, append an additional transaction"
    (is (= (:transactions fb-acc+tr)
           (logic.account/with-new-transaction fb-acc fb-tr mocked-random-uuid)))))

(deftest with-update-balance
  (testing "Updates balance in account"
    (is (= (:balances acc+tr) (:balances (logic.account/with-updated-balance acc tr)))))
  (testing "Updates category and fallback balances in account"
    (is (= (:balances fb-acc+tr) (:balances (logic.account/with-updated-balance fb-acc fb-tr))))))

(deftest updated-account
  (testing "Append transaction and update balances"
    (is (= acc+tr (logic.account/updated-account acc tr mocked-random-uuid)))
    (is (= fb-acc+tr (logic.account/updated-account fb-acc fb-tr mocked-random-uuid)))))
