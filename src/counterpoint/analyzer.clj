(ns counterpoint.analyzer
  (:require [counterpoint.util :as util]))

;;What do we want to look at?

;;variability - total note length traveled / cantus length
;;climax placement

(defn distance-traveled
  ([intervals total]
   (if (= 1 (count intervals))
     total
     (recur (rest intervals)
                  (+ (Math/abs (- (first intervals) (second intervals))) total))))
  ([intervals]
   (distance-traveled intervals 0)))

(defn climax-index
  [coll]
  (first (apply max-key second (map-indexed vector coll))))

(defn analyze-cantus [cantus]
  (let [cantus-intervals (util/melody->numeric-intervals (first cantus) cantus)
        variability (float (/ (distance-traveled cantus-intervals)
                              (count cantus)))
        climax-depth (float (/ (inc (climax-index cantus-intervals))
                               (count cantus)))]
    (println "Variability: " variability)
    (println "Climax Depth: " climax-depth)))
