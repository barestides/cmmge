(ns cmmge.analyzer
  (:require [cmmge.util :as util]
            [cmmge.constants :refer :all]
            [cmmge.counterpoint.cantus-firmi :as cantus-firmi]))

;;What do we want to look at?

;;variability - total note length traveled / cantus length
;;climax placement


(defn distance-traveled
  ([mel]
   (distance-traveled (mapv :pitch mel) 0))
  ([[first-note second-note :as mel] total]
   (if second-note
     (recur (rest mel) (+ total (Math/abs (- first-note second-note))))
     total)))

(defn melody-length
  [mel pulse]
  (/ (apply + (map #(nice-names->note-values (:dur %)) mel)) (nice-names->note-values pulse)))

(defn smoothness-index
  [mel pulse]
  (float (/ (distance-traveled mel) (melody-length mel pulse))))

(defn climax-index
  [coll]
  (first (apply max-key second (map-indexed vector coll))))

(defn analyze-cantus [cantus]
  (let [cantus-intervals (util/melody->numeric-intervals (first cantus) cantus)
        variability (float (/ (distance-traveled cantus-intervals)
                              (count cantus)))
        climax-depth (float (/ (inc (climax-index cantus-intervals))
                               (count cantus)))]
    {:variability variability
     :climax-depth climax-depth}))

(def cantus-averages
  (let [cantus-analytics (map analyze-cantus (vals cantus-firmi/cantus-firmi))]
    (apply merge (map (fn [key]
                        {key (util/mean (map key cantus-analytics))})
                      (keys (first cantus-analytics))))))
