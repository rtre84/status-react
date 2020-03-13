(ns status-im.test.hardwallet.common
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.hardwallet.common :as common]))

(deftest test-show-connection-sheet
  (testing "the card is not connected yet"
    (let [db  {:hardwallet {:card-connected? false}}
          res (common/show-connection-sheet
               {:db db}
               {:on-card-connected :do-something
                :handler           (fn [{:keys [db]}]
                                     {:db (assoc db :some-key :some-value)})})]
      (is (= :do-something
             (get-in res [:db :hardwallet :on-card-connected])))
      (is (nil? (get-in res [:db :some-key])))
      (is (true? (get-in res [:db :bottom-sheet/show?])))))
  (testing "the card is connected before the interaction"
    (let [db  {:hardwallet {:card-connected? true}}
          res (common/show-connection-sheet
               {:db db}
               {:on-card-connected :do-something
                :handler           (fn [{:keys [db]}]
                                     {:db (assoc db :some-key :some-value)})})]
      (is (nil? (get-in res [:db :hardwallet :on-card-connected])))
      (is (= :do-something
             (get-in res [:db :hardwallet :last-on-card-connected])))
      (is (= :some-value (get-in res [:db :some-key])))
      (is (true? (get-in res [:db :bottom-sheet/show?])))))
  (testing "on-card-connected is not specified"
    (is
     (thrown?
      js/Error
      (common/show-connection-sheet
       {:db {}}
       {:handler (fn [_])}))))
  (testing "handler is not specified"
    (is
     (thrown?
      js/Error
      (common/show-connection-sheet
       {:db {}}
       {:on-card-connected :do-something})))))

