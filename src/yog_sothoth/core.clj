(ns yog-sothoth.core
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [yog-sothoth
             [config :as c]
             [gui :as gui]
             [proxy :as proxy]]))

(set! *warn-on-reflection* true)

(defn- at-exit [runnable]
  (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable runnable)))

;; (def d {:jsonrpc "2.0" :method "setup" :params [] :id "null"})
;; (def d2 {:jsonrpc "2.0" :method "setup" :params [{:user_id "123"}] :id "null"})

;; (def demo-event-data [{:from "0" :to "1" :size 0 :info [] :data d}
;;                       {:from "1" :to "0" :size 1 :info [] :data d}
;;                       {:from "1" :to "0" :size 2 :info [] :data d2}])

;; (defn populate-with-debug [events]
;;   (doseq [event events]
;;     (gui/add-event event)))

(defn -main
  [& args]
  (gui/initialize)
  ;; (populate-with-debug demo-event-data)
  (let [local-port (c/get :local-port)
        server-stop-fn (proxy/start-server local-port)]
    (at-exit server-stop-fn))
  (log/info "Application started."))
