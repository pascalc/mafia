(ns mafia.core
  (:use [clojure.pprint]
        [mafia.flow])
  (:require [clojure.set :as set]
            (mafia
              [util       :as util]
              [players    :as player]
              [suspicions :as suspicion])))

;; Single games

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
    (add-watch players    :modified (player/update-players game))
    (add-watch mafia      :modified (player/watch-mafia game))
    (add-watch suspicions :modified (suspicion/broadcast-aggregate game))
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
    game))
