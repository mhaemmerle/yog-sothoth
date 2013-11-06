(ns yog-sothoth.proxy
  (:require [clojure.tools.logging :as log]
            [aleph.tcp :refer [start-tcp-server tcp-client]]
            [lamina.core :refer [receive-all enqueue wait-for-result
                                 on-closed siphon map*]]
            [gloss.core :refer [finite-frame string]]
            [cheshire.core :refer [parse-string]]
            [yog-sothoth [gui :as gui] [config :as c]]))

(def decoder #(parse-string %))

(defn get-frame-format []
  (finite-frame :int32 (string :utf-8)))

(defn create-remote-channel [host port]
  (log/info "create-remote-channel")
  (let [remote-channel (wait-for-result
                        (tcp-client {:host host
                                     :port port
                                     :frame (get-frame-format)}))]
    (on-closed remote-channel #(log/info "remote channel closed"))
    remote-channel))

(defn decode [msg {:keys [from] :as meta}]
  (try
    (let [decoded-msg (decoder msg)
          event {:from from :to nil :size 0 :info "" :data decoded-msg}]
      (gui/add-event event)
      msg)
    (catch Exception e (str "failed to decode message" msg))
    (finally
      ;; FIXME add error event
      msg)))

(defn handler [local-channel client-info]
  (let [remote-channel (create-remote-channel (c/get :remote-host) (c/get :remote-port))]
    (on-closed local-channel #(log/info "local channel closed"))
    (siphon (map* #(decode % {:from :local}) local-channel) remote-channel)
    (siphon (map* #(decode % {:from :remote}) remote-channel) local-channel)))

(defn start-server [port]
  (start-tcp-server handler {:port port :frame (get-frame-format)}))
