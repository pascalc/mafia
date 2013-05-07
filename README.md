# mafia

Backend for a tech-augmented version of Mafia

## Usage
    mafia.core=>(-> (new-game) 
                  (player/add-player! :pascal) 
                  (player/add-player! :rasmus)
                  (player/add-player! :fredrik)
                  (player/add-player! :michael))

    mafia.core=> (start! (game 1))
    #{:michael} are the mafia!
    Playing a round of game 1
    {:id 1,
     :players #<Atom@21a80fb8: #{:pascal :fredrik :rasmus :michael}>,
     :mafia #<Atom@20a00a4b: #{:michael}>,
     :suspicions
     #<Atom@6b03d2a8: 
       {:pascal [:rasmus :fredrik :michael],
        :rasmus [:pascal :fredrik :michael],
        :fredrik [:pascal :rasmus :michael],
        :michael [:pascal :rasmus :fredrik]}>,
     :last-updated #<Atom@47fb4a7b: #inst "2013-05-07T21:39:31.845-00:00">}
    #<CancelableFutureTask timeout=10000ms, due in 10000ms>
    
    Eliminating the most suspicious player!
    {:michael [:rasmus :fredrik],
     :rasmus [:fredrik :michael],
     :fredrik [:rasmus :michael]}
    {"Aggregate #1" [[:rasmus 0] [:fredrik 1] [:michael 2]]}
    Eliminated: :pascal
    
    Let game 1 continue!
    Playing a round of game 1
    {:id 1,
     :players #<Atom@21a80fb8: #{:fredrik :rasmus :michael}>,
     :mafia #<Atom@20a00a4b: #{:michael}>,
     :suspicions
     #<Atom@6b03d2a8: 
       {:michael [:rasmus :fredrik],
        :rasmus [:fredrik :michael],
        :fredrik [:rasmus :michael]}>,
     :last-updated #<Atom@47fb4a7b: #inst "2013-05-07T21:39:46.584-00:00">}
    
    Eliminating the most suspicious player!
    {:michael [:fredrik], :fredrik [:michael]}
    {"Aggregate #1" [[:michael 0] [:fredrik 0]]}
    Eliminated: :rasmus
    
    Game 1 is over, the :mafia have won!

## License

Copyright © 2013 Pascal Chatterjee

Distributed under the Eclipse Public License, the same as Clojure.
