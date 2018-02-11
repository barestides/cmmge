(ns cmmge.triadic-transformations
  (:require [overtone.algo.chance :as chance]
            [overtone.music.pitch :as pitch]))

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

(defn r-transform
  "Transform a chord to its Relative
  CM -> Am"
  [[root third fifth]]
  (if (minor? root third)
    [root third (dec fifth)]
    [root third (inc fifth)]))

(defn l-transform
  "Transform a chord to its Leading-Tone Exchange
  CM -> Em"
  [[root third fifth]]
  (if (minor? root third)
    [(inc root) third fifth]
    [(dec root) third fifth]))

(defn n-transform
  "Transform a chord to its minor subdominant.
  CM -> Fm "
  [chord]
  (-> chord
      r-transform
      l-transform
      p-transform))

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
  (let [transformation (chance/choose [p-transform r-transform l-transform])]
    (prn transformation)
    (transformation-dispatcher transformation chord)))

(defn progression-notes
  ([start-chord length]
   (progression-notes start-chord length []))
  ([start-chord length so-far]
   (cond

     (empty? so-far)
     (recur start-chord length [start-chord])

     (= (count so-far) length)
     so-far

     :otherwise
     (let [next-chord (transform (last so-far))]
       (recur start-chord length (conj so-far next-chord))))))
