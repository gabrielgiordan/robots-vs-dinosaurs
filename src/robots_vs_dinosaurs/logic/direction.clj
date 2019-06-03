(ns robots-vs-dinosaurs.logic.direction)

(defonce
  four-sides
  {:up    [0 1]
   :right [1 0]
   :down  [0 -1]
   :left  [-1 0]})

(defonce
  eight-sided
  {:up-left    [-1 1]
   :up         [0 1]
   :up-right   [1 1]
   :right      [1 0]
   :down-right [1 -1]
   :down       [0 -1]
   :down-left  [-1 -1]
   :left       [-1 0]})

(defrecord Direction [orientation point])

(defn new-direction
  "Creates a new Direction."
  [orientation directions]
  (map->Direction
    {:orientation orientation
     :directions (orientation directions)}))

(defn new-four-sided
  "Creates a directions with 4 sides."
  [orientation]
  (new-direction orientation four-sides))

(defn new-eight-sided
  "Creates a directions with 8 sides."
  [orientation]
  (new-direction orientation eight-sided))

(defn direction->index
  "Converts a Direction to its index."
  [{:keys [orientation]} directions]
  (->> (keep-indexed #(when (= orientation (key %2)) %1) directions)
       (take 1)
       (first)))

(defn index->direction
  "Converts an index to a Direction.
  If out of bounds, the index is normalized."
  [index directions]
  (let [length (count directions)
        normalized (mod index length)]
    (-> (take 1 (keep-indexed #(when (= normalized %1) %2) directions))
        (first)
        (first)
        (new-direction directions))))

(defn- direction-inc
  "Gets the next direction."
  [direction directions]
  (-> direction
      (direction->index directions)
      (inc)
      (index->direction directions)))

(defn- direction-dec
  "Gets the previous direction."
  [direction directions]
  (-> direction
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
  (direction-inc direction eight-sided))

(defn eight-sided-dec
  "Gets the previous eight sided direction."
  [direction]
  (direction-dec direction eight-sided))