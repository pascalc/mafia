(ns mafia.server
  (:use [org.httpkit.server]
        [compojure.handler :only [site]]
        [compojure.core :only [defroutes GET POST]])
  (:require [cheshire.core :as json]
            (mafia
              [core :as mafia]
              [suspicions :as suspicion]
              [flow :as flow]))) 

;; Communication

;; In

;; TODO Register players and viewers

(defn message-dispatcher
  [game channel {:keys [type] :as msg}]
  (println "Dispatching" msg)
  (case (keyword type)
    :register (mafia/register! game (keyword (msg :name)) channel)))

;; Out

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

;; Handler

(defn ws-handler [game-id request]
  (with-channel request ch
    (on-receive ch 
      (fn [data]
        (message-dispatcher 
          (mafia/game game-id) 
          ch 
          (-> data 
            json/parse-string 
            clojure.walk/keywordize-keys))))))

;; Routes

(defroutes routes
  (POST "/game" []
    (let [id (:id (mafia/new-game))]
      {:status  201
       :headers {"Content-Type" "application/json"}
       :body    (json/generate-string {:id id})}))
  (GET "/game/:game-id" [game-id :as request] 
    (ws-handler (Integer. game-id) request))
  (POST "/game/:game-id/start" [game-id]
    (flow/start! (mafia/game (Integer. game-id)))
    {:status 204}))

;; Server

(defonce server (atom nil))

(defn start-server! []
  (reset! server 
    (run-server (site #'routes) {:port 4714})))

(defn stop-server! []
  (@server))
