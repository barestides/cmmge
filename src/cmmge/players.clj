(ns cmmge.players
  (:require [overtone.live :refer :all]))

(defn melody-player [nome beat melody instm]
  (let [{:keys [pitch amp duration]} (first melody)
        next (+ beat duration)]
    (at (nome beat) (instm :freq (midi->hz (note pitch)) :vol amp))
    (when (not-empty (rest melody))
      (apply-by (nome next) #'melody-player [nome next (rest melody) instm]))))

(defn play-one-chord
  [chord instm]
  (dorun
   (map instm chord)))

(defn chord-player [nome beat progression instm]
  (let [next (inc beat)]
    (at (nome beat) (play-one-chord (first progression) instm))
    (prn (first progression))
    (when (not-empty (rest progression))
      (apply-by (nome next) #'chord-player [nome next (rest progression) instm]))))
