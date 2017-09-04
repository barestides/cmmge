(ns counterpoint.util
  (:require [overtone.music.pitch :refer :all]))

(defn melody->numeric-intervals
  "Converts a melody given as keyword notes to intervals based off the given root."
  [root pitches]
  (map (fn [pitch] (-> pitch
                       note
                       (- (note root)))) pitches))
