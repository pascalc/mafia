(ns mafia.core
  (:use [clojure.pprint])
  (:require [clojure.set :as set]))

;; State - players and their suspicions

(def players
  (atom
    #{:pascal
      :fredrik
      :michael
      :rasmus}))

(def suspicions
  (->> (for [player @players] 
          [player (->> (remove (partial = player) @players) vec)])
    vec
    (into {})
    atom))

;; Eliminating players

(defn remove-player [suspicions player]
  (let [other-players (remove (partial = player) (keys suspicions))
        suspicions    (transient suspicions)]
    (dissoc! suspicions player)
    (doseq [other other-players]
      (assoc! suspicions other
        (vec (remove (partial = player) (suspicions other)))))
    (persistent! suspicions)))

(add-watch players :removed
  (fn [k r old-state new-state]
    (let [removed (first (set/difference old-state new-state))]
      (println "Removing" removed)
      (swap! suspicions remove-player removed)
      (pprint suspicions))))

(defn eliminate! [player]
  (swap! players set/difference (set [player])))

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
