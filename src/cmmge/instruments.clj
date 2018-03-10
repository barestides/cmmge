(ns cmmge.instruments
  (:require ;; [overtone.inst.sampled-piano :refer :all]
   [overtone.sc.sample :as samp]
   [overtone.live :refer :all]))

;; (def piano #(sampled-piano % 1 1 0 0.09 0.2 0.1 0.01 -5 1))

(defn sample-file-format
  [note]
  (str "Piano.mf." note ".aiff"))

(def three-octaves [:C2 :A4 :Db2 :F2 :Eb4 :B3 :Bb4 :B2 :C4 :Db4 :Eb2 :E3 :F3 :E2 :C3 :Ab3 :B4 :Bb2 :A3 :Ab4 :Eb3 :Gb2 :Db3 :G3 :Bb3 :Gb4 :E4 :G4 :Gb3 :D2 :G2 :A2 :D3 :F4 :Ab2 :D4])

(defn load-sample-notes
  [dir file-fn notes]
  (apply merge (map
                (fn [note]
                  (prn note)
                  {note (samp/sample (str dir (file-fn (name note))))})
                notes)))

(def iowa-piano-notes
  (load-sample-notes "resources/samples/iowapiano/" sample-file-format three-octaves))

(defn iowa-piano
  [note]
  ((note iowa-piano-notes)))


(definst bass-inst [freq 60 attack 0.01 sustain 0.3 release 0.05 vol 1]
  (* (env-gen (lin attack sustain release) 1 1 0 1 FREE)
     (+ (rlpf
         (+ (saw freq))
         259
         0.2)
        (bpf
         (saw freq)
         800
         0.2))

     vol))

(defn bassinst-fn
  [& args]
  (bass-inst args)
  )

(defn bass [note]
  (bass-inst (midi->hz note)))

(definst something-different
  [freq 60 attack 0.01 sustain 0.2 release 0.05 vol 1]
  (* (env-gen (lin attack sustain release) 1 1 0 1 FREE)
     (+ (rlpf
         (+ (saw freq))
         300
         0.2)
        (bpf
         (+ (saw freq))
         800
         0.2))
     vol))

(definst lead [freq 60 attack 0.01 sustain 0.3 release 0.1 vol 1]
  (* (env-gen (lin attack sustain release) 1 1 0 1 FREE)
     (bpf (saw freq) (line:kr 40 500 sustain) 1.0)
     (bpf (saw (* 2 freq)) (line:kr 800 80 sustain) 1.0)))
