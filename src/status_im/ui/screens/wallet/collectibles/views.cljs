(ns status-im.ui.screens.wallet.collectibles.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as component.styles]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]
            [status-im.ui.components.topbar :as topbar]))

(defmulti render-collectible (fn [symbol _] symbol))

(defmethod render-collectible :default [symbol {:keys [id name]}]
  [react/view {:style styles/default-collectible}
   [react/text (str (clojure.core/name symbol) " #" (or id name))]])

(defview collectibles-list []
  (letsubs [{:keys [name symbol]} [:get-screen-params]
            collectibles [:screen-collectibles]]
    [react/view {:style component.styles/flex}
     [topbar/topbar {:title name}]
     (cond
       (nil? collectibles)
       [react/view {:style styles/loading-indicator}
        [react/activity-indicator {:animating true :size :large :color colors/blue}]]
       (seq collectibles)
       [list/flat-list {:data      collectibles
                        :key-fn    (comp str :id)
                        :render-fn #(render-collectible symbol %)}]
       :else
       ;; Should never happen. Less confusing to debug new NFT support.
       [react/view {:style styles/loading-indicator}
        [react/text (i18n/label :t/error)]])]))

