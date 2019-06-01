(ns robots-vs-dinosaurs.logic.position
  (:require [robots-vs-dinosaurs.direction :as direction]))

;(defprotocol Position "Position protocol."
;
;  (move-towards [this direction] "Moves towards a facing direction."))
;
;(defrecord Position2d [x y] Position
;
;  (move-towards
;    [this {coordinate-x :x coordinate-y :y}]
;    (+ 1 0)))
;;(->Coordinates2 ((+ x (:x direction))
;;                (+ y (:y direction))))))
;
;(defn make-position2d
;  "Makes new coordinates."
;  [x y]
;  {:pre [(pos? x)
;         (pos? y)]}
;  (->Position2d x y))