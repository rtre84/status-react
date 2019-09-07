(ns status-im.utils.platform
  (:require [status-im.android.platform :as android]
            [status-im.ios.platform :as ios]
            [status-im.desktop.platform :as desktop]
            ["react-native" :as react-native]
            ["react-native-fs" :as react-native-fs]))

(def platform
  (.-Platform react-native))

(def os
  (when platform
    (.-OS platform)))

(def version
  (when platform
    (.-Version platform)))

(def android? (= os "android"))
(def ios? (= os "ios"))
(def desktop? (= os "desktop"))
(def mobile? (not= os "desktop"))
(def iphone-x? (and ios? (ios/iphone-x-dimensions?)))

(def isMacOs? (when platform (.-isMacOs platform)))
(def isNix? (when platform (or (.-isLinux platform) (.-isUnix platform))))
(def isWin? (when platform (.-isWin platform)))

(def platform-specific
  (cond
    android? android/platform-specific
    ios? ios/platform-specific
    :else desktop/platform-specific))

(defn no-backup-directory []
  (cond
    android? (str (.-DocumentDirectoryPath react-native-fs)
                  "/../no_backup")
    ios?          (.-LibraryDirectoryPath react-native-fs)))

(defn android-version>= [v]
  (and android? (>= version v)))
