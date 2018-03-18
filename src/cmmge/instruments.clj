(ns cmmge.instruments
  (:require ;; [overtone.inst.sampled-piano :refer :all]
   [overtone.sc.sample :as samp]
   [overtone.live :refer :all]))

;; (def piano #(sampled-piano % 1 1 0 0.09 0.2 0.1 0.01 -5 1))

;; (defn sample-file-format
;;   [note]
;;   (str "Piano.mf." note ".aiff"))

;; (def three-octaves [:C2 :A4 :Db2 :F2 :Eb4 :B3 :Bb4 :B2 :C4 :Db4 :Eb2 :E3 :F3 :E2 :C3 :Ab3 :B4 :Bb2 :A3 :Ab4 :Eb3 :Gb2 :Db3 :G3 :Bb3 :Gb4 :E4 :G4 :Gb3 :D2 :G2 :A2 :D3 :F4 :Ab2 :D4])

;; (defn load-sample-notes
;;   [dir file-fn notes]
;;   (apply merge (map
;;                 (fn [note]
;;                   (prn note)
;;                   {note (samp/sample (str dir (file-fn (name note))))})
;;                 notes)))

;; (def iowa-piano-notes
;;   (load-sample-notes "ssresources/samples/iowapiano/" sample-file-format three-octaves))

;; (defn iowa-piano
;;   [note]
;;   ((note iowa-piano-notes)))


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

(definst bass2
  [freq 60 attack 0.03 sustain 0.23 release 0.07 vol 1]
  (*
   (env-gen (lin attack sustain release) 1 1 0 1 FREE)
   (+ (lpf (+ (saw freq)
              (saw (+ freq 0.2))
              (saw (- freq 0.2)))
           300)
      (sin-osc (/ freq 2)))
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

(def partials [0.5 1 1.19 1.56 2 2.51 2.66 3.01 4.1])

(def partials-vols  [0.25 1 0.8 0.5 0.9 0.4 0.3 0.6 0.1])


(definst bell
  [freq 120 attack 0.02 sustain 0.5 release 0.2]
  (*
   (env-gen (lin attack sustain release) 1 0.5 0 1 FREE)
   (* 0.3 (apply + (map #(* (sin-osc (* % freq)) %2) partials partials-vols)))))

(def tet-ratios
  {:u 1
   :m2 1.059463
   :M2 1.122462
   :m3 1.189207
   :M3 1.259921
   :p4 1.334840
   :tt 1.414214
   :p5 1.498307
   :m6 1.587401
   :M6 1.681793
   :m7 1.781797
   :M7 1.887749
   :o  2})
