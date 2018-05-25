(ns cmmge.generation
  (:require [cmmge.analyzer :as a]
            [cmmge.constants :refer :all]
            [cmmge.util :as util]))

;;There could be smarter versions of both of these
(def possible-durs
  {:e [:h :q :e :de :dq]
   :s [:de :s :e]
   :qs [:s :e]})

(def strength-orderings
  {:e [1 3 2 4 1.5 3.5 2.5 4.5]
   ;;not right:
   :s [1 3 2 4 1.5 3.5 2.5 4.5]})

;;divide by 4 because we're assuming 4 beats per measure
;;1 is the strongest, then 3, then 2, then 4
(defn beat-strength
  [beat granularity]
  (let [strength-ordering (granularity strength-orderings)]
    (as-> beat $
      (mod $ 4)
      (.indexOf strength-ordering $)
      (inc $)
      (float (/ $ (count strength-ordering))))))

(defn smoothness-filter
  [mel range index dur pulse note]
  (let [new-index (a/smoothness-index (conj mel {:dur dur :pitch note}) pulse)]
    (and (>= new-index (- index range)) (<= new-index (+ index range)))))

(defn notes-in-range-and-key
  [config]
  (let [{:keys [tonic mode floor ceiling]} config
        intervals (mode modes->ints)
        ;;dunno how to make this better, there's too much repetition
        above-notes (filter #(>= (+ tonic ceiling) %)
                            (for [interval intervals
                                  octave (range (Math/ceil (/ ceiling 12)))]
                              (+ tonic (+ octave interval))))
        below-notes (filter #(<= (- tonic floor) %)
                            (for [interval intervals
                                  octave (range (Math/ceil (/ floor 12)))]
                              (- tonic (+ octave interval))))]
    (set (concat above-notes below-notes))))

(defn notes-in-smoothness-range
  [config notes mel dur]
  (let [{:keys [len-beats pulse smoothness-index]} config
        mel-completeness (float (/ (a/melody-length mel pulse) len-beats))
        ;;This method of decreasing the acceptable range for the current smoothness index is suboptimal I think.
        ;;for one, we can't guarantee that will hit the desired smoothness index, this would depend on the
        ;;rate at which we decrease the range
        ;;for two, and I'm not sure on this one; but I think that this method will result in a bias towards
        ;;higher leaps at the beginning of the melody, and lower leaps/steps towards the end

        smoothness-range (case mel-completeness
                           0.0 smoothness-index
                           ;;the range should approach 0 as we near the end of the melody
                           ;;this could be better. it usually results in smoothness indexes higher than desired
                           (/ 1 mel-completeness))]
    (filter (partial smoothness-filter mel smoothness-range smoothness-index dur pulse) notes)))

(defn np
  [config mel dur]
  (let [{:keys [tonic]} config
        notes-in-range-and-key (notes-in-range-and-key config)
        avail-notes (cond
                      (empty? mel)
                      #{tonic}

                      :else
                      (notes-in-smoothness-range config notes-in-range-and-key mel dur))]
    (rand-nth (into [] avail-notes))))

(defn mel-gen
  ([config]
   (mel-gen2 config []))
  ([config mel]
   (let [{:keys [beat-granularity pulse len-beats tonic]} config
         cur-beat (a/melody-length mel pulse)]
     (cond

       (= cur-beat len-beats)
       (do
         (prn "smoothness index: " (a/smoothness-index mel pulse))
         (util/spy mel)
         mel)

       :else
       (let [remainder (- len-beats cur-beat)
             ;; _ (prn remainder)
             ;;hey this needs to change so that triplets can work
             dur (rand-nth (filter #(>= remainder (float (/ (nice-names->note-values %)
                                                            (nice-names->note-values pulse))))
                                   (beat-granularity possible-durs)))
             last-pitch (or (:pitch (last mel)) tonic)
             pitch (np config mel dur)]
         ;;dur for note can't be greater than remainder
         (recur config (conj mel {:pitch pitch :dur dur})))))))
