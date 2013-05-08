(defproject mafia "0.1.0-SNAPSHOT"
  :description "Backend for a tech-augmented version of Mafia"
  :url "http://maf.io"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [http-kit "2.1.1"]
                 [compojure "1.1.5"]
                 [cheshire "5.1.1"]]
  :source-paths ["src"]
  :repl-options {:init-ns mafia.core})
