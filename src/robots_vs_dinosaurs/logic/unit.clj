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

(defn dinosaur?
  [unit]
  (instance? Dinosaur unit))

(defn new-robot
  [id point orientation]
  (map->Robot
    {:id        id
     :type      :robot
     :point     point
     :direction (direction/new-four-sided orientation)}))

(defn robot?
  [unit]
  (instance? Robot unit))

(defn move-forward
  [{:keys [direction] :as unit}]
  (update-in unit [:point] point/toward direction))

(defn move-backwards
  [{:keys [direction] :as unit}]
  (update-in unit [:point] point/away direction))

(defn turn-right
  [{:keys [direction] :as unit}]
  (update-in unit [:direction] direction/four-sided-inc))

(defn turn-left
  [{:keys [direction] :as unit}]
  (update-in unit [:direction] direction/four-sided-dec))

(defn attack
  [{:keys [point]} on-attack]
  (on-attack (point/around direction/four-sides point)))

;; todo: compose functions attack with remove from board