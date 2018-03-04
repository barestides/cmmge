(ns cmmge.triadic-transformations
  (:require [clojure.core.match :refer [match]]
            [overtone.algo.chance :as chance]
            [overtone.music.pitch :as pitch]
            [cmmge.pitch-utils :as mypitch]))

;; https://en.wikipedia.org/wiki/Neo-Riemannian_theory#Triadic_transformations_and_voice_leading

(defn minor?
  [root third]
  (= 3 (- third root)))

(defn p-transform
  "Transform a chord to its parallel.
  CM -> Cm"
  [[root third fifth]]
  (if (minor? root third)
    [root (inc third) fifth]
    [root (dec third) fifth]))

;;ughhh we want to "flatten" a chord to its first inversion, b/c these transformtions don't take
;;inversions into account.
(defn first-inversion
  [triad]
  (let [sorted (sort triad)]
    (if (= (- (last sorted) (first sorted)) 7)
      sorted
      (recur (mypitch/invert-chord sorted (chance/choose [1 -1]))))))

(defn r-transform
  "Transform a chord to its Relative
  CM -> Am"
  [[root third fifth]]
  (if (minor? root third)
    [(- root 2) third fifth]
    [root third (+ fifth 2)]))

(defn l-transform
  "Transform a chord to its Leading-Tone Exchange
  CM -> Em"
  [[root third fifth]]
  (if (minor? root third)
    [root third (inc fifth)]
    [(dec root) third fifth]))

(defn tiered-transform
  [transformations]
  (apply comp (interleave transformations (repeat first-inversion))))

(defn n-transform
  "Transform a chord to its minor subdominant.
  CM -> Fm "
  [chord]
  ((tiered-transform [r-transform l-transform p-transform]) chord))

(defn s-transform
  "Exchanges two triads that share a third
   cM  -> c#m"
  [chord]
  (-> chord
      l-transform
      p-transform
      r-transform))

(defn h-transform
  "Transform a chord to its hexatonic pole
  CM -> Abm"
  [chord]
  (-> chord
      l-transform
      p-transform
      l-transform))

(defn transformation-dispatcher
  [transformation chord]
  (transformation (sort chord)))

(defn transform
  [chord]
  (let [transformation (chance/choose [l-transform p-transform r-transform])]
    (prn transformation "  " chord (map pitch/find-note-name chord) (pitch/find-chord chord))
    (first-inversion (transformation chord))))

(defn progression-notes
  ([start-chord length]
   (progression-notes start-chord length []))
  ([start-chord length so-far]
   (cond

     (empty? so-far)
     (recur start-chord length [(sort start-chord)])

     (= (count so-far) length)
     so-far

     :otherwise
     (let [next-chord (transform (last so-far))]
       (recur start-chord length (conj so-far next-chord))))))
