(ns robots-vs-dinosaurs.logic.direction
  (:require
    (clojure.spec
      [alpha :as s])
    (robots-vs-dinosaurs.logic
      [point :as point])))

(s/def :direction/orientation string?)
(s/def :direction/direction
  (s/keys :req-un [:direction/orientation :point/point]))

(defonce
  four-sides
  {:up    (point/new-point 0 1)
   :right (point/new-point 1 0)
   :down  (point/new-point 0 -1)
   :left  (point/new-point -1 0)})

(defonce
  eight-sides
  {:up-left    (point/new-point -1 1)
   :up         (point/new-point 0 1)
   :up-right   (point/new-point 1 1)
   :right      (point/new-point 1 0)
   :down-right (point/new-point 1 -1)
   :down       (point/new-point 0 -1)
   :down-left  (point/new-point -1 -1)
   :left       (point/new-point -1 0)})

(defrecord Direction [orientation point])

(defn new-direction
  "Creates a new Direction."
  [orientation directions]
  (map->Direction
    {:orientation orientation
     :point       (orientation directions)}))

(defn new-four-sided
  "Creates a directions with 4 sides."
  [orientation]
  (new-direction orientation four-sides))

(defn new-eight-sided
  "Creates a directions with 8 sides."
  [orientation]
  (new-direction orientation eight-sides))

(defn direction->index
  "Converts a Direction to its index."
  [{:keys [orientation]} directions]
  (->>
    (keep-indexed #(when (= orientation (key %2)) %1) directions)
    (take 1)
    (first)))

(defn index->direction
  "Converts an index to a Direction.
  If out of bounds, the index is normalized."
  [index directions]
  (let [length (count directions)
        normalized (mod index length)]
    (some->
      (take 1 (keep-indexed #(when (= normalized %1) %2) directions))
      (first)
      (first)
      (new-direction directions))))

(defn- direction-inc
  "Gets the next direction."
  [direction directions]
  (some->
    direction
    (direction->index directions)
    (inc)
    (index->direction directions)))

(defn- direction-dec
  "Gets the previous direction."
  [direction directions]
  (some->
    direction
    (direction->index directions)
    (dec)
    (index->direction directions)))

(defn four-sided-inc
  "Gets the next four sided direction."
  [direction]
  (direction-inc direction four-sides))

(defn four-sided-dec
  "Gets the previous four sided direction."
  [direction]
  (direction-dec direction four-sides))

(defn eight-sided-inc
  "Gets the next eight sided direction."
  [direction]
  (direction-inc direction eight-sides))

(defn eight-sided-dec
  "Gets the previous eight sided direction."
  [direction]
  (direction-dec direction eight-sides))