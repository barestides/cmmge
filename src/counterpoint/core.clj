(ns counterpoint.core
  (:gen-class)
  (:require [counterpoint.generation :as gen]
            [counterpoint.cantus-firmi :as cantus-firmi]
            [overtone.live :refer :all]
            [overtone.inst.sampled-piano :refer :all]))

(def m (metronome 120))

(def cantus-firmi-intervals {:schenker [:i :ii :iv :iii :iv :v :vi :v :iii :ii :i]})
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

(defn play []
  (play-counterpoint
   (m)
   (-> cantus-firmi/cantus-firmi
       :schenker
       first
       note)
   (mapv note (:schenker cantus-firmi/cantus-firmi))
   (gen/construct-first-species (map #(degree->interval % :major)
                                     (:schenker cantus-firmi-intervals))
                                :major)))

(defn construct [] (gen/construct-first-species (map #(degree->interval % :major)
                                                     (:schenker cantus-firmi-intervals)) :major))

(defn play-cantus [cantus]
  (play-notes (m) (degrees->pitches (cantus cantus-firmi-intervals) :major :c4) 1))
