(ns mafia.util
  (:require [clojure.set :as set]))

(defn abs-difference
  [s1 s2]
  (cond 
    (> (count s1) (count s2)) (set/difference s1 s2)
    (< (count s1) (count s2)) (set/difference s2 s1)
    :else                     #{}))

(defn shift-left
  [l]
  (vec (concat (rest l) [(first l)])))

(defn replace-in-list
  [l target replacement]
  (mapv
    #(if (= % target) replacement %)
    l))

(defn assoc-shifted-replaced
  [acc [next-k _]]
  (let [[[last-k last-v]] (vec (last acc))]
    (conj acc
      {next-k (-> last-v shift-left (replace-in-list next-k last-k))})))
