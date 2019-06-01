(ns robots-vs-dinosaurs.logic.robot
  (:require [robots-vs-dinosaurs.direction :as direction]
            [robots-vs-dinosaurs.position :as coordinates]))

;
;(prn "kk")
;(def coord (coordinates/make 1 2))
;(def dir (direction/make-direction-4 :up))
;(prn coord)
;(prn dir)
;(prn (coordinates/move-towards coord dir))
;

;(def ^:private directions {:up [0 1] :right [1 0] :down [0 -1] :left [-1 0]})
;
;(declare turn-left turn-right rotate get-direction-index get-direction-at index-of)
;
;(defn make-robot
;  "Makes a robot at `x` and `y` with facing direction."
;  [x y facing-direction]
;  {:coordinates [x y] :facing-direction facing-direction})
;
;(defn turn-right
;  "Turns the robot right."
;  [robot]
;  (assoc robot :facing-direction (rotate robot inc)))
;
;(defn turn-left
;  "Turns the robot left."
;  [robot]
;  (assoc robot :facing-direction (rotate robot dec)))
;
;(defn- rotate
;  "Rotates a robot clockwise (`inc`) or anti-clockwise (`dec`)."
;  [robot inc-or-dec]
;  (let [index (get-direction-index (robot :facing-direction))
;        new-index (mod (inc-or-dec index) 4)]
;    ((get-direction-at new-index) 0)))
;
;(defn- get-direction-index
;  "Gets the direction index, comparing by `=`."
;  [dir]
;  (index-of #(when (= dir (%2 0)) %1) directions))
;
;(defn- get-direction-at
;  "Gets the direction, given an `index`."
;  [index]
;  (index-of #(when (= index %1) %2) directions))
;
;(defn- index-of
;  "Gets the element in the given `coll` given the predicate `pred`."
;  [pred coll]
;  (first (keep-indexed pred coll)))
;
;(defn- move
;  "Moves the robot forward (`+`) or backwards (`-`)"
;  [robot +-]
;  (mapv +- (robot :coordinates) (directions (robot :facing-direction))))
;
;(defn move-forward
;  "Moves the robot forward."
;  [robot]
;  (assoc robot :coordinates (move robot +)))
;
;(defn move-backwards
;  "Moves the robot backwards."
;  [robot]
;  (assoc robot :coordinates (move robot -)))
;
;(defn attack [])
