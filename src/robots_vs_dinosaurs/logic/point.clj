(ns robots-vs-dinosaurs.logic.point)

(defn new-point
  "Creates a new point."
  [x y]
  [x y])

(defn point+
  "Sum two points."
  [a b]
  (mapv + a b))

(defn point-
  "Subtract two points."
  [a b]
  (mapv - a b))

(defn points+
  "Sum a collection of points."
  [coll point]
  (mapv (partial point+ point) coll point))

(defn points-
  "Subtract a collection of points."
  [coll point]
  (mapv (partial point+ point) coll point))

(defn inbound?
  "Checks if a point is inbound a width and height."
  [[w h] [x y]]
  (and (>= x 0) (>= y 0) (< x w) (< y h)))