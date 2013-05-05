(ns mafia.core
  (:use [clojure.pprint])
  (:require [clojure.set :as set]))

;; State - players' suspicions

(def suspicions
  (atom {}))

;; Adding players

(defn- add-player-and-insert-suspicions
  [suspicions new-player]
  (let [other-players (keys suspicions)
        suspicions    (transient suspicions)]
    (assoc! suspicions new-player (vec other-players))
    (doseq [other other-players]
      (assoc! suspicions other
        (conj (suspicions other) new-player)))
    (persistent! suspicions)))

(defn add-player! [player]
  (swap! suspicions
    add-player-and-insert-suspicions player))

;; Eliminating players

(defn- remove-player [suspicions player]
  (let [other-players (remove (partial = player) (keys suspicions))
        suspicions    (transient suspicions)]
    (dissoc! suspicions player)
    (doseq [other other-players]
      (assoc! suspicions other
        (vec (remove (partial = player) (suspicions other)))))
    (persistent! suspicions)))

(defn eliminate! [player]
  (swap! suspicions remove-player player))

;; Aggregating suspicions

(defn to-ranking
  [l]
  (into {} 
    (map (fn [item pos] [item pos]) 
      l (range (count l)))))

(defn aggregate-suspicions
  [suspicion-lists]
  (let [ranks (reduce 
                #(merge-with + %1 %2)
                (map to-ranking suspicion-lists))]
    (->> (sort-by val ranks)
      vec)))

(def aggregate (atom nil))

;; Changing suspicions

(add-watch suspicions :modified
  (fn [k r old-state new-state]
    (pprint @r)
    (reset! aggregate 
      (aggregate-suspicions (vals @r)))
    (pprint @aggregate)))

(defn set-suspicions!
  [player new-suspicions]
  {:pre [(contains? @suspicions player)]}
  (swap! suspicions 
    update-in [player] (constantly new-suspicions)))

;; For debug use

(defn modify-suspicions!
  [player suspect new-rank]
  {:pre [(contains? @suspicions player)
         (contains? @suspicions suspect)]}
  (let [remove-suspect    #(remove (partial = suspect) %)
        old-suspicions    (@suspicions player)
        head              (take new-rank old-suspicions)
        tail              (drop new-rank old-suspicions)
        new-suspicions    (concat (remove-suspect head) [suspect] (remove-suspect tail))]
    (set-suspicions! player (vec new-suspicions)))
  nil)
