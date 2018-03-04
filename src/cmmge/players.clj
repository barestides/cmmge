(ns cmmge.players
  (:require [overtone.live :refer :all]))

(defn melody-player [nome beat melody instm]
  (let [{:keys [pitch amp duration]} (first melody)
        sustain-secs (float (/ (beat-ms duration (metro-bpm nome)) 1000))
        next (+ beat duration)]
    (at (nome beat) (instm :freq (midi->hz (note pitch)) :vol amp :sustain sustain-secs))
    (when (not-empty (rest melody))
      (apply-by (nome next) #'melody-player [nome next (rest melody) instm]))))

(defn play-one-chord
  [chord instm]
  (dorun
   (map instm chord)))

(defn chord-player [nome beat progression instm]
  (let [next (inc beat)]
    (at (nome beat) (play-one-chord (first progression) instm))
    (when (not-empty (rest progression))
      (apply-by (nome next) #'chord-player [nome next (rest progression) instm]))))

(defn phrase-player [nome beat phrases]
  (let [{:keys [melody inst]} (first phrases)
        next-phrase-start (+ beat (apply + (map :duration melody)))
        next-phrases (rest phrases)]
    (melody-player nome beat melody inst)
    (when (not-empty next-phrases)
      (apply-by (nome next-phrase-start) #'phrase-player [nome next-phrase-start next-phrases]))))
