(ns robots-vs-dinosaurs.logic.unit
  (:require
    (robots-vs-dinosaurs.logic
      [point :as point]
      [direction :as direction])))

(defrecord Dinosaur [id type point])
(defrecord Robot [id type point direction])

(defn new-dinosaur
  [id point]
  (map->Dinosaur
    {:id    id
     :type  :dinosaur
     :point point}))

(defn new-robot
  [id point orientation]
  (map->Robot
    {:id        id
     :type      :robot
     :point     point
     :direction (direction/new-8-sided orientation)}))

(defn dinosaur?
  [o]
  (instance? Dinosaur o))

(defn robot?
  [o]
  (instance? Robot o))

(defn- move
  "Gets the new point of `movement`
  to the `move-handler` handle the movement."
  [{:keys [direction point] :as unit} movement move-handler]
  (move-handler unit (movement point direction)))

(defn move-forward
  "Moves the unit point towards
  the current direction."
  [unit move-handler]
  (move unit point/toward move-handler))

(defn move-backward
  "Moves the unit point away
  of the current direction."
  [unit move-handler]
  (move unit point/away move-handler))

(defn turn-right-4
  "Rotates the unit to the right."
  [unit]
  (update unit :direction direction/inc-4-sided))

(defn turn-left-4
  "Rotates the unit to the left."
  [unit]
  (update unit :direction direction/dec-4-sided))

(defn- attack
  "Attacks around the given directions.
  The `attack-handler` takes 2 arguments,
  the attacker and the points to be attacked."
  [{:keys [point] :as unit} directions attack-handler]
  (attack-handler (point/around directions point)))

(defn attack-4-directions
  "Attacks around the four sides."
  [unit attack-handler]
  (attack unit direction/four-sides attack-handler))