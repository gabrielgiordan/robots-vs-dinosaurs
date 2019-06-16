(ns robots-vs-dinosaurs.logic.point)

;;
;; Point
;;
(defrecord Point [x y])

(defn new-point
  "Creates a new point."
  [x y]
  (->Point x y))

(defn point?
  [o]
  (instance? Point o))

(defn point+
  "Sum two points."
  [a b]
  (apply merge-with + [a b]))

(defn point-
  "Subtract two points."
  [a b]
  (apply merge-with - [a b]))

(defn points+
  "Sum a collection of points,
  each point plus the given point."
  [coll point]
  (map (partial point+ point) coll))

(defn points-
  "Subtract a collection of points,
  each point minus the given point."
  [coll point]
  (map #(point- % point) coll))

(defn around
  "Gets a coll of summed
  direction points."
  [directions point]
  (points+ (vals directions) point))

(defn toward
  "Move the point
  towards the direction."
  [point direction]
  (point+ point (:point direction)))

(defn away
  "Move the point away
  from the direction."
  [point direction]
  (point- point (:point direction)))
