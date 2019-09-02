(ns status-im.ens.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.ethereum.contracts :as contracts]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.resolver :as resolver]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.signing.core :as signing]
            [status-im.ethereum.eip55 :as eip55]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [name]))

(defn fullname [custom-domain? username]
  (if custom-domain?
    username
    (stateofus/subdomain username)))

(re-frame/reg-fx
 :ens/resolve-address
 (fn [[registry name cb]]
   (ens/get-addr registry name cb)))

(re-frame/reg-fx
 :ens/resolve-pubkey
 (fn [[registry name cb]]
   (resolver/pubkey registry name cb)))

(defn assoc-details-for [db username k v]
  (assoc-in db [:ens/names :details username k] v))

(fx/defn set-state
  {:events [::name-resolved]}
  [{:keys [db]} username state]
  (when (= username
           (get-in db [:ens/registration :username]))
    {:db (assoc-in db [:ens/registration :state] state)}))

(fx/defn on-resolver-found
  {:events [::resolver-found]}
  [{:keys [db] :as cofx} resolver-contract]
  (let [{:keys [state username custom-domain?]} (:ens/registration db)
        {:keys [public-key]} (:multiaccount db)
        {:keys [x y]} (ethereum/coordinates public-key)
        namehash (ens/namehash (str username (when-not custom-domain?
                                               ".stateofus.eth")))]
    (signing/eth-transaction-call
     cofx
     {:contract   resolver-contract
      :method     "setPubkey(bytes32,bytes32,bytes32)"
      :params     [namehash x y]
      :on-result  [::save-username custom-domain? username]
      :on-error   [:ens/on-registration-failure]})))

(fx/defn save-username
  {:events [::save-username]}
  [{:keys [db] :as cofx} custom-domain? username]
  (let [name   (fullname custom-domain? username)
        names  (get-in db [:multiaccount :usernames] [])
        new-names (conj names name)]
    (multiaccounts.update/multiaccount-update cofx
                                              (cond-> {:usernames new-names}
                                                (empty? names) (assoc :preferred-name name))
                                              {:on-success #(re-frame/dispatch [::username-saved])})))

(fx/defn on-input-submitted
  {:events [::input-submitted ::input-icon-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [state username custom-domain?]} (:ens/registration db)
        registry-contract (get ens/ens-registries (ethereum/chain-keyword db))
        ens-name (str username (when-not custom-domain?
                                 ".stateofus.eth"))]
    (case state
      (:available :owned)
      (navigation/navigate-to-cofx cofx :ens-checkout {})
      :connected-with-different-key
      (ens/resolver registry-contract ens-name
                    #(re-frame/dispatch [::resolver-found %]))
      :connected
      (save-username cofx custom-domain? username)
      ;; for other states, we do nothing
      nil)))

(fx/defn username-saved
  {:events [::username-saved]}
  [{:keys [db] :as cofx}]
  (navigation/navigate-to-cofx cofx :ens-confirmation {}))

(defn- on-resolve
  [registry custom-domain? username address public-key response]
  (cond
    ;; if we get an address back, we try to get the public key associated
    ;; with the username as well
    (= (eip55/address->checksum address)
       (eip55/address->checksum response))
    (resolver/pubkey registry (fullname custom-domain? username)
                     #(re-frame/dispatch [::name-resolved username
                                          (cond
                                            (not public-key) :owned
                                            (= % public-key) :connected
                                            :else :connected-with-different-key)]))

    ;; No address for a stateofus subdomain: it can be registered
    (and (nil? response) (not custom-domain?))
    (re-frame/dispatch [::name-resolved username :available])

    :else
    (re-frame/dispatch [::name-resolved username :taken])))

(fx/defn register-name
  {:events [::register-name-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [amount contract custom-domain? username address public-key]}
        (:ens/registration db)
        {:keys [x y]} (ethereum/coordinates public-key)]
    (re-frame/dispatch [::save-username custom-domain? username])
    #_(signing/eth-transaction-call
       cofx
       {:contract   (contracts/get-address db :status/snt)
        :method     "approveAndCall(address,uint256,bytes)"
        :params     [contract
                     (money/unit->token amount 18)
                     (abi-spec/encode "register(bytes32,address,bytes32,bytes32)"
                                      [(ethereum/sha3 username) address x y])]
        :on-result  [::save-username custom-domain? username]
        :on-error   [:ens/on-registration-failure]})))

