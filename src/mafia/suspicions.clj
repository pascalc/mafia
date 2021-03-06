(ns mafia.suspicions
  (:require [mafia.util :as util]))

;; Aggregating suspicions

(defn- distribute-suspicion-lists
  [suspicions]
  (->> (drop 1 suspicions)
    (reduce util/assoc-shifted-replaced [(into {} [(first suspicions)])])
    (apply merge)))

(defn distribute-suspicions!
  [game]
  (swap! (:suspicions game) distribute-suspicion-lists))

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

(defn aggregate [game]
  (aggregate-suspicions (vals @(:suspicions game))))

;; Changing suspicions

(defn set-suspicions!
  [game player new-suspicions]
  {:pre [(contains? @(:suspicions game) player)]}
  (swap! (:suspicions game)
    update-in [player] (constantly new-suspicions)))

;; For debug use

(defn modify-suspicions!
  [game player suspect new-rank]
  {:pre [(contains? @(:suspicions game) player)
         (contains? @(:suspicions game) suspect)]}
  (let [remove-suspect    #(remove (partial = suspect) %)
        old-suspicions    (@(:suspicions game) player)
        head              (take new-rank old-suspicions)
        tail              (drop new-rank old-suspicions)
        new-suspicions    (concat (remove-suspect head) [suspect] (remove-suspect tail))]
    (set-suspicions! game player (vec new-suspicions)))
  nil)
