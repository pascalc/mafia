(ns mafia.io
  (:use [clojure.pprint]
        [org.httpkit.server :only [send!]])
  (:require [cheshire.core :as json]
            (mafia
              [suspicions :as suspicion]
              [players :as player])))

(defn player-channels
  [game & {:keys [players]}]
  (let [channels @(get-in (:channels game) [:players])]
    (vals
      (if players
        (select-keys channels players)
        channels))))

(defn viewer-channels
  [game]
  @(get-in (:channels game) [:viewers]))

;; Broadcasters

;; TODO Send to players and viewers
 
(defn send-to-viewers! [game data]
  (doseq [channel (viewer-channels game)]
    (send! channel (json/generate-string data))))

(defn send-to-players! [game players data]
  (doseq [channel (player-channels game :players players)]
    (send! channel (json/generate-string data))))

(defn send-to-player! [game player data]
  (doseq [channel (player-channels game :players #{player})]
    (send! channel (json/generate-string data))))

(defn send-to-mafia! [game data]
  (send-to-players! game @(:mafia game) data))

(defn send-to-civilians! [game data]
  (send-to-players! game 
    (clojure.set/difference 
      @(:players game)
      @(:mafia game))
    data))

(defn broadcast! [game data]
  (send-to-players! game nil data)
  (send-to-viewers! game data))

;; Listeners 

;; Players

(defn broadcast-player-eliminated [game]
  (fn [k r old-state new-state]
    (when-let [removed (player/player-removed? old-state new-state)]
      (send-to-players! game nil
        {:event       "player-eliminated"
         :eliminated  removed
         :mafia       (contains? @(:mafia game) removed)})
      (send-to-viewers! game
        {:event       "player-eliminated"
         :eliminated  removed
         :mafia       (contains? @(:mafia game) removed)
         :suspicions  @(:suspicions game)}))))

(defn watch-mafia [game]
  (fn [k r old-state new-state]
    (cond 
      (> (count new-state) (count old-state)) 
        (do 
          (println (format "%s are the mafia!" new-state))
          (doseq [m new-state]
            (send-to-player! game m
              {:event "mafia-chosen", 
               :mafia true
               :other_mafia (remove #{m} new-state)}))
          (send-to-civilians! game {:event "mafia-chosen", :mafia false}))
      (= 0 (count new-state)) 
        (println "All the mafia are dead!"))))

;; Suspicions

(defn send-suspicions [game] 
  (fn [k r old-state new-state]
    (doseq [[player new-suspicions] new-state]
      (when (not (= new-suspicions (old-state player)))
        (println "Sending new suspicions to" player)
        (send-to-player! game player
          {:event     "set-suspicions"
           :suspicions new-suspicions})))))

(defn broadcast-aggregate [game] 
  (fn [k r old-state new-state]
    (let [aggregate-info (suspicion/aggregate-suspicions (vals @r))]
      (pprint @r)
      (pprint 
        {(str "Aggregate #" (:id game)) 
         aggregate-info})
      (send-to-viewers! game 
        {:event     "aggregate"
         :aggregate (map first aggregate-info)}))))

;; Game state

(defn broadcast-started [game] 
  (fn [k r old-state new-state]
    (broadcast! game 
      {:event   "started"})))

(defn broadcast-game-over [game] 
  (fn [k r old-state new-state]
    (broadcast! game 
      { :event "game-over"
        :winner new-state})))
