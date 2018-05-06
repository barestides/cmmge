(ns cmmge.player
  (:require
   [overtone.music.pitch :as p]
   [overtone.music.time :as t]
   [overtone.music.rhythm :as r]
   [overtone.at-at :as at-at]
   [cmmge.constants :refer :all]))

(def player-state
  {:metronome (r/metronome 120)
   :pulse-note :q
   :playing (atom true)})

(def pool (at-at/mk-pool))

(defn play-drum-pattern
  [nome beat pattern inst-fn pulse]
  (let [{:keys [dur rest?]} (first pattern)
        real-dur (float (/ (dur nice-names->note-values)
                           (pulse nice-names->note-values)))
        next-note (+ beat real-dur)]
    (when-not rest? (at-at/at (nome beat) inst-fn pool))
    (when (not-empty (rest pattern))
      (t/apply-by (nome next-note) #'play-drum-pattern [nome next-note (rest pattern) inst-fn pulse]))))

;;melodic tracks are different because the inst fn needs the specific note and the duration.
;;it's not just an impulse like percussive

;;we have to decide how to to treat pitch. Obviously at the point where we're sending it to the midi
;;receiver, it needs to be a midi number

;;ok the real-dur is messed up. it needs to be in millis. Also, it needs to use the bpm of the metronome
;;somehow

;;an eighth note played at 120bpm with the pulse on the quarter note should play for 0.25s, or 250millis
;;because there is one quarter note per beat, so every 0.5s, and an eighth note is 1/2 of a quarter note

;;jinkies probs move this to a utils ns
;;also make it not fuck ugly
;;also it maybe screws up for eighth note triplets...
(defn real-dur
  [dur-note pulse nome]
  (let [dur-to-pulse (float (/ (dur-note nice-names->note-values)
                               (pulse nice-names->note-values)))]

    (* (float (/ dur-to-pulse (float (/ (r/metro-bpm nome)
                                        60))))
       1000)))

(defn play-melodic-pattern
  [nome beat pattern inst-fn pulse]
  (let [{:keys [dur rest? pitch]} (first pattern)

        ;;however, we need to consider that inst-fns might not always expect a midi note, maybe they
        ;;expect a freq. I think this is ok for now though
        midi-note (p/note pitch)
        ;;this is so ugly but idgaf
        dur-to-pulse (float (/ (dur nice-names->note-values)
                               (pulse nice-names->note-values)))
        real-dur (real-dur dur pulse nome)
        next-note (+ beat dur-to-pulse)]
    (prn real-dur)
    (when-not rest? (at-at/at (nome beat) #(inst-fn midi-note real-dur) pool))
    (when (not-empty (rest pattern))
      (t/apply-by (nome next-note) #'play-melodic-pattern
                  [nome next-note (rest pattern) inst-fn pulse]))))

;;I bet we can do some cool macro shit to allow us to have special chars denoting a rest, rather than
;;calling a function that just returns a normal note map
;;alternatively, pitch could just be nil, and we could check on that
;;alternatively, could have velocity as part of the note map, and that could be 0
;;not a big fan of the latter though

(defn play-drum-track
  [state track]
  (let [{:keys [pulse-note metronome]} state
        {:keys [inst-fn pattern]} track]
    (play-one-pattern metronome (metronome) pattern inst-fn pulse-note)))

(defn play-track
  [state track]
  (let [{:keys [pulse-note metronome]} state
        {:keys [inst-fn pattern inst-type]} track
        player (case inst-type
                 :percussive play-drum-pattern
                 :melodic play-melodic-pattern)]
    (player metronome (metronome) pattern inst-fn pulse-note)))

;;`tracklist` - vector of maps; each map is a `track`
;;each `track` contains:
;;`pattern` - that indicates what notes should be played when
;;`inst-type` - which can be percussive or melodic
;;`inst-fn`, which is what actually plays the sound. this function should at least take amplitude
;;`name` friendly name optional for now
;;           in the case of melodic instruments, it should take pitch as well

;;I don't know if having the entire track in the pattern makes sense, you could indicate when a track
;;starts and stops? I don't think there's any problem with having multi bar rests though.

;;A lot of this comes down to what to express as data, and what to express
;;as transformations (functions), which I think is a fundamental concept in programming.

;;if it's data, I think it's easier to generate, makes realtime modulation difficult
;;no matter what, at some point it needs to get to being data, so we'll start with that.


(defn play-tracks
  [tracklist]
  (r/metro-start (:metronome player-state) 0)
  (doseq [track tracklist]
    (play-track player-state track)))
