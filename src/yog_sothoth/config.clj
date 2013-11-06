(ns yog-sothoth.config
  (:refer-clojure :exclude [get]))

(def config {:local-port 7070
             :remote-port 9003
             :remote-host "localhost"
             :window-size {:width 1000 :height 1000}})

(defn get [key]
  (clojure.core/get config key))
