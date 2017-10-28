(ns counterpoint.generation
  (:require [clojure.set :as set]
            [roul.random :as roul]
            [counterpoint.analyzer :as analyzer]))

(defn climax-index [cantus-firmi-intervals]
  (let [cantus-climax (analyzer/climax-index cantus-firmi-intervals)
        climax-index-range (disj (set (range 3 (- (count cantus-firmi-intervals) 3)))
                                 cantus-climax)]
    ;;climax can note be the same as the cantus climax, and shouldn't be the first / last 3 notes
    (rand-nth (into [] climax-index-range))))

;;when calculating next note, there are several restrictions
;;it should not be more than a twelfth away from the cantus note
;;no similar motion into perfect intervals
;;never two consecutive perfect intervals
;;probably don't want two consecutive same note
;; Avoid combining similar motion with leaps, especially large ones.

(def consonant-intervals-within-range
  {:major [4 7 9 12 14 16]
   :minor [3 7 8 12 14 15]})

;;we can reduce filter over a bunch of conditions to wittle down the valid intervals
;;let's elminate illegal possibilities before focusing on making stepwise motion a
;;priority
;;to improve stepwise motion, I think we want to measure "distance Traveled",
;;so if we went 2 -> 12 -> 7, we traversed from a major second to a perfect fifth,
;;but it's not smooth; the distance traveled is (+ (abs (- 12 2))  (abs (- 12 7)))
;;where the leaps are is not as important as their frequency, however we should
;;avoid similar motion with leaps

(defn pick-note [cantus first-species consonant-intervals])

;;This is a recursion problem, instead of updating some atom, we should recur over the first species
;;vector that we're building until it is full. Each time the function is run it adds or changes one
;;of the notes.
;;this also lets us lazily implement "restarting if what is here won't work"; if we get to a point
;;where we have no legal notes we can change or add, and the vector is not full, we re-call the function
;;with nils, as if we're building for the first time, and we let rng do the work

(def perfect-intervals #{7 12 0})

(defn filter-perfects
  [notes previous-interval]
  ;;if previous is perfect, remove perfect intervals from possibilities
  (if (some true? (map (partial = previous-interval)
                       perfect-intervals))
    (set/difference notes perfect-intervals)
    notes))

(defn compose [cantus species possible-notes]
  ;; (prn species)
  ;; (prn cantus)
  (cond (= (count cantus) (count species))
        species

        :otherwise
        (let [clipped-cantus (take (count species) cantus)
              previous-species-note (last species)
              previous-cantus-note (last clipped-cantus)
              current-cantus-note (get cantus (count species))
              cantus-interval (- current-cantus-note previous-cantus-note)
              previous-interval (- previous-species-note previous-cantus-note)
              wittled-possibilities (-> possible-notes
                                        set
                                        (filter-perfects previous-interval)
                                        (disj previous-species-note))
              next-note (+ (rand-nth (into [] wittled-possibilities))
                           current-cantus-note)
              new-species (conj species next-note)
              last-interval (- (last species) (last clipped-cantus))]
          (recur cantus new-species possible-notes))))

(defn notes-to-climax [start-note cantus climax-index scale]
  (let [cantus-range (into [] (take (inc climax-index) cantus))
        consonant-intervals (scale consonant-intervals-within-range)]
    ;; (prn "Possible intervals:" cantus-range)
    (compose cantus-range [start-note] consonant-intervals)))

(defn construct-first-species [cantus-intervals scale]
  ;;always just do above for now
  ;;start with last, penultimate, then first,
  ;;;working with numeric intervals is easier, 0 is the root
  ;;we can also work with negative intervals

  ;;welp, dunno how the first note is gonna be ti if we're above the cantus
  ;;without having voice crossing, maybe that means if it's above, it always
  ;;needs to end on P8?
  (let [;;can start on either P1, P5, or P8
        first-note (rand-nth [0 7 12])
        conclusion 12
        climax-index (climax-index cantus-intervals)
        ;;now we need to figure out the notes from the first note to the climax position
        penultimate (cond (= (-> cantus-intervals butlast last) 2) 11)
        notes-to-climax (notes-to-climax first-note cantus-intervals climax-index scale)
        filler-nils (repeat (- (count cantus-intervals) 2 (count notes-to-climax)) nil)
        first-species (into []
                            (flatten [notes-to-climax filler-nils penultimate conclusion]))]
    (prn "Cantus:" cantus-intervals)
    (prn "First :" first-species)
    (prn "Diff  :" (map (fn [f c]
                          (if (nil? f)
                            nil
                            (- f c)))
                        first-species cantus-intervals))
    first-species))
