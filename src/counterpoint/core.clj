(ns counterpoint.core
  (:gen-class)
  (:require [counterpoint.generation :as gen]
            [overtone.live :refer :all]
            [overtone.inst.sampled-piano :refer :all]))

(def m (metronome 120))

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
