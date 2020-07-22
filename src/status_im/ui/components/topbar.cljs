(ns status-im.ui.components.topbar
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [reagent.core :as reagent]
            [quo.core :as quo]
            [status-im.utils.label :as utils.label]))

(defn default-navigation [modal?]
  {:icon                (if modal? :main-icons/close :main-icons/arrow-left)
   :accessibility-label :back-button
   :handler             #(re-frame/dispatch [:navigate-back])})

(defn container [style title-padding & children]
  (into []
        (concat
         [react/view {:style     style
                      :on-layout #(reset! title-padding (max (-> ^js % .-nativeEvent .-layout .-width)
                                                             @title-padding))}]
         children)))

(defn button [value nav?]
  (let [{:keys [handler icon label accessibility-label]} value]
    [react/view {:padding-horizontal (if nav? 8 0)}
     [quo/button (merge {:on-press #(when handler (handler))}
                        (cond
                          icon  {:type  :icon
                                 :theme :icon}
                          label {:type :secondary})
                        (when accessibility-label
                          {:accessibility-label accessibility-label}))
      (cond
        icon  icon
        label (utils.label/stringify label))]]))

(def default-title-padding 16)
;; TODO(Ferossgp): Tobbar should handle safe area
(defn topbar [{:keys [initial-title-padding]}]
  (let [title-padding (reagent/atom (or initial-title-padding default-title-padding))]
    (fn [& [{:keys [title navigation accessories show-border? modal? content]}]]
      (let [navigation (or navigation (default-navigation modal?))]
        [react/view (cond-> {:height 56 :align-items :center :flex-direction :row}
                      show-border?
                      (assoc :border-bottom-width 1 :border-bottom-color colors/gray-lighter))
         (when-not (= navigation :none)
           [container {} title-padding
            [button navigation true]])
         [react/view {:flex 1}]
         (when accessories
           [container {:flex-direction :row :padding-right 6} title-padding
            (for [value accessories]
              ^{:key value}
              [button value false])])
         (when content
           [react/view {:position :absolute :left @title-padding :right @title-padding
                        :top 0 :bottom 0}
            content])
         (when title
           [react/view {:position :absolute :left @title-padding :right @title-padding
                        :top 0 :bottom 0 :align-items :center :justify-content :center}
            [react/text {:style {:typography :title-bold :text-align :center} :number-of-lines 2}
             (utils.label/stringify title)]])]))))
