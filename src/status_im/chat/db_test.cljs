(ns status-im.chat.db-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.db :as db]))

(deftest group-chat-name
  (testing "it prepends # if it's a public chat"
    (is (= "#withhash" (db/group-chat-name {:group-chat true
                                            :chat-id    "1"
                                            :public?    true
                                            :name       "withhash"}))))
  (testing "it leaves the name unchanged if it's a group chat"
    (is (= "unchanged" (db/group-chat-name {:group-chat true
                                            :chat-id    "1"
                                            :name       "unchanged"})))))

(deftest intersperse-datemarks
  (testing "it mantains the order even when timestamps are across days"
    (let [message-1 {:datemark "Dec 31, 1999"
                     :whisper-timestamp 946641600000} ; 1999}
          message-2 {:datemark "Jan 1, 2000"
                     :whisper-timestamp 946728000000} ; 2000 this will displayed in 1999
          message-3 {:datemark "Dec 31, 1999"
                     :whisper-timestamp 946641600000} ; 1999
          message-4 {:datemark "Jan 1, 2000"
                     :whisper-timestamp 946728000000} ; 2000
          ordered-messages [message-4
                            message-3
                            message-2
                            message-1]
          [m1 d1 m2 m3 m4 d2] (db/add-datemarks ordered-messages)]
      (is (= "Jan 1, 2000"
             (:datemark m1)))
      (is (= {:type :datemark
              :value "Jan 1, 2000"} d1))
      (is (= "Dec 31, 1999"
             (:datemark m2)
             (:datemark m3)
             (:datemark m4)))
      (is (= {:type :datemark
              :value "Dec 31, 1999"} d2)))))

(deftest add-gaps
  (testing "empty state"
    (is (empty?
         (db/add-gaps
          nil
          nil
          nil
          false
          false))))
  (testing "empty state pub-chat"
    (is (=
         [{:type       :gap
           :value      ":first-gap"
           :first-gap? true}]
         (db/add-gaps
          nil
          nil
          {:lowest-request-from 10
           :highest-request-to  30}
          true
          true))))
  (testing "simple case with gap"
    (is (= '({:whisper-timestamp 40000
              :message-id        :m4
              :timestamp         40000}
             {:type  :gap
              :value ":gapid1"
              :gaps  {:ids [:gapid1]}}
             {:whisper-timestamp 30000
              :timestamp         30000
              :message-id        :m3}
             {:value             "today"
              :type              :datemark
              :whisper-timestamp 30000
              :timestamp         30000}
             {:whisper-timestamp 20000
              :timestamp         20000
              :message-id        :m2}
             {:whisper-timestamp 10000
              :timestamp         10000
              :message-id        :m1}
             {:value             "yesterday"
              :type              :datemark
              :whisper-timestamp 10000
              :timestamp         10000})
           (db/add-gaps
            [{:message-id        :m4
              :whisper-timestamp 40000
              :timestamp         40000}
             {:message-id        :m3
              :whisper-timestamp 30000
              :timestamp         30000}
             {:type :datemark
              :value "today"
              :whisper-timestamp 30000
              :timestamp 30000}
             {:message-id        :m2
              :whisper-timestamp 20000
              :timestamp         20000}
             {:message-id        :m1
              :whisper-timestamp 10000
              :timestamp         10000}
             {:type :datemark
              :value "yesterday"
              :whisper-timestamp 10000
              :timestamp 10000}]
            [{:from 25
              :to   30
              :id   :gapid1}]
            nil
            nil
            nil))))
  (testing "simple case with gap after all messages"
    (is (= '({:type  :gap
              :value ":gapid1"
              :gaps  {:ids (:gapid1)}}
             {:whisper-timestamp 40000
              :message-id        :m4
              :timestamp         40000}
             {:whisper-timestamp 30000
              :message-id        :m3
              :timestamp         30000}
             {:value             "today"
              :type              :datemark
              :whisper-timestamp 30000
              :timestamp         30000}
             {:whisper-timestamp 20000
              :message-id        :m2
              :timestamp         20000}
             {:whisper-timestamp 10000
              :message-id        :m1
              :timestamp         10000}
             {:value             "yesterday"
              :type              :datemark
              :whisper-timestamp 10000
              :timestamp         10000})
           (db/add-gaps
            [{:message-id        :m4
              :whisper-timestamp 40000
              :timestamp         40000}
             {:message-id        :m3
              :whisper-timestamp 30000
              :timestamp         30000}
             {:type :datemark
              :value "today"
              :whisper-timestamp 30000
              :timestamp         30000}
             {:message-id        :m2
              :whisper-timestamp 20000
              :timestamp         20000}
             {:message-id        :m1
              :whisper-timestamp 10000
              :timestamp         10000}
             {:type :datemark
              :value "yesterday"
              :whisper-timestamp 10000
              :timestamp         10000}]
            [{:from 100
              :to   110
              :id   :gapid1}]
            nil
            nil
            nil)))))
