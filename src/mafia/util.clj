(ns mafia.util
  (:require [clojure.set :as set]))

(defn abs-difference
  [s1 s2]
  (cond 
    (> (count s1) (count s2)) (set/difference s1 s2)
    (< (count s1) (count s2)) (set/difference s2 s1)
    :else                     #{}))
