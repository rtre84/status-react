(ns status-im.stickers.core-test
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.stickers.core :as stickers]))

(deftest valid-sticker?
  (is (true?  (stickers/valid-sticker? {:hash ""})))
  (is (false? (stickers/valid-sticker? {}))))
