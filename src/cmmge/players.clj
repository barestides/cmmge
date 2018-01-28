(ns cmmge.players
  (:require [overtone.live :refer :all]))

(defn melody-player [nome beat melody instm]
  (let [{:keys [pitch amp duration]} (first melody)
        next (+ beat duration)]
    (at (nome beat) (instm :freq (midi->hz (note pitch)) :vol amp))
    (when (not-empty (rest melody))
      (apply-by (nome next) #'melody-player [nome next (rest melody) instm]))))
