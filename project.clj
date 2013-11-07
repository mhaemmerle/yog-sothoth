(defproject yog-sothoth "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.slf4j/slf4j-api "1.6.6"]
                 [org.slf4j/slf4j-log4j12 "1.6.6"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/tools.cli "0.2.1"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [aleph "0.3.0"]
                 [cheshire "5.2.0"]
                 [seesaw "1.4.4"]
                 [byte-streams "0.1.6"]]
  :repositories [["sonatype-oss-public"
                  "https://oss.sonatype.org/content/groups/public/"]]
  :plugins [[lein-marginalia "0.7.1"]]
  :main yog-sothoth.core)
