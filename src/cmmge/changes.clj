(ns cmmge.changes
  (require [cmmge.pitch-utils :as pu])
  )


;;We can think of music as series of changes over time.
;;These can be "fine" changes within a melody, or with a larger contect, changes to song
;;structure for example.

;;more thoughts:,

;;If a sound has too many changes, the listener cannot keep track of what is happening,
;;and even if it's organized, if the listener can't organize it, subconsiously or consiously,
;;it will just sound like noise. But if a sound doesn't have enough changes, it sound simple,
;;and isn't engaging to listen to.


;;Is popular music dull because the songs themselves are simple, or are they more formulaic,
;;so people have a better idea of what to expect?

;;Things that affect complexity:
;; instrument count
;; syncopation
;; melodic / harmonic divergence from the key
;;like with modes, if you are in the key of c major, throwing in an f# isn't as
;;much of a divergence as a d#


;;a phrase can be a group of musical "objects"
;;these objects are consitent for the most part
;;e.g. the kick is one quarter note on 1 and 3, but can have alternatives that we can swap in
;;as desired.

;;these alternatives could have values that have musical consequences, mostly related to building
;;decreasing tension

;;using the kick example, one alternative could be instead of a quarter, it's two eighth notes, the
;;second on the target beat, so AND - THREE

;;applying these alternatives is how we can define the direction of the song and add interest.
;;the more alternatives we apply, the more drastic the change is

;;alternatives could also have values describing *how much* change the alternative does
;;the sum of all alternatives applied over a given period gives us a value for the intensity of the
;;change.

;;It would also be cool to have a value describing in what way the change is made
;;does it employ syncopation, dynamics, does it build or release tension?

;;these could be related to chord qualities, something with modal interchange?

;; the musical idea (this needs a name, phrase?) is some datastructure defining one or more instruments
;; along with their melody (I think melody is still appropriate here)
;; we will probably use measures or maybe beats as our main unit here

;;an alteration (also needs a name) is a function that is applied to the idea that changes one or
;;more parts of it.
;;examples include: subdividing a note, transposing a pitch, increasing the loudness of the instrument
;;modifying the timbre of the instrument (rudimentary example: doing a semi-open hat instead of a closed
;;one)

;; A phrase should loop until being told to stop. how does it know to stop?
;;do we use a change that just mutes everything in the phrase, and continue to repeat the muted phrase
;;if it were sheet music, this is the equivalent of a mutlti-bar rest.
;;changes should return new phrases, we could just have a change called like "rest", that sets the
;;phrase to not do anything for some number of measures

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
