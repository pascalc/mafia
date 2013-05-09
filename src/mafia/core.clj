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
        channels      {:players (atom {}), :viewers (atom #{})}
        last-updated  (atom (java.util.Date.))
        game          (Game. new-id players mafia suspicions channels last-updated)]
    (add-watch players    :modified (player/sync-suspicions game))
    (add-watch mafia      :modified (io/watch-mafia game))
    (add-watch suspicions :modified (io/broadcast-aggregate game))
    (add-watch suspicions :updated  
      (fn [k r o n] (reset! last-updated (java.util.Date.))))
    game))

;; Registering communication channels

(defn register-player! [game player channel]
  (swap! (get-in (:channels game) [:players]) 
    assoc player channel)
  (player/add-player! game player)
  (io/send-to-players! game #{player} 
    {:suspicions (get @(:suspicions game) player)}))

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
