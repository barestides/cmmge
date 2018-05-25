(ns cmmge.keep-it-goin
  (:require [overtone.music.rhythm :as r]
            [overtone.algo.chance :as chance]
            [overtone.music.pitch :refer :all]
            [overtone.sc.server :refer :all]
            [cmmge.constants :refer :all]
            [cmmge.players :as players]
            [cmmge.util :as util]
            [cmmge.instruments :as insts]))

(defn div [n d]
  (float (/ n d)))

;; (def ugens (atom {:swinger (fn [beat-no duration]
;;                              (prn (float (m)))
;;                              (let [fuzz (chance/ranged-rand -0.01 0.01)
;;                                      dest (div duration 2)
;;                                      amount #(/ dest (* 0.4 (m)))
;;                                    ;; (float (/ 0.5 (Math/pow beat-no 2)))
;;                                      ]
;;                                (prn (amount)) (amount)))}))

(def m (r/metronome 90))

;; a note is gonna just be a map like this:
{:pitch :a2
 :amp 1
 :duration 0.25} ;;out of 1 beat

(defn half-note [n] (update n :duration #(div % 2)))

;; (defn rest
;;   [beats]
;;   [{:pitch :a2 :amp 0 :duration beats}])

(def base-bassline (into []
                     (concat
                      (repeat 6 {:pitch :a4 :amp 0.5 :duration 0.5})
                      (repeat 3 {:pitch :a4 :amp 0.5 :duration 0.3333})
                      ;; [{:pitch :a2 :amp 0 :duration 0.25}]
                      )))

(def groove1 [{:pitch :g4 :amp 0.5 :duration 1}
              {:pitch :bb4 :amp 0.5 :duration 1}
              {:pitch :db5 :amp 0.5 :duration 1}
              {:pitch :c5 :amp 0.5 :duration 1}])

;; (def springy-melody
;;   (rest ))
(defn stutter-note
  [note]
  (repeat 2 (half-note note)))

(defn random-stutter
  [melody]
  (flatten (map (fn [n]
                  (if (util/percent-chance 0.8)
                    (stutter-note n)
                    n))
                melody)))

(defn pitch-shift1
  [melody semitones]
  (map #(update % :pitch (fn [pitch]
                           (+ (note pitch) semitones))) melody))



(def bassline (concat
               base-bassline
               (pitch-shift1 base-bassline -2)
               (pitch-shift1 base-bassline 3)
               (pitch-shift1 base-bassline -4)))

(def stutter-bassline (concat base-bassline
                              (random-stutter base-bassline)))


(players/melody-player m (m) (cycle ((random-stutter groove1))) insts/bass-inst)
;; (players/melody-player m (m) (pitch-shift1 (cycle bassline) 12) insts/lead)
