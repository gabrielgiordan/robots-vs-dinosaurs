(ns robots-vs-dinosaurs.logic.board
  (:require [robots-vs-dinosaurs.spec :as spec]))

(deftype Point [x y])

;; Direction
(defrecord Direction [name point])
(defrecord Orientation [direction available-directions])

(defn new-direction
  [direction-key available]
  (->Direction direction-key (direction-key available) available))

(defn get-orientation
  ""
  [data]
  (:orientation data))

(defn get-facing
  ""
  [data]
  (:facing data))

(defn get-direction-index
  [{:keys [orientation available-directions]}]
  (let [keep-orientation
        (fn [index facing]
          (when (= orientation (key facing)) index))]
  (first (take 1 (keep-indexed keep-orientation available-directions)))))



;; Unit
(defprotocol Positionable
  (get-position [this] ""))

;; Dinosaur
(defrecord Dinosaur [id position direction])

(extend-protocol Positionable
  Dinosaur
  (get-position [this] (:position this)))

(defn new-dinosaur
  [id position direction]
  (->Dinosaur id position direction))



;; Robot
(defprotocol IRobot
  (get-score [this] ""))

(defrecord Robot [id position direction score]
  IRobot
  (get-score [this] (:score this)))

(extend-protocol IUnit
  Robot
  (get-id [this] (:id this))
  (get-position [this] (:position this))
  (get-direction [this] (:direction this)))

(defn new-robot
  [id position direction]
  (->Robot id position direction 0))


;; Scoreboard
(defrecord Scoreboard [total])

(defn new-scoreboard
  ([score]
   (->Scoreboard score))
  ([]
   (new-scoreboard 0)))

(defn get-total
  [data]
  (:total data))

(defn sum-total
  [data n]
  (new-scoreboard
    (+ n (get-total data))))


;; Board
(defrecord Board [size units])

(defn new-board
  ([size units]
   (->Board size units))
  ([size]
   (->Board size #{})))

(defn get-size
  [data]
  (:size data))

(defn get-units
  [data]
  (:units data))


;; Simulation
(defrecord Simulation [id scoreboard board])

(defn new-simulation
  ([id scoreboard board]
   (->Simulation id scoreboard board))
  ([id board]
   (new-simulation id (new-scoreboard) board)))




;(defonce ^:const default-data :empty)
;
;(defrecord Size [width height])
;(defrecord Board [matrix size default-data])
;
;(defrecord Board2d [size])
;
;(defn inbound?
;  "Returns positive when `x` and `y` is inbound."
;  [{:keys [width height]} {:keys [x y]}]
;  (and (every? #(>= % 0) [x y]) (< x width) (< y height)))
;
;(defn get-at
;  "Gets the value at `x` and `y`."
;  [{:keys [matrix] :as board} {:keys [x y] :as coordinates}]
;  {:pre [(inbound? board coordinates)]}
;  (get-in matrix [y x]))
;
;(defn set-at
;  "Sets the `value` in `x` and `y`."
;  [{:keys [matrix] :as board} {:keys [x y] :as coordinates} value]
;  {:pre [(inbound? board coordinates)]}
;  (assoc board :matrix (assoc-in matrix [y x] value)))
;
;(defn- create-vec
;  "Makes a `vec` given `length` and `value`."
;  [length value]
;  {:pre [(pos? length)]}
;  (vec (repeat length value)))
;
;;; Memoize `make-vec` function for caching purposes.
;(alter-var-root #'create-vec memoize)
;
;(defn- create-matrix
;  "Makes a new matrix."
;  [width height default]
;  {:pre [(every? pos? [width height])]}
;  (create-vec width (create-vec height default)))
;
;(defn create-board
;  "Makes a new `Board2d` record."
;  [width height]
;  (->Board2d (->Size width height))
;  ;(->Board (create-matrix width height default-data) (->Size width height) default-data)
;  )