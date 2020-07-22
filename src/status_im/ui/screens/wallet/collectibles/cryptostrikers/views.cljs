(ns status-im.ui.screens.wallet.collectibles.cryptostrikers.views
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]
            [status-im.ui.components.svgimage :as svgimage]
            [status-im.ui.screens.wallet.collectibles.views :as collectibles]
            [status-im.i18n :as i18n]
            [quo.core :as quo]))

(defmethod collectibles/render-collectible :STRK [_ {:keys [external_url description name image]}]
  [react/view {:style styles/details}
   [react/view {:style styles/details-text}
    [svgimage/svgimage {:style  styles/details-image
                        :source {:uri image
                                 :k   1.4}}]
    [react/view {:flex 1 :justify-content :center}
     [react/text {:style styles/details-name}
      name]
     [react/text
      description]]]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/view-cryptostrikers)
     :icon                :main-icons/address
     :accessibility-label :open-collectible-button
     :on-press            #(re-frame/dispatch [:open-collectible-in-browser external_url])}]])
