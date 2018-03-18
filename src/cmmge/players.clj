(ns cmmge.players
  (:require [overtone.live :refer :all]
            [cmmge.constants :refer :all]))

(defn melody-player [nome beat melody instm pulse]
  (let [{:keys [pitch amp dur]} (first melody)
        real-dur (float (/ (dur nice-names->note-values)
                           (pulse nice-names->note-values)))
        sustain-secs (float (/ (beat-ms real-dur (metro-bpm nome)) 1000))
        next (+ beat real-dur)]
    (at (nome beat) (instm :freq (midi->hz (note pitch)) :vol amp :sustain sustain-secs))
    (when (not-empty (rest melody))
      (apply-by (nome next) #'melody-player [nome next (rest melody) instm pulse]))))

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
