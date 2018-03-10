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

;;8 bit music theory talks about ambient chord progressions

;;this provoked a though regarding voice leading between chords. Depending on what notes in what octaves
;;you choose, a progression can sound smooth or "edgy",  Basically, if we have less total movement to
;;get to a chord, it sounds smoother, what does it sound like if have more? What if we variate it?

;; https://www.youtube.com/watch?v=MycI9ZOSPRk&t=1s
(defn voice-lead
  [from-chord to-chord smoothness]


  )
