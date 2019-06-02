(ns robots-vs-dinosaurs.logic.unit
  (:require [robots-vs-dinosaurs.logic.point :as p]
            [robots-vs-dinosaurs.logic.direction :as d]))

(defrecord Dinosaur [id point])
(defrecord Robot [id point direction])

(defn new-dinosaur
  [id point]
  (->Dinosaur id point))

(defn new-robot
  [id point orientation]
  (->Robot id point (d/new-four-sided-direction orientation)))

(defn move+
  [unit]
  (p/point+ (:point unit) (-> unit :direction :point)))

(defn move-
  [unit]
  (p/point- (:point unit) (-> unit :direction :point)))

;; defmulti or protocol for other types.
(defn rotate++
  [unit]
  (d/direction++ (:direction unit) d/four-sided-directions))

(defn rotate--
  [unit]
  (d/direction-- (:direction unit) d/four-sided-directions))