(ns yog-sothoth.proxy
  (:require [clojure.tools.logging :as log]
            [aleph.tcp :refer [start-tcp-server tcp-client]]
            [lamina.core :refer [receive-all enqueue wait-for-result
                                 on-closed siphon map*]]
            [gloss [core :refer [finite-frame finite-block string]]
             [io :refer [contiguous]]]
            [cheshire.core :refer [parse-string]]
            [yog-sothoth [gui :as gui] [config :as c]]
            [byte-streams :refer [convert]]))

(def decoder #(parse-string % true))

(defn get-frame-format [] (finite-block :int32))

(defn create-remote-channel [host port]
  (log/info "create-remote-channel")
  (let [remote-channel (wait-for-result
                        (tcp-client {:host host
                                     :port port
                                     :frame (get-frame-format)}))]
    (on-closed remote-channel #(log/info "remote channel closed"))
    remote-channel))

(defn relay-and-log [buffer {:keys [from] :as meta}]
  (try
    (let [b (contiguous buffer)
          decoded (decoder (convert b String))
          event {:from from :to nil :size (.capacity b) :info :ok :data decoded}]
      (gui/add-event event)
      buffer)
    (catch Exception e
      (log/error "failed to decode message: " e)
      (gui/add-event {:from from :to nil :size nil :info :error :data e}))
    (finally
      ;; FIXME add error event
      buffer)))

(defn handler [local-channel client-info]
  (let [remote-channel (create-remote-channel (c/get :remote-host) (c/get :remote-port))]
    (on-closed local-channel #(log/info "local channel closed"))
    (siphon (map* #(relay-and-log % {:from :local}) local-channel) remote-channel)
    (siphon (map* #(relay-and-log % {:from :remote}) remote-channel) local-channel)))

(defn start-server [port]
  (start-tcp-server handler {:port port :frame (get-frame-format)}))
