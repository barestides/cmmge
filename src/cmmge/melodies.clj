(ns cmmge.melodies
  (:require [cmmge.pitch-utils :as pu]
            [cmmge.constants :refer :all]
            [cmmge.instruments :as inst]))

(def bassline-1
  (let [first-bar [{:pitch :f2 :dur :q} {:pitch :g2 :dur :q}
                   {:pitch :ab2 :dur :e} {:pitch :g2 :dur :dq}]
        second-bar [{:pitch :f2 :dur :q} {:pitch :bb2 :dur :e}
                    {:pitch :ab2 :dur :q} {:pitch :g2 :dur :dq}]]
    (mapv #(assoc % :amp 1) (concat first-bar second-bar))))

(def bassline-phrase
  {:melody bassline-1
   :inst inst/bass-inst
   :type :bassline
   :key :f
   :mode :minor})

;;best one - anderson .paak, guitar

(defn rest-note
  [len]
  {:pitch :c3 :dur len :amp 0})

(def kaytranada-bd-lesson
  (pu/same-vol [{:pitch :c2 :dur :e}
                (rest-note :s)
                {:pitch :c2 :dur :s}
                {:pitch :c2 :dur :e}
                (rest-note :e)
                {:pitch :c3 :dur :s}
                {:pitch :c2 :dur :s}
                (rest-note :e)
                {:pitch :c2 :dur :s}

                {:pitch :c3 :dur :s}
                {:pitch :c3 :dur :et}
                {:pitch :c3 :dur :et}] 0.5))

(defn loop-mel
  [mel times]
  (apply concat (take times (repeat mel))))

(def thriller-bassline
  (pu/same-vol [{:pitch :eb3
                 :dur :e}
                {:pitch :f3
                 :dur :e}
                {:pitch :ab3
                 :dur :e}
                {:pitch :bb3
                 :dur :e}
                {:pitch :f3
                 :dur :e}
                (rest-note :e)
                (rest-note :de)
                {:pitch :f3
                 :dur :s}]
               0.5)
  )
