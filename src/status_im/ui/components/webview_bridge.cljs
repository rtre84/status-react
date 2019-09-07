(ns status-im.ui.components.webview-bridge
  (:require [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [reagent.core :as reagent.core]
            [status-im.utils.platform :as platform]
            [status-im.utils.config :as config]
            ["react-native" :as react-native]
            ["react-native-webview-bridge" :default webview-bridge]))

(def webview-bridge-class
  (memoize
   (fn []
     (reagent/adapt-react-class webview-bridge))))

(defn module [] (.-WebViewBridgeModule (.-NativeModules react-native)))

(defn webview-bridge [{:keys [dapp? dapp-name] :as opts}]
  (if (and config/cached-webviews-enabled? platform/android? dapp?)
    (reagent.core/create-class
     (let [dapp-name-sent? (reagent.core/atom false)]
       {:component-did-mount
        (fn []
          ;; unfortunately it's impossible to pass some initial params
          ;; to view, that's why we have to pass dapp-name to the module
          ;; before showing webview
          (.setCurrentDapp (module) dapp-name
                           (fn [] (reset! dapp-name-sent? true))))
        :reagent-render
        (fn [opts]
          (when @dapp-name-sent?
            [(webview-bridge-class) opts]))}))
    [(webview-bridge-class) opts]))
