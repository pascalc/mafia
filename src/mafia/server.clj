(ns mafia.server
  (:use [org.httpkit.server]
        [compojure.handler :only [site]]
        [compojure.core :only [defroutes GET POST]])
  (:require [cheshire.core :as json]
            (mafia
              [core :as mafia]
              [flow :as flow]))) 

;; TODO Register players and viewers

(defn message-dispatcher
  [game channel {:keys [type] :as msg}]
  (println "Dispatching" msg)
  (case (keyword type)
    :register-player (mafia/register-player! game (keyword (msg :name)) channel)
    :register-viewer (mafia/register-viewer! game channel)))

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
