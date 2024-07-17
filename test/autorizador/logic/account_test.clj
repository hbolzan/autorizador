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

(def acc {:id          #uuid "09693c78-9c1d-483b-8306-fb1e430964b3"
          :customer-id #uuid "d740f20a-1074-4db0-896d-fa2418e8cf34"
          :transactions
          [{:id        #uuid "4dae55f1-f425-4784-b887-b5429700fb95"
            :source-id #uuid "1252c9cb-cb9a-45a5-9cf0-f60f2803e1c1"
            :amount    -10.00M
            :category  :food}]
          :balances    {:food 10.00M :meal 0.00M :cash 0.00M}})

(def tr #:transaction{:id         #uuid "fad45ef5-8035-4302-9f42-fa8eb06e7467"
                      :account-id #uuid "b05f3600-167d-42a9-9812-c64db1ec0d80"
                      :amount     8.50M
                      :mcc        :5412
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

(def mocked-random-uuid (constantly #uuid "de622c76-e119-4682-b5ff-5a6fa6eb353b"))

(deftest with-new-transaction
  (testing "Append a transaction account transaction list with the category mapped by the mcc"
    (is (= (:transactions acc+tr)
           (logic.account/with-new-transaction (:transactions acc) tr mocked-random-uuid)))))

(deftest with-update-balance
  (testing "Updates balance in account"
    (is (= (:balances acc+tr) (:balances (logic.account/with-updated-balance acc tr))))))

(deftest updated-account
  (testing "Append transaction and update balances"
    (is (= acc+tr (logic.account/updated-account acc tr mocked-random-uuid)))))
