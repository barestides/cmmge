(ns cmmge.chance-utils
  (:require [overtone.algo.chance :as chance]))

(defn weighted-choose
  "Returns an element from list vals based on the corresponding
  probabilities list. The length of vals and probabilities should be
  similar and the sum of all the probabilities should be 1. It is also
  possible to pass a map of val -> prob pairs as a param.

  The following will return one of the following vals with the
  corresponding probabilities:
  1 -> 50%
  2 -> 30%
  3 -> 12.5%
  4 -> 7.5%
  (weighted-choose [1 2 3 4] [0.5 0.3 0.125 0.075])
  (weighted-choose {1 0.5, 2 0.3, 3 0.125, 4 0.075})"
  ([val-prob-map] (weighted-choose (keys val-prob-map) (vals val-prob-map)))
  ([vals probabilities]
   (when-not (= (count vals) (count probabilities))
     (throw (IllegalArgumentException. (str "Size of vals and probabilities don't match. Got "
                                            (count vals)
                                            " and "
                                            (count probabilities)))))
   ;;because adding floats isn't alwasy 1.0
   (when-not (= (float (reduce + probabilities)) 1.0)
     (throw (IllegalArgumentException. (str "The sum of your probabilities is not 1.0"))))

   (let [paired (map vector probabilities vals)
         sorted (sort #(< (first %1) (first %2)) paired)
         summed (loop [todo sorted
                       done []
                       cumulative 0]
                  (if (empty? todo)
                    done
                    (let [f-prob (ffirst todo)
                          f-val  (second (first todo))
                          cumulative (+ cumulative f-prob)]
                      (recur (rest todo)
                             (conj done [cumulative f-val])
                             cumulative))))
         rand-num (rand)]
     (loop [summed summed]
       (when (empty? summed)
         (throw (Exception. (str "Error, Reached end of weighed choice options"))))
       (if (< rand-num (ffirst summed))
         (second (first summed))
         (recur (rest summed)))))))
