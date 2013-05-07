(ns mafia.flow
  (:use [clojure.pprint]
        [mafia.players :only [choose-mafia! eliminate-most-suspicious!]])
  (:require [org.httpkit.timer :as timer]))

(def ROUND-LENGTH-MS
  "The length of one round of Mafia in ms"
  (* 1000 10))

(declare play-round!)

(defn start! [game]
  (when-let [num-mafia (count @(:players game))]
    (choose-mafia! game)
    (play-round! game)))

(defn victory? [game]
  (let [num-mafia     (count @(:mafia game))
        num-civilians (- (count @(:players game)) num-mafia)]
    (cond 
      (= num-mafia num-civilians) :mafia
      (= num-mafia 0)             :civilians
      :else                       false)))

(defn play-round! [game]
  (println "Playing a round of game" (:id game))
  (pprint game)
  (timer/schedule-task ROUND-LENGTH-MS
    (eliminate-most-suspicious! game)
    (if-let [winners (victory? game)]
      (do
        (println "Game" (:id game) "is over, the" winners "have won!"))
      (do
        (println "Let game" (:id game) "continue!")
        (play-round! game)))))
