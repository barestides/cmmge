(ns cmmge.user
  (:require [clojure.repl :refer :all]
            [overtone.music.rhythm :refer :all]
            [overtone.music.pitch :as pitch]
            ;; [cmmge.melodies :as melodies]
            [cmmge.players :refer :all]
            [cmmge.instruments :as insts]
            [cmmge.triadic-transformations :refer :all]))

(def m (metronome 120))

;;maybe this should be called `repl`
