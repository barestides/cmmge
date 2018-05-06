(ns cmmge.counterpoint.generation
  (:require [clojure.set :as set]
            [roul.random :as roul]
            [overtone.music.pitch :refer :all]
            [overtone.algo.chance :as chance]
            [overtone.algo.lists :as lists]
            [cmmge.constants :refer :all]
            [cmmge.chance-utils :as cu]
            [cmmge.pitch-utils :as pu]
            [cmmge.util :as util]
            [cmmge.analyzer :as analyzer]))

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
  (if (contains? perfect-intervals previous-interval)
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
                                        util/spy
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

;; (defn plot-notes
;;   ([start-pitch end-pitch duration base-rhythm]
;;    (plot-notes start-pitch end-pitch duration base-rhythm
;;                [{:pitch start-pitch :duration base-rhythm}]))
;;   ([start-pitch end-pitch duration base-rhythm melody]
;;    ;;duration is no of beats
;;    ;;let's not worry about amp for now
;;    (let [distance (Math/abs (- (note start-pitch) (note end-pitch)))
;;          interval (chance/choose ((chance/weighted-choose jump-percentages) jump-vals))]
;;      (conj )

;;      )))

(def jump-vals
  {:step [1]
   :hop [2 3]
   :leap [4 5 6]})

(def jump-percentages
  {:step 0.7
   :hop 0.2
   :leap 0.1})

(def direction-pcts
  {1 0.7
   -1 0.3})

(defn melody-length
  [melody]
  (apply + (map :duration melody)))

(defn first-melody-note [notes]
  {:pitch (first notes)
   :amp 1
   :duration 1})

(defn next-duration
  [melody]
  (let [durations (mapv :duration melody)
        last-dur (last durations)
        same-len-streak (count (take-while (partial = last-dur) (reverse durations)))
        all-dur-choices #{1 0.5}
        dur-choices (vec (cond
                           ;;if we've had 4 of the same note lengths, don't allow the next note to
                           ;;be of the same length
                           (< 3 same-len-streak)
                           (disj all-dur-choices last-dur)

                           :else
                           all-dur-choices))]
    (chance/choose dur-choices)))

(defn next-pitch
  [melody notes length]
  (let [completeness (float (/ (melody-length melody) length))
        dir-pcts (cond
                   (<= completeness 0.5)
                   {1 0.2
                    -1 0.8}

                   (> completeness 0.5)
                      {1 0.8
                       -1 0.2})
        direction (cu/weighted-choose dir-pcts)
        step-distance (chance/choose (jump-vals (cu/weighted-choose jump-percentages)))
        last-pitch (:pitch (last melody))
        chromatic-pitch (+ last-pitch (* step-distance direction))
        diatonic-pitch (first (subseq notes >= chromatic-pitch))]

    (prn "c" (note-info chromatic-pitch))
    (prn "d" (note-info diatonic-pitch))

    ;;when completeness is < 0.5, we should move further away from the tonic
    ;;when it is  > 0.5, we should move towards the tonic
    (+ (* step-distance direction) diatonic-pitch)))

;;length is beats
(defn diatonic-melody-maker
  ([key length]
   (diatonic-melody-maker key length []))
  ([key length melody]
   (let [notes (apply scale key)
         all-notes (pu/possible-notes (first notes) nil nil)
         mlength (melody-length melody)]
     (cond

       (= mlength 0) (recur key length [(first-melody-note notes)])

       (>= mlength length) melody

       :else
       (let [step-distance (chance/choose (jump-vals (cu/weighted-choose jump-percentages)))
             next-pitch (next-pitch melody all-notes length)
             ;; _ (prn next-pitch)
             next-duration (next-duration melody)
             next-note {:pitch next-pitch
                        :amp 1
                        :duration next-duration}]
         (recur key length (conj melody next-note)))))))

;;restrictions for a melody
;;agnostic to key

;;a motif is a few notes that for now, always add up to a multiple of two beats

;;what characteristics do we want to control?
;;distance traveled (pitch) to length ratio
;;number of notes to length ratio
;;range
;;syncopated

(def sample-motif
  [{:pitch :u  :dur :e}
   {:pitch :M2 :dur :e}
   {:pitch :m3 :dur :e}
   {:pitch :p5 :dur :e}
   {:pitch :m3 :dur :q}])

(defn absolutize-melody
  [melody root]
  (map (fn [note]
         (update note :pitch #(+ (% nice-names->intervals) root)))
       melody))

(defn swap-last
  [motif interval]
  (conj (into [] (butlast motif))
        (assoc (last motif) :pitch interval)))

(defn transpose
  [interval motif]
  (let [interval-int (interval nice-names->intervals)
        intervals->nice-names (set/map-invert nice-names->intervals)]
    (mapv
     (fn [note]
       (let [amt (+ interval-int (get nice-names->intervals (:pitch note)))]
         (assoc note :pitch (get intervals->nice-names amt))))
     motif)))

(defn construct
  [motif & transformations]
  (absolutize-melody
   (pu/same-vol (apply concat
                    motif
                    (map #(% motif) transformations))
             1)
   38))


;;let's do some stuff with drums
;;I think this will be easier
;;we can start with having a pattern that repeats
;;fills can be defined by length, and deviation from the pattern

(def someprog
  (construct sample-motif
             (partial transpose :m2)
             (partial transpose :tt)
             (partial transpose :p5)))

;; I'm envisioning two sets of intervals that are used to construct a multi-bar melody
;; The melody is formed by cycling through these sets.

;;the first set cycles more slowly than the second set, say once per bar.

;;the second set cycles at some factor of the first, with some randomization
;;so if the cycle frequency for the first set is 4 beats,

;;the second set cycles at n/4
(def sample-ints [:u :m3 :p5 :m7])
(def sample-prog [:u :m3 :p4 :p5])

;; (defn op-mel-obj)

;;how do we want to represent intervals? obviously, intervals are a concept we will use a lot.
;;sometimes we want to work with them as integers, for instance, increasing an interval by somea
;;amount. However, sometimes we want tow work with them as names or keywords, such as m. Like say
;;we store sets to represent nodes, and these contain keywords like`:m2`. We could have the sets
;;contain numbers instead, but that's less clear to work with from a musical perspective. At some point,
;;whether for end user or for developing from a musical perspective, we will want to convert the numeric
;;intervals to "nice" ones, and it's probably easiest to just access the data structure that plays the
;;intervals. So rather than convert numbers to nice intervals, we should just store the intervals
;;as more complex, and look at the other value in the map.

;;That might have gotten off the rails, but I think I've arrived at a solution:
;;Use a map to represent an interval. Right now I only envision it having a numeric value representing
;;it's distance from the root, and a "name" for how it referred to musically. The name should be
;;short keyword, no longer than 3 characters.


;;map a sequence of pitches to a measure,, could add more notes in between, needs to decide where to
;;put the pitches given into the measure.
()


(defn double-integer-sets
  [prog mel]
  (for [prog-note prog
        mel-note mel]
    {:pitch  (+ prog-note mel-note)}))
