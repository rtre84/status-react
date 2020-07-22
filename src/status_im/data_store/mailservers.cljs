(ns status-im.data-store.mailservers
  (:require [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn mailserver-request-gaps->rpc
  [{:keys [chat-id] :as gap}]
  (-> gap
      (assoc :chatId chat-id)
      (dissoc :chat-id)))

(fx/defn load-gaps
  [cofx chat-id success-fn]
  {::json-rpc/call [{:method "mailservers_getMailserverRequestGaps"
                     :params [chat-id]
                     :on-success #(let [indexed-gaps (reduce (fn [acc {:keys [id] :as g}]
                                                               (assoc acc id g))
                                                             {}
                                                             %)]
                                    (success-fn chat-id indexed-gaps))
                     :on-failure #(log/error "failed to fetch gaps" %)}]})

(fx/defn save-gaps
  [cofx gaps]
  {::json-rpc/call [{:method "mailservers_addMailserverRequestGaps"
                     :params [(map mailserver-request-gaps->rpc gaps)]
                     :on-success #(log/info "saved gaps successfully")
                     :on-failure #(log/error "failed to save gap" %)}]})

(fx/defn delete-gaps
  [cofx ids]
  {::json-rpc/call [{:method "mailservers_deleteMailserverRequestGaps"
                     :params [ids]
                     :on-success #(log/info "deleted gaps successfully")
                     :on-failure #(log/error "failed to delete gap" %)}]})

(fx/defn delete-gaps-by-chat-id
  [cofx chat-id]
  {::json-rpc/call [{:method "mailservers_deleteMailserverRequestGapsByChatID"
                     :params [chat-id]
                     :on-success #(log/info "deleted gaps successfully")
                     :on-failure #(log/error "failed to delete gap" %)}]})
