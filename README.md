# mafia

Backend for a tech-augmented version of Mafia

## Usage
    mafia.core=> (pprint suspicions)
    #<Atom@3a0fbd45: 
      {:pascal [:fredrik :rasmus :michael],
       :fredrik [:pascal :rasmus :michael],
       :rasmus [:pascal :fredrik :michael],
       :michael [:pascal :fredrik :rasmus]}>
    nil
    mafia.core=> (modify-suspicions! :fredrik :michael 0)
    (modify-suspicions! :fredrik :michael 0)
    {:pascal [:fredrik :rasmus :michael],
     :fredrik [:michael :pascal :rasmus],
     :rasmus [:pascal :fredrik :michael],
     :michael [:pascal :fredrik :rasmus]}
    [[:pascal 1] [:fredrik 2] [:michael 4] [:rasmus 5]]
    nil
    mafia.core=> (eliminate! :pascal)
    (eliminate! :pascal)
    Removing :pascal
    {:michael [:fredrik :rasmus],
     :fredrik [:michael :rasmus],
     :rasmus [:fredrik :michael]}
    [[:fredrik 0] [:michael 1] [:rasmus 2]]

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
