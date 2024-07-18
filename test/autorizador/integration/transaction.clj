(ns autorizador.integration.transaction
  (:require [autorizador.integration.aux.db :as aux.db]
            [autorizador.server :as server]
            [cheshire.core :as json]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [org.httpkit.client :as http]))

(defonce server (atom nil))
(def server-port 8839)

(use-fixtures :once
  (fn [tests]
    (reset! server (server/start-server server-port))
    (try
      (tests)
      (finally
        (server/stop-server @server)))))

(defn url [path]
  (format "http://localhost:%s/api/v1%s" server-port path))

(defn http-get [path]
  (-> @(http/get (url path)) :body (json/parse-string keyword)))

(defn http-post [path body]
  (let [response @(http/post (url path) {:body body})]
    (select-keys (assoc response :body (-> response :body (json/parse-string keyword)))
                 [:status :body])))
(defn transaction
  [account-id amount mcc merchant]
  {:transaction {:id         (random-uuid)
                 :account-id account-id
                 :amount     amount
                 :merchant   merchant
                 :mcc        mcc}})

(defn bad-transaction
  [account-id amount mcc merchant]
  {:transaction {:id         (random-uuid)
                 :account-id account-id
                 :amount     amount
                 :merchant   merchant
                 :mzc        mcc}})

(defn transaction-body
  ([account-id amount mcc] (transaction-body account-id amount mcc "PADARIA DO ZE               SAO PAULO BR"))
  ([account-id amount mcc merchant]
   (json/generate-string
    (transaction account-id amount mcc merchant))))

(defn invalid-transaction-body
  ([account-id amount mcc] (invalid-transaction-body account-id amount mcc "PADARIA DO ZE               SAO PAULO BR"))
  ([account-id amount mcc merchant]
   (json/generate-string
    (bad-transaction account-id amount mcc merchant))))

;; (let [t (transaction (random-uuid) 10.00M :5411 "PADARIA")]
;;   (assoc-in t [:transaction :mxc] (-> t :transaction :mcc)))

(deftest approved-transaction
  (testing "Valid transaction approved"
    (let [{account-id :id} (aux.db/init-accounts-db! 200.00M 300.00M 500.00M)]
      (is (= {:food 200.0, :meal 300.0, :cash 500.0}
             (:balances (http-get (str "/accounts/" account-id))))
          "Account initial balance is set")

      (is (= {:status 200 :body {:authorization {:code "00"}}}
             (http-post "/transaction" (transaction-body account-id 50.00M :5411)))
          "Trasaction approved, response status is 200")

      (is (= {:food 150.0, :meal 300.0, :cash 500.0}
             (:balances (http-get (str "/accounts/" account-id))))
          "Amount was debited from food account"))))

(deftest denied-transaction
  (testing "Valid transaction denied for insufficient funds"
    (let [{account-id :id} (aux.db/init-accounts-db! 200.00M 10.00M 5.00M)]
      (is (= {:food 200.0, :meal 10.0, :cash 5.0}
             (:balances (http-get (str "/accounts/" account-id))))
          "Account initial balance is set")

      (is (= {:status 200 :body {:authorization {:code "51"}}}
             (http-post "/transaction" (transaction-body account-id 50.00M :5811)))
          "Trasaction denied for insufficient funds, response status is 200")

      (is (= {:food 200.0, :meal 10.0, :cash 5.0}
             (:balances (http-get (str "/accounts/" account-id))))
          "Balances did not change"))))

(deftest invalid-transaction
  (testing "Invalid transaction denied for other reasons"
    (let [{account-id :id} (aux.db/init-accounts-db! 200.00M 100.00M 500.00M)]
      (is (= {:food 200.0, :meal 100.0, :cash 500.0}
             (:balances (http-get (str "/accounts/" account-id))))
          "Account initial balance is set")

      (is (= {:status 200 :body {:authorization {:code "07"}}}
             (http-post "/transaction" (invalid-transaction-body account-id 50.00M :5811)))
          "Account is invalid, trasaction was denied with code 07, response status is 200")

      (is (= {:food 200.0, :meal 100.0, :cash 500.0}
             (:balances (http-get (str "/accounts/" account-id))))
          "Balances did not change"))))

(deftest merchant-transaction
  (testing "Valid transaction approved"
    (let [{account-id :id} (aux.db/init-accounts-db! 200.00M 300.00M 500.00M)
          merchant (aux.db/init-merchants-db! "BAR DA ESQUINA" :5812)]
      (is (= {:food 200.0, :meal 300.0, :cash 500.0}
             (:balances (http-get (str "/accounts/" account-id))))
          "Account initial balance is set")

      (is (= {:status 200 :body {:authorization {:code "00"}}}
             (http-post "/transaction" (transaction-body account-id 250.00M :5411 (:id merchant))))
          "Trasaction approved, response status is 200")

      (is (= {:food 200.0, :meal 50.0, :cash 500.0}
             (:balances (http-get (str "/accounts/" account-id))))
          "Amount was debited from meal account because of merchant mcc precedence"))))

(deftest fallback-transaction
  (testing "Valid transaction approved using cash fallback"
    (let [{account-id :id} (aux.db/init-accounts-db! 200.00M 300.00M 500.00M)]
      (is (= {:food 200.0, :meal 300.0, :cash 500.0}
             (:balances (http-get (str "/accounts/" account-id))))
          "Account initial balance is set")

      (is (= {:status 200 :body {:authorization {:code "00"}}}
             (http-post "/transaction" (transaction-body account-id 250.00M :5411)))
          "Trasaction approved, response status is 200")

      (is (= {:food 0.0, :meal 300.0, :cash 450.0}
             (:balances (http-get (str "/accounts/" account-id))))
          "Amount was debited from food and cash balances"))))
