(ns cmmge.changes
  (require [cmmge.pitch-utils :as pu]))

(defn apply-new-pitch-vect
  [phrase pitch-vect]
  (update phrase :melody (fn [melody] (map #(assoc %1 :pitch %2) melody pitch-vect))))

(defn modulation-change
  [phrase amount]
  (let [{:keys [melody key]} phrase
        ;; new-key (pu/modulate-key key amount)
        modulated-pitches (pu/convert-melody-to-new-key (mapv :pitch melody) key amount)]
    (apply-new-pitch-vect phrase modulated-pitches)))

(defn apply-modulations
  [phrase & amounts]
  (into [phrase] (map (partial modulation-change phrase) amounts)))
