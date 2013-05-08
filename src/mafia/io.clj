(ns mafia.io
  (:use [clojure.pprint]
        [org.httpkit.server :only [send!]])
  (:require [cheshire.core :as json]
            (mafia
              [suspicions :as suspicion]
              [players :as player])))

;; Broadcasters

;; TODO Send to players and viewers
 
(defn broadcast! [game data]
  (doseq [channel (vals @(:channels game))]
    (send! channel (json/generate-string data))))

(defn send-to-players! [game players data]
  (doseq [channel (vals (select-keys @(:channels game) players))]
    (send! channel (json/generate-string data))))

(defn send-to-mafia! [game data]
  (send-to-players! game @(:mafia game) data))

(defn send-to-civilians! [game data]
  (send-to-players! game 
    (clojure.set/difference 
      @(:players game)
      @(:mafia game))
    data))

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
