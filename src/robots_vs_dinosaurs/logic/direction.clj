(ns robots-vs-dinosaurs.logic.direction)

(defonce
  four-sided-directions
  {:up    [0 1]
   :right [1 0]
   :down  [0 -1]
   :left  [-1 0]})

(defonce
  eight-sided-directions
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
  (->Direction orientation (orientation directions)))

(defn new-four-sided-direction
  "Creates a directions with 4 sides."
  [orientation]
  (new-direction orientation four-sided-directions))

(defn new-eight-sided-direction
  "Creates a directions with 8 sides."
  [orientation]
  (new-direction orientation eight-sided-directions))

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

(defn direction++
  "Gets the next Direction."
  [direction directions]
  (-> direction
      (direction->index directions)
      (inc)
      (index->direction directions)))

(defn direction--
  "Gets the previous direction."
  [direction directions]
  (-> direction
      (direction->index directions)
      (dec)
      (index->direction directions)))