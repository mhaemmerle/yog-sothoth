(ns yog-sothoth.core
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [yog-sothoth [gui :as gui] [proxy :as proxy] [config :as c]]))

(set! *warn-on-reflection* true)

(defn- at-exit [runnable]
  (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable runnable)))

(def demo-event-data [{:from "0" :to "1" :size 0 :info []}
                      {:from "1" :to "0" :size 1 :info []}
                      {:from "1" :to "0" :size 2 :info [] :data {:event :test}}])

(defn populate-with-debug [events]
  (doseq [event events]
    (gui/add-event event)))

(defn -main
  [& args]
  (gui/initialize)
  (populate-with-debug demo-event-data)
  (let [local-port (c/get :local-port)
        server-stop-fn (proxy/start-server local-port)]
    (at-exit server-stop-fn))
  (log/info "Application started."))