(defn- valid-custom-domain? [username]
  (and (ens/is-valid-eth-name? username)
       (stateofus/lower-case? username)))

(defn- valid-username? [custom-domain? username]
  (if custom-domain?
    (valid-custom-domain? username)
    (stateofus/valid-username? username)))

(defn- state [custom-domain? username]
  (cond
    (or (string/blank? username)
        (> 4 (count username))) :too-short
    (valid-username? custom-domain? username) :searching
    :else :invalid))

(fx/defn set-username-candidate
  {:events [::set-username-candidate]}
  [{:keys [db]} username]
  (let [{:keys [custom-domain?]} (:ens/registration db)
        state  (state custom-domain? username)]
    (merge
     {:db (-> db
              (assoc-in [:ens/registration :username] username)
              (assoc-in [:ens/registration :state] state))}
     (when (= state :searching)
       (let [{:keys [multiaccount]} db
             {:keys [public-key]} multiaccount
             address (ethereum/default-address db)
             registry (get ens/ens-registries (ethereum/chain-keyword db))]
         {:ens/resolve-address [registry
                                (fullname custom-domain? username)
                                #(on-resolve registry custom-domain? username address public-key %)]})))))

(fx/defn return-to-ens-main-screen
  {:events [::got-it-pressed ::cancel-pressed]}
  [{:keys [db] :as cofx} _]
  (fx/merge cofx
            ;; clear registration data
            {:db (dissoc db :ens/registration)}
            ;; we reset navigation so that navigate back doesn't return
            ;; into the registration flow
            (navigation/navigate-reset {:index   1
                                        :key     :profile-stack
                                        :actions [{:routeName :my-profile}
                                                  {:routeName :ens-main}]})))

(fx/defn switch-domain-type
  {:events [::switch-domain-type]}
  [{:keys [db] :as cofx} _]
  (fx/merge cofx
            {:db (update-in db [:ens/registration :custom-domain?] not)}
            (set-username-candidate (get-in db [:ens/registration :username] ""))))

(fx/defn save-preferred-name
  {:events [::save-preferred-name]}
  [{:keys [db] :as cofx} name]
  (multiaccounts.update/multiaccount-update cofx
                                            {:preferred-name name}
                                            {}))

(fx/defn switch-show-username
  {:events [:ens/switch-show-username]}
  [{:keys [db] :as cofx} _]
  (let [show-name? (not (get-in db [:multiaccount :show-name?]))]
    (multiaccounts.update/multiaccount-update cofx
                                              {:show-name? show-name?}
                                              {})))

(fx/defn on-registration-failure
  "TODO not sure there is actually anything to do here
   it should only be called if the user cancels the signing
   Actual registration failure has not been implemented properly"
  {:events [:ens/on-registration-failure]}
  [{:keys [db]} username])

(fx/defn store-name-detail
  {:events [:ens/store-name-detail]}
  [{:keys [db]} name k v]
  {:db (assoc-details-for db name k v)})

(fx/defn navigate-to-name
  {:events [:ens/navigate-to-name]}
  [{:keys [db] :as cofx} name]
  (let [registry (get ens/ens-registries (ethereum/chain-keyword db))]
    (fx/merge cofx
              {:ens/resolve-address [registry name #(re-frame/dispatch [:ens/store-name-detail name :address %])]
               :ens/resolve-pubkey  [registry name #(re-frame/dispatch [:ens/store-name-detail name :public-key %])]}
              (navigation/navigate-to-cofx :ens-name-details name))))

(fx/defn start-registration
  {:events [::add-username-pressed ::get-started-pressed]}
  [{:keys [db] :as cofx}]
  (navigation/navigate-to-cofx cofx :ens-search {}))
