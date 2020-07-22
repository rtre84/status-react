(ns status-im.ui.screens.fleet-settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.node.core :as node]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.fleet-settings.styles :as styles])
  (:require-macros [status-im.utils.views :as views]))

(defn- fleet-icon [current?]
  [react/view (styles/fleet-icon-container current?)
   [vector-icons/icon :main-icons/mailserver
    (styles/fleet-icon current?)]])

(defn change-fleet [fleet]
  (re-frame/dispatch [:fleet.ui/fleet-selected fleet]))

(defn render-row [current-fleet]
  (fn [fleet]
    (let [current? (= fleet current-fleet)]
      [react/touchable-highlight
       {:on-press #(change-fleet fleet)
        :accessibility-label :fleet-item}
       [react/view styles/fleet-item
        [fleet-icon current?]
        [react/view styles/fleet-item-inner
         [react/text {:style styles/fleet-item-name-text}
          fleet]]]])))

(defn fleets [custom-fleets]
  (map name (keys (node/fleets {:custom-fleets custom-fleets}))))

(views/defview fleet-settings []
  (views/letsubs [custom-fleets [:fleets/custom-fleets]
                  current-fleet [:fleets/current-fleet]]
    [react/view {:flex 1}
     [topbar/topbar {:title :t/fleet-settings}]
     [react/view styles/wrapper
      [list/flat-list {:data               (fleets custom-fleets)
                       :default-separator? false
                       :key-fn             identity
                       :render-fn          (render-row (name current-fleet))}]]]))
