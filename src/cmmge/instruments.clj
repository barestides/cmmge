(ns cmmge.instruments
  (:require ;; [overtone.inst.sampled-piano :refer :all]
            [overtone.live :refer :all]))

;; (def piano #(sampled-piano % 1 1 0 0.1 0.3 0.3 0 -4 1))

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
