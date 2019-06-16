(ns robots-vs-dinosaurs.logic.size)

;;
;; Size
;;
(defrecord Size [width height])

(defn new-size
  "Creates a new size."
  [width height]
  (map->Size
    {:width  width
     :height height}))