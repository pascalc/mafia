(ns mafia.players
  (:use [mafia.suspicions :only [aggregate]])
  (:require [clojure.set :as set]
            [mafia.util :as util]))

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

(defn eliminate-most-suspicious!
  [game]
  (println "Eliminating the most suspicious player!")
  (let [most-suspicious (->> (aggregate game) first first)]
    (eliminate! game most-suspicious)))

;; Selecting mafia

(defn num-mafia
  "How many mafia given this many players?"
  [num-players]
  (cond
    (<= num-players 2)  nil ;; Too few to play
    (<= num-players 5)  1
    (>= num-players 6)  2))

(defn choose-mafia! [game]
  (let [players @(:players game)
        mafia   (repeatedly (num-mafia (count players))
                  #(rand-nth (vec players)))]
    (reset! (:mafia game) (set mafia))))

;; Handling changes in players

(defn player-added? [old-state new-state]
  (let [changed (first (util/abs-difference old-state new-state))]
    (when (< (count old-state) (count new-state))
      changed)))

(defn player-removed? [old-state new-state]
  (let [changed (first (util/abs-difference old-state new-state))]
    (when (> (count old-state) (count new-state))
      changed)))

(defn sync-suspicions [game]
  (fn [k r old-state new-state]
    (let [added   (player-added? old-state new-state)
          removed (player-removed? old-state new-state)]
      (cond
        added   (swap! (:suspicions game) add-player-and-insert-suspicions added)
        removed (do
                  (swap! (:suspicions game) remove-player-from-suspicions removed)
                  (swap! (:mafia game) set/difference #{removed}))))))

