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
   started?
   winner
   last-updated])

(defn- create-game-object []
  (let [new-id        (swap! game-counter inc)
        players       (atom #{})
        mafia         (atom nil)
        suspicions    (atom {})
        channels      {:players (atom {}), :viewers (atom #{})}
        started?      (atom false)
        winner        (atom nil)
        last-updated  (atom (java.util.Date.))
        game          (Game. new-id players mafia suspicions channels 
                             started? winner last-updated)]
    (add-watch players    :elimination  (io/broadcast-player-eliminated game))
    (add-watch players    :sync         (player/sync-suspicions game))
    (add-watch mafia      :chosen       (io/watch-mafia game))
    (add-watch suspicions :aggregate    (io/broadcast-aggregate game))
    (add-watch suspicions :changed      (io/send-suspicions game))
    (add-watch suspicions :updated  
      (fn [k r o n] (reset! last-updated (java.util.Date.))))
    (add-watch started?   :started      (io/broadcast-started game))
    (add-watch winner     :winner       (io/broadcast-game-over game))
    game))

;; Registering communication channels

(defn register-player! [game player channel]
  (swap! (get-in (:channels game) [:players]) 
    assoc player channel)
  (player/add-player! game player))

(defn register-viewer! [game channel]
  (swap! (get-in (:channels game) [:viewers]) 
    conj channel))

;; All games

(defonce games (atom {}))

(defn game [id]
  (@games id))

(defn new-game []
  (let [game (create-game-object)]
    (swap! games assoc (:id game) game)
    game))
