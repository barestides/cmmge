(ns cmmge.melodies
  (:require [cmmge.pitch-utils :as pu]
            [cmmge.instruments :as inst]))

(def bassline-1
  (let [first-bar [{:pitch :f2 :duration 1} {:pitch :g2 :duration 1.05}
                   {:pitch :ab2 :duration 0.45} {:pitch :g2 :duration 1.5}]
        second-bar [{:pitch :f2 :duration 1} {:pitch :bb2 :duration 0.5}
                    {:pitch :ab2 :duration 1} {:pitch :g2 :duration 1.5}]]
    (mapv #(assoc % :amp 1) (concat first-bar second-bar))))

(def bassline-phrase
  {:melody bassline-1
   :inst inst/bass-inst
   :type :bassline
   :key :f
   :mode :minor})
