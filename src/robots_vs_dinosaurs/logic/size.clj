(ns robots-vs-dinosaurs.logic.size)

(defrecord Size [width height])

(defn new-size
  [width height]
  (map->Size
    {:width  width
     :height height}))