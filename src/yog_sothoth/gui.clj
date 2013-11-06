(ns yog-sothoth.gui
  (:require [clojure.tools.logging :as log]
            [cheshire.core :refer [parse-string]]
            [seesaw [core :as sc] [tree :as st] [bind :as sb] [selection :as se]]
            [yog-sothoth.config :as c]))

(sc/native!)

(def events-model (atom []))
(def detail-model (atom nil))

(def empty-table-model [:columns [{:key :from :text "From"}
                                  {:key :to :text "To"}
                                  {:key :size :text "Size"}
                                  {:key :info :text "Info"}]
                        :rows []])

(defn- add-row [model row]
  (let [[_ columns _ rows] model]
    [:columns columns :rows (conj rows row)]))

(defn add-event [event]
  (swap! events-model add-row event)
  nil)

(defn remove-all-events []
  (reset! events-model empty-table-model)
  nil)

(defn create-frame []
  (sc/frame :title "Yog 0.1.0-SNAPSHOT"
            :width (:width (c/get :window-size))
            :height (:height (c/get :window-size))
            :on-close :exit))

(defn event-item-renderer [renderer info]
  (sc/config! renderer :text (:label (:value info))))

(defn create-border []
  (javax.swing.BorderFactory/createLineBorder java.awt.Color/BLACK))

(defn on-selected-table-row [table event]
  (let [selection (sc/selection table)
        row-data (seesaw.table/value-at table selection)]
    (reset! detail-model (:data row-data))
    nil))

(defn create-events-table []
  (let [table (sc/table :id :events
                        :show-grid? true
                        :show-horizontal-lines? true
                        :show-vertical-lines? true)]
    (sb/bind events-model (sb/property table :model))
    (sc/listen table :selection (partial on-selected-table-row table))
    (reset! events-model empty-table-model)
    table))

(defn create-tree-model [data]
  (st/simple-tree-model seq? identity data))

(defn create-detail-tree []
  (let [tree (sc/tree :id :tree)]
    (sb/bind detail-model
             (sb/transform create-tree-model)
             (sb/property tree :model))
    tree))

(defn create-header []
  (let [local-items [(sc/label "Local")
                     (sc/text :text (c/get :local-port) :editable? false)]
        local-group (sc/horizontal-panel :items local-items)
        remote-items [(sc/label "Remote")
                      (sc/text :text (c/get :remote-port) :editable? false)]
        remote-group (sc/horizontal-panel :items remote-items)]
    (sc/flow-panel :id :header :items [local-group remote-group] :align :right)))

(defn create-frame-content []
  (let [split-pane (sc/top-bottom-split
                    (sc/scrollable (create-events-table))
                    (sc/scrollable (create-detail-tree) :hscroll :never))]
    (.setBorder split-pane nil)
    (sc/border-panel :border 5 :vgap 5 :hgap 5
                     :north (create-header)
                     :center split-pane
                     :south (sc/label :id :status :text "Status:"))))

(defn initialize []
  (let [frame (create-frame)
        frame-content (create-frame-content)]
    (.add ^javax.swing.JFrame frame frame-content)
    (sc/invoke-later (sc/show! frame))
    frame))
