(ns cmmge.chord-progressions
  (:require [overtone.live :refer :all]
            [cmmge.instruments :as insts]))

(def patterns {:quarters [[1] [1] [1] [1]]
               :eigths [[1 1] [1 1] [1 1] [1 1]]
               :sixteenths [[0 1 1 1] [0 1 1 1] [0 1 1 1] [0 1 1 1]]
               :whole [[1]]
               :somethin-sexy [[1 1 1 0] [0] [0] [0 1]]
               :off-beats [[0 1] [0 1] [0 1] [0 1]]
               :one-and-three [[1] [0] [1] [0]]
               :two-and-four [[0] [1] [0] [1]]})

(def progression [:i :iv :v :vi])

(def chords [[:d4 :minor]
             [:b4 :minor]
             [:b3 :minor]
             [:g3 :major]])

(def m (metronome 120))

(def progression-spec {:length 4})

;;can't use `key` as a name smh
(defn gen-progression [thekey specs]

  )


(defn play-chords [nome instm chord-notes pattern]
  (let [note-length (float (/ 1 (count pattern)))]
    (flatten
     (map-indexed
      (fn [note amp]
        (when (= amp 1)
          (at (m (+ nome (* note note-length)))
              (flatten (map instm (apply chord chord-notes))))))
      pattern))))

(defn play-measure [nome instm chord-notes pattern]
  (let [next-bar (+ nome 4)]
    (flatten
     (map-indexed (fn [beat beat-pattern]
                    (play-chords (+ nome beat) instm (first chord-notes) beat-pattern))
                  pattern))
    (apply-by (m next-bar) #'play-measure [next-bar instm (rest chord-notes) pattern])))

;; (play-measure (m) insts/piano (cycle chords) (:somethin-sexy patterns))
