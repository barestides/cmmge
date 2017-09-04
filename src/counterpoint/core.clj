(ns counterpoint.core
  (:gen-class)
  (:require [counterpoint.generation :as gen]
            [overtone.live :refer :all]
            [overtone.inst.sampled-piano :refer :all]))

(def m (metronome 120))

(def cantus-firmi {:ss1 [:c4 :d4 :e4 :f4 :g4 :d4 :f4 :e4 :d4 :c4]
                   :ss2 [:d4 :g4 :f#4 :b4 :a4 :f#4 :g4 :f#4 :e4 :d4]
                   :ss3 [:a3 :b3 :c#4 :f#4 :e4 :a3 :b3 :d4 :c#4 :b3 :a3]
                   :ss4 [:c4 :d4 :f4 :e4 :f4 :g4 :a4 :g4 :e4 :d4 :c4]
                   :fux [:f4 :g4 :a4 :f4 :d4 :e4 :f4 :c4 :a4 :f4 :g4 :f4]
                   :chromatic [:c4 :c#4 :d4 :eb4 :e4 :f4 :f#4 :g4 :g#4 :a4 :bb4 :b4 :c5]
                   :cmajor [:c4 :d4 :e4 :f4 :g4 :a4 :b4 :c5]
                   :cminor [:c4 :d4 :eb4 :f4 :g4 :ab4 :bb4 :c5]
                   :gminor [:g3 :a3 :bb3 :c4 :d4 :eb4 :f4 :g4]})

(def cantus-firmi-intervals {:ss4 [:i :ii :iv :iii :iv :v :vi :v :iii :ii :i]})
(def my-piano #(sampled-piano % 1 1 0 0 0.5 0.5 0 -4 1))

(defn play-counterpoint [nome root cantus-firmus first-species]
  (let [next (+ nome 1)
        cantus-note (first cantus-firmus)
        first-species-note (first first-species)]
    (at nome
        (my-piano cantus-note)
        (when first-species-note
          (my-piano (+ root first-species-note))))
    (when (not-empty (rest cantus-firmus))
      (apply-by (m next)  #'play-counterpoint [next
                                        root
                                        (rest cantus-firmus)
                                        (rest first-species)]))))


(defn play-notes [nome notes l]
  (let [next (+ nome l)]
    (at nome (my-piano (note (first notes))))
    (when (not-empty (rest notes))
      (apply-by (m next)  #'play-notes [next (rest notes) l]))))

;; (play-counterpoint
;;  (m)
;;  (-> cantus-firmi
;;      :ss4
;;      first
;;      note)
;;  (mapv note (:ss4 cantus-firmi))
;;  (construct-first-species (:ss4 cantus-firmi-intervals) :major))

(gen/construct-first-species (map #(degree->interval % :major)
                                  (:ss4 cantus-firmi-intervals)) :major)

;; (play-notes (m) (degrees->pitches (:ss4 cantus-firmi-intervals) :major :c3) 2)
