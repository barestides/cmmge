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
               0.5))

;;when writing melodies out, we shouldn't need to write `dur` and `pitch` repeatedly,
(defn flesh-out-melody
  [melody]
  (for [[dur pitch] melody]
    (if pitch
      {:dur dur :pitch pitch}
      {:dur dur :rest? true})))


;;jinkies handle anacruses
;;https://en.wikipedia.org/wiki/Anacrusis
;;for now, we'll just include extra rests in the lead-inbar
;;jinkies slurs, esp across barlines
(def a-hard-days-night-vocals
  (flesh-out-melody
   [[:h]
    [:e]
    [:e :g4]
    [:e :g4]
    [:e :f4]

    [:h :g4]
    [:dq :g4]
    [:e :g4]

    [:h :g4]
    [:e]
    [:e :g4]
    [:e :g4]
    [:e :f4]

    [:e :g4]
    [:e :bb4]
    [:dq :bb4]
    [:q :g4]
    [:e :f4]

    [:s :g4]
    [:s :f4]
    [:e :e4]
    [:q :e4]
    [:e]
    [:e :e4]
    [:e :f4]
    [:e :e4]

    [:h :g4]
    [:dq :g4]
    [:e :g4]

    [:h :g4]
    [:e]
    [:e :g4]
    [:e :g4]
    [:e :f4]

    [:e :g4]
    [:e :bb4]
    [:dq :bb4]
    [:q :g4]
    [:e :f4]

    [:s :g4]
    [:s :f4]
    [:e :e4]
    [:q :e4]
    ])
  )
