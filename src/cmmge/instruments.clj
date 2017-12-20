(ns cmmge.instruments
  (:require [overtone.inst.sampled-piano :refer :all]
            [overtone.live :refer :all]))

(def piano #(sampled-piano % 1 1 0 0 0.5 0.5 0 -4 1))

(definst bass-inst [freq 60 attack 0.01 sustain 0.4 release 0.1 vol 1]
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

(defn bass [note]
  (bass-inst (midi->hz note)))
