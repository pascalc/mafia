(ns mafia.core
  (:use [clojure.pprint])
  (:require [clojure.set :as set]
            [mafia.util :as util]))

;; Single games

(declare 
  broadcast-aggregate
  update-players
  watch-mafia)

(def game-counter (atom 0))

(defrecord Game 
  [id
   players
   mafia
   suspicions
   last-updated])

(defn- create-game-object []
  (let [new-id        (swap! game-counter inc)
        players       (atom #{})
        mafia         (atom nil)
        suspicions    (atom {})
        last-updated  (atom (java.util.Date.))
        game          (Game. new-id players mafia suspicions last-updated)]
    (add-watch players    :modified (update-players game))
    (add-watch mafia      :modified (watch-mafia game))
    (add-watch suspicions :modified (broadcast-aggregate game))
    (add-watch suspicions :updated  
      (fn [k r o n] (reset! last-updated (java.util.Date.))))
    game))

;; All games

(def games (atom {}))

(defn game [id]
  (@games id))

(defn new-game []
  (let [game (create-game-object)]
    (swap! games assoc (:id game) game)
    (:id game)))

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

(defn add-player! [game player]
  (swap! (:players game) conj player)
  game)

;; Eliminating players

(defn- remove-player-from-suspicions [suspicions player]
  (let [other-players (remove (partial = player) (keys suspicions))
        suspicions    (transient suspicions)]
    (dissoc! suspicions player)
    (doseq [other other-players]
      (assoc! suspicions other
        (vec (remove (partial = player) (suspicions other)))))
    (persistent! suspicions)))

(defn eliminate! [game player]
  (swap! (:players game) set/difference #{player})
  game)

;; Selecting mafia

(defn choose-mafia! [game num-mafia]
  (let [mafia (repeatedly num-mafia 
                #(rand-nth (vec @(:players game))))]
    (reset! (:mafia game) (set mafia))))

;; Handling changes in players

(defn update-players [game]
  (fn [k r old-state new-state]
    (let [changed (first (util/abs-difference old-state new-state))]
      (if (< (count old-state) (count new-state))
        (do
          (swap! (:suspicions game) add-player-and-insert-suspicions changed)
          (println "New player:" changed))
        (do
          (swap! (:suspicions game) remove-player-from-suspicions changed)
          (println "Eliminated:" changed)
          (swap! (:mafia game) set/difference #{changed}))))))

(defn watch-mafia [game]
  (fn [k r old-state new-state]
    (cond 
      (< 0 (count new-state)) (println (format "%s are the mafia!" new-state))
      (= 0 (count new-state)) (println "All the mafia are dead!"))))

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
