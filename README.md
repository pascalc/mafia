# mafia

Backend for a tech-augmented version of Mafia

## Usage
    mafia.core=> (add-player! :pascal)
    {:pascal []}
    []
    {:pascal []}

    mafia.core=> (add-player! :fredrik)
    {:pascal [:fredrik], :fredrik [:pascal]}
    [[:pascal 0] [:fredrik 0]]
    {:pascal [:fredrik], :fredrik [:pascal]}
    
    mafia.core=> (add-player! :rasmus)
    {:pascal [:fredrik :rasmus],
     :fredrik [:pascal :rasmus],
     :rasmus [:pascal :fredrik]}
    [[:pascal 0] [:fredrik 1] [:rasmus 2]]
    {:pascal [:fredrik :rasmus], :fredrik [:pascal :rasmus], :rasmus [:pascal :fredrik]}
    
    mafia.core=> @aggregate
    [[:pascal 0] [:fredrik 1] [:rasmus 2]]
    
    mafia.core=> (eliminate! :pascal)
    {:rasmus [:fredrik], :fredrik [:rasmus]}
    [[:rasmus 0] [:fredrik 0]]
    {:rasmus [:fredrik], :fredrik [:rasmus]}
    
    mafia.core=> @aggregate
    [[:rasmus 0] [:fredrik 0]]

## License

Copyright © 2013 Pascal Chatterjee

Distributed under the Eclipse Public License, the same as Clojure.
