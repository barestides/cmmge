(ns cmmge.instruments
  (:require ;; [overtone.inst.sampled-piano :refer :all]
   [overtone.sc.sample :as samp]
   [overtone.live :refer :all]))

;;I don't think we'll do much with this, because having midi out to synthesizers / samplers allows for better
;;control over sound, but we'll keep it around anyway

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
  (bass-inst args))

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
