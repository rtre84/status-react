(ns status-im.ui.screens.wallet.accounts.sheets
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn accounts-options [mnemonic]
  (fn []
    [react/view
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/wallet-manage-assets)
       :icon                :main-icons/token
       :accessibility-label :wallet-manage-assets
       :on-press            #(hide-sheet-and-dispatch
                              [:navigate-to :wallet-settings-assets])}]
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/set-currency)
       :icon                :main-icons/language
       :accessibility-label :wallet-set-currency
       :on-press            #(hide-sheet-and-dispatch
                              [:navigate-to :currency-settings])}]
     [quo/list-item
      {:theme    :accent
       :title    (i18n/label :t/view-signing)
       :icon     :main-icons/info
       :on-press #(hide-sheet-and-dispatch
                   [:show-popover {:view :signing-phrase}])}]
     (when mnemonic
       [quo/list-item
        {:theme               :negative
         :title               (i18n/label :t/wallet-backup-recovery-title)
         :icon                :main-icons/security
         :accessibility-label :wallet-backup-recovery-title
         :on-press            #(hide-sheet-and-dispatch
                                [:navigate-to :profile-stack {:screen  :backup-seed
                                                              :initial false}])}])]))

(defn send-receive [account type]
  [react/view
   (when-not (= type :watch)
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/wallet-send)
       :icon                :main-icons/send
       :accessibility-label :send-transaction-button
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet/prepare-transaction-from-wallet account])}])
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/receive)
     :icon                :main-icons/receive
     :accessibility-label :receive-transaction-button
     :on-press            #(hide-sheet-and-dispatch
                            [:show-popover {:view    :share-account
                                            :address (:address account)}])}]])

(defn add-account []
  (let [keycard? @(re-frame/subscribe [:keycard-multiaccount?])]
    [react/view
     [quo/list-item
      {:title               (i18n/label :t/generate-a-new-account)
       :theme               :accent
       :icon                :main-icons/add
       :accessibility-label :add-account-sheet-generate
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet.accounts/start-adding-new-account
                               {:type :generate}])}]
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/add-a-watch-account)
       :icon                :main-icons/show
       :accessibility-label :add-account-sheet-watch
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet.accounts/start-adding-new-account
                               {:type :watch}])}]
     (when-not keycard?
       [quo/list-item
        {:title               (i18n/label :t/enter-a-seed-phrase)
         :theme               :accent
         :icon                :main-icons/text
         :accessibility-label :add-account-sheet-seed
         :on-press            #(hide-sheet-and-dispatch
                                [:wallet.accounts/start-adding-new-account
                                 {:type :seed}])}])
     (when-not keycard?
       [quo/list-item
        {:title               (i18n/label :t/enter-a-private-key)
         :theme               :accent
         :icon                :main-icons/address
         :accessibility-label :add-account-sheet-private-key
         :on-press            #(hide-sheet-and-dispatch
                                [:wallet.accounts/start-adding-new-account
                                 {:type :key}])}])]))

(defn account-settings []
  [react/view
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/account-settings)
     :accessibility-label :account-settings-bottom-sheet
     :icon                :main-icons/info
     :on-press            #(hide-sheet-and-dispatch
                            [:navigate-to :account-settings])}]])
