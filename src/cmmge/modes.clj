(ns cmmge.modes
  (:require [overtone.live :refer :all]
            [overtone.inst.sampled-piano :refer :all]
            [overtone.inst.synth :refer :all]
            [overtone.inst.drum :refer :all]
            [cmmge.instruments :as insts]))

(def m (metronome 120))
(def my-piano
  (fn [pitch]
    (when pitch (sampled-piano pitch 1 0.8 0 0 0.2 0.2 1 -10 1))))
;;A melody is a vector of notes. A note is a pitch and a duration. We can use nil to indicate rests.
;;duration is based off of quarter note 1 = 1 quarter note, 0.5 = one eigth note.

(def pi 3.14159)


(defn transpose-melody
  [melody amount]
  (mapv (fn [[pitch length]]
          (vector (when pitch (+ (note pitch) amount)) length))
        melody))

(defn speed-melody
  [melody rate]
  (mapv (fn [[pitch length]]
          (vector pitch (float (/ length rate))))
        melody))

(defn subdivide [melody divisions]
  (mapv (fn [[pitch length]]
          [[pitch (float (/ length divisions))]])))

(def progression [[:a3 :minor] [:d3 :minor] [:g3 :major] [:c3 :major]
                  [:f3 :major] [:d3 :minor] [:e3 :minor] [:a2 :minor]])

(defn walking-bassline
  ([progression]
   (walking-bassline progression []))
  ([progression bassline]
   (let [[first-chord second-chord] progression
         notes (mapv #(- % 12) (apply scale first-chord))
         first-note (first notes)
         second-note (rand-nth notes)
         third-note (rand-nth [(first notes) (get notes 2) (get notes 4)])
         fourth-note (if second-chord
                       (let [next-chord-root (- (first (apply scale second-chord)) 24)]
                         (rand-nth [(+ next-chord-root 7)
                                    (- next-chord-root 1)
                                    (- next-chord-root 2)
                                    (+ next-chord-root 2)
                                    (+ next-chord-root 1)]))
                       first-note)
         measure-bassline [[first-note 1] [second-note 1] [third-note 1] [fourth-note 1]]
         extended-bassline (concat bassline measure-bassline)]
     (if (empty? (rest progression))
       extended-bassline
       (walking-bassline (rest progression) extended-bassline)))))

(def base-melody [[:c3 1] [:eb3 1] [:g3 1] [:bb3 0.5] [:g3 1.5] [nil 3]])

(def melody-1 (concat base-melody (speed-melody base-melody 2)
                      (transpose-melody base-melody 4)
                      (speed-melody (transpose-melody base-melody 4) 2)))

(defn melody-player [nome melody instm]
  (let [[pitch duration] (first melody)
        next (+ nome duration)]
    (at (m nome) (instm (note pitch)))
    (when (not-empty (rest melody))
      (apply-by (m next) #'melody-player [next (rest melody) instm]))))

(defn play-chords [nome instm chord-notes pattern]
  (let [note-length (float (/ 1 (count pattern)))]
    (flatten
     (map-indexed
      (fn [note amp]
        (when (= amp 1)
          (at (m (+ nome (* note note-length)))
              (flatten (map instm (apply chord chord-notes))))))
      pattern))))

;; (melody-player (m) melody-1)
(defn play-measure [nome instm chord-notes pattern]
  (let [next-bar (+ nome 4)]
    (flatten
     (map-indexed (fn [beat beat-pattern]
                    (play-chords (+ nome beat) instm (first chord-notes) beat-pattern))
                  pattern))
    (apply-by (m next-bar) #'play-measure [next-bar instm (rest chord-notes) pattern])))

(def bassline [[:a3 1] [:d3 1] [:g3 1] [:c3 1] [:f3 1] [:b2 1] [:c#3 1]])

;; (melody-player (m) bassline insts/bass)

;; (play-measure (m) my-piano (cycle progression) [[1]])
;; (melody-player (m) (walking-bassline progression) bass)
