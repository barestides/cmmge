(ns cmmge.instruments
  (:require [overtone.inst.sampled-piano :refer :all]))

(def piano #(sampled-piano % 1 1 0 0 0.5 0.5 0 -4 1))
