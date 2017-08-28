(ns counterpoint.core
  (:gen-class)
  (:require [overtone.live :refer :all]
            [overtone.inst.sampled-piano :refer :all]
            [roul.random :as roul]))

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

(defn find-max-index [coll]
  (first (apply max-key second (map-indexed vector coll))))

(defn climax-index [cantus-firmi-intervals]
  (let [cantus-climax (find-max-index cantus-firmi-intervals)
        climax-index-range (disj (set (range 3 (- (count cantus-firmi-intervals) 3)))
                                 cantus-climax)]
    ;;climax can note be the same as the cantus climax, and shouldn't be the first / last 3 notes
    (rand-nth (into [] climax-index-range))))

;;when calculating next note, there are several restrictions
;;it should not be more than a twelfth away from the cantus note
;;no similar motion into perfect intervals
;;never two consecutive perfect intervals

(def consonant-intervals-within-range
  {:major [2 4 7 9 12 14 16]
   :minor [2 3 7 8 12 14 15]})

;;we can reduce filter over a bunch of conditions to wittle down the valid intervals
(defn pick-note [cantus first-species consonant-intervals])

;;This is a recursion problem, instead of updating some atom, we should recur over the first species
;;vector that we're building until it is full. Each time the function is run it adds or changes one
;;of the notes.
;;this also lets us lazily implement "restarting if what is here won't work"; if we get to a point
;;where we have no legal notes we can change or add, and the vector is not full, we re-call the function
;;with nils, as if we're building for the first time, and we let rng do the work

(defn compose [cantus species possible-notes]
  (prn species)
  (if (= (count cantus) (count species))
    species
    (compose cantus (conj species (rand-nth possible-notes)) possible-notes)))

(defn notes-to-climax [start-note cantus climax-index scale]
  (let [cantus-range (rest (take (inc climax-index) cantus))
        consonant-intervals (scale consonant-intervals-within-range)]
    (prn "Possible intervals:" consonant-intervals)
    (compose cantus-range [] consonant-intervals)))

(defn construct-first-species [cantus-intervals scale]
  ;;always just do above for now
  ;;start with last, penultimate, then first,
  ;;;working with numeric intervals is easier, 0 is the root
  ;;we can also work with negative intervals
  (let [intervals (mapv #(degree->interval % scale) cantus-intervals)
        ;;can start on either P1, P5, or P8
        first-note (rand-nth [0 7 12])
        conclusion 0
        climax-index (climax-index intervals)
        ;;now we need to figure out the notes from the first note to the climax position
        penultimate (cond (= (-> cantus-intervals butlast last) :ii) -1)
        notes-to-climax (notes-to-climax first-note intervals climax-index scale)
        filler-nils (repeat (- (count intervals) 3 (count notes-to-climax)) nil)
        first-species (into []
                            (flatten [first-note notes-to-climax filler-nils penultimate conclusion]))]
    (prn "Cantus:" intervals)
    (prn "First :" first-species)
    first-species))

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

(play-counterpoint
 (m)
 (-> cantus-firmi
     :ss4
     first
     note)
 (mapv note (:ss4 cantus-firmi))
 (construct-first-species (:ss4 cantus-firmi-intervals) :major))

;; (play-notes (m) (degrees->pitches (:ss4 cantus-firmi-intervals) :major :c3) 2)
