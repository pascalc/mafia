(ns mafia.suspicions
  (:use [clojure.pprint]))

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

(defn aggregate [game]
  (aggregate-suspicions (vals @(:suspicions game))))

(defn broadcast-aggregate [game] 
  (fn [k r old-state new-state]
    (pprint @r)
    (pprint 
      {(str "Aggregate #" (:id game)) 
       (aggregate-suspicions (vals @r))})))

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
