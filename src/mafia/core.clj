(ns mafia.core
  (:use [clojure.pprint]
        [mafia.flow])
  (:require [clojure.set :as set]
            (mafia
              [util       :as util]
              [players    :as player]
              [io         :as io])))

;; Single games

(defonce game-counter (atom 0))

(defrecord Game 
  [id
   players
   mafia
   suspicions
   channels
   last-updated])

(defn- create-game-object []
  (let [new-id        (swap! game-counter inc)
        players       (atom #{})
        mafia         (atom nil)
        suspicions    (atom {})
        channels      (atom {})
        last-updated  (atom (java.util.Date.))
        game          (Game. new-id players mafia suspicions channels last-updated)]
    (add-watch players    :modified (player/sync-suspicions game))
    (add-watch mafia      :modified (io/watch-mafia game))
    (add-watch suspicions :modified (io/broadcast-aggregate game))
    (add-watch suspicions :updated  
      (fn [k r o n] (reset! last-updated (java.util.Date.))))
    game))

;; Registering communication channels

;; TODO Register players and viewers

(defn register! [game player channel]
  (swap! (:channels game) assoc player channel)
  (player/add-player! game player))

;; All games

(defonce games (atom {}))

(defn game [id]
  (@games id))

(defn new-game []
  (let [game (create-game-object)]
    (swap! games assoc (:id game) game)
    game))
