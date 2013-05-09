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

(defn watch-mafia [game]
  (fn [k r old-state new-state]
    (cond 
      (= 0 (count old-state)) (println (format "%s are the mafia!" new-state))
      (= 0 (count new-state)) (println "All the mafia are dead!"))))

;; Suspicions

(defn broadcast-aggregate [game] 
  (fn [k r old-state new-state]
    (let [aggregate-info (suspicion/aggregate-suspicions (vals @r))]
      (pprint @r)
      (pprint 
        {(str "Aggregate #" (:id game)) 
         aggregate-info})
      (broadcast! game 
        {:aggregate (map first aggregate-info)}))))
