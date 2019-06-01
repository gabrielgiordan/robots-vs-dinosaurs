(ns robots-vs-dinosaurs.logic.direction
  (:require [robots-vs-dinosaurs.util :as util]))

;;; Those others directions are for another implementation, perhaps a direction with 8 sides.
;(def ^:const ^:private directions {:up-left    {:x -1 :y 1}
;                                   :up         {:x 0 :y 1}
;                                   :up-right   {:x 1 :y 1}
;                                   :right      {:x 1 :y 0}
;                                   :down-right {:x 1 :y -1}
;                                   :down       {:x 0 :y -1}
;                                   :down-left  {:x -1 :y -1}
;                                   :left       {:x -1 :y 0}})
;
;(defprotocol Dir
;  (get-dir [this]))
;
;(extend-protocol Dir
;  :4
;  )
;
;(defprotocol Direction "Direction protocol."
;
;  (get-available-directions [this] "Returns the direction as a vec.")
;
;  (get-available-rotations [this] "Returns the available rotations as a vec.")
;
;  (get-index [this] "Returns the current direction index.")
;
;  (rotate-to [this to] "Rotates the direction towards the rotation direction.")
;
;  (get-coordinates [this] "Gets the coordinates of the current direction."))
;
;(def ^:private -directions {:up :right :down :left})
;(def ^:private -rotations {:right :left})
;
;(def ^:private -directions-vec (memoize (util/map->vec -directions))) ; Memoize, to cache result.
;(def ^:private -rotations-vec (memoize (util/map->vec -rotations)))
;
;;(alter-var-root #'sqrt-denom memoize)
;
;
;(defrecord DirectionWith4Sides [direction] Direction
;
;  (get-available-directions
;    [_]
;    (-directions-vec))
;
;  (get-available-rotations
;    [_]
;    (-rotations-vec))
;
;  (get-index
;    [this]
;    (->> (get-available-directions this)
;         (keep-indexed #(when (= direction %2) %1))
;         (first)))
;
;  (rotate-to
;    [this to]
;    (let [inc-or-dec (if (= to :right) inc dec)]
;      (->DirectionWith4Sides ((get-available-directions this) (-> (get-index this)
;                                                                  (inc-or-dec)
;                                                                  (mod 4))))))
;
;  (get-coordinates
;    [_]
;    (directions direction)))
;
;(defn make-direction-with-4-sides
;  "Makes a new direction with 4 sides. The `initial-direction` can be `:up`, `:right`, `:down` or `:left`."
;  [initial-direction]
;  {:pre [(contains? -directions initial-direction)]}
;  (->DirectionWith4Sides initial-direction))
;
;;(get-coordinates (rotate-to (make-direction-4 :up) :left))
;
;;(get-available-directions (make-direction-4 :up))