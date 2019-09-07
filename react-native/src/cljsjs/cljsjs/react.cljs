(ns cljsjs.react
  (:require ["react-native" :as react-native]))

(when (exists? js/window)
  (set! js/ReactNative react-native))
