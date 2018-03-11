(ns cmmge.players
  (:require [overtone.live :refer :all]))

(def nice-names->intervals
  {:u 0
   :m2 1
   :M2 2
   :m3 3
   :M3 4
   :p4 5
   :tt 6
   :p5 7
   :m6 8
   :M6 9
   :m7 10
   :M7 11
   :o 12})

(def nice-names->note-values
  {:whole 1
   :half 0.5
   :dotted-quarter 0.375
   :quarter 0.25
   :dotted-eighth 0.1875
   :eighth 0.125
   :sixteenth 0.0625
   :thirty-second 0.03125})

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
